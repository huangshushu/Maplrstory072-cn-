package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import constants.GameConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterIdChannelPair;
import handling.world.CharacterTransfer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.exped.MapleExpedition;
import handling.world.guild.MapleGuild;
import java.util.List;
import server.maps.FieldLimitType;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.MaplePacketCreator;
import tools.data.LittleEndianAccessor;
import tools.packet.CSPacket;
import tools.packet.FamilyPacket;

public class InterServerHandler {

    public static final void EnterCS(final MapleClient c, final MapleCharacter chr) {
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null) || (chr.getEventInstance() != null) || (c.getChannelServer() == null)) {
            c.getSession().write(MaplePacketCreator.serverBlocked(2));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器正忙着。请在一分钟或更少的时间再试.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final ChannelServer ch = ChannelServer.getInstance(c.getChannel());

        chr.changeRemoval();

        if (c.getPlayer().getBuffedValue(MapleBuffStat.SUMMON) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SUMMON, -1);
        }

        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            World.Messenger.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        chr.getMap().removePlayer(chr);
        c.getSession().write(MaplePacketCreator.getChannelChange(c, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
        c.setPlayer(null);
        c.setReceiving(false);
    }

    public static final void EnterMTS(final MapleClient c, final MapleCharacter chr) {
//      no choice since client don't support the way i did earlier, this is the other way to do.       
        chr.dropMessage(1, "本服务端由大王提供，QQ：2215672221。");
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static final void Loggedin(final int playerid, final MapleClient c) {
        final ChannelServer channelServer = c.getChannelServer();
        MapleCharacter player;
        final CharacterTransfer transfer = channelServer.getPlayerStorage().getPendingCharacter(playerid);
        if (transfer == null) { // Player isn't in storage, probably isn't CC
            //Pair<String, String> ip = LoginServer.getLoginAuth(playerid);
            //String s = c.getSessionIPAddress();
            //if (ip == null || !s.substring(s.indexOf('/') + 1, s.length()).equals(ip.left)) {
            //    if (ip != null) {
            //        LoginServer.putLoginAuth(playerid, ip.left, ip.right);
            //    }
            //    c.getSession().close();
            //    return;
            //}
            //c.setTempIP(ip.right);
            player = MapleCharacter.loadCharFromDB(playerid, c, true);
        } else {
            player = MapleCharacter.ReconstructChr(transfer, c, true);
        }
        c.setPlayer(player);
        c.setAccID(player.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }

        final int state = c.getLoginState();
        boolean allowLogin = false;

        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
        }
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close();
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        channelServer.addPlayer(player);

        player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
        player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
        player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));

        c.getSession().write(MaplePacketCreator.getCharInfo(player));
        c.getSession().write(CSPacket.enableCSUse());
        c.getSession().write(MaplePacketCreator.temporaryStats_Reset()); // .
        if (player.isIntern()) {
            SkillFactory.getSkill(9001004).getEffect(1).applyTo(player);
        }
        player.getMap().addPlayer(player);

        try {
            // Start of buddylist
            final int buddyIds[] = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            if (player.getParty() != null) {
                final MapleParty party = player.getParty();
                World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));

                if (party != null && party.getExpeditionId() > 0) {
                    final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                    if (me != null) {
                        c.getSession().write(MaplePacketCreator.expeditionStatus(me, false));
                    }
                }
            }

            if (player.getSidekick() == null) {
                player.setSidekick(World.Sidekick.getSidekickByChr(player.getId()));
            }
            if (player.getSidekick() != null) {
                c.getSession().write(MaplePacketCreator.updateSidekick(player, player.getSidekick(), false));
            }
            final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));

            // Start of Messenger
            final MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()));
                World.Messenger.updateMessenger(messenger.getId(), c.getPlayer().getName(), c.getChannel());
            }

            // Start of Guild and alliance
            if (player.getGuildId() > 0) {
                World.Guild.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(MaplePacketCreator.showGuildInfo(player));
                final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                if (gs != null) {
                    final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                    if (packetList != null) {
                        for (byte[] pack : packetList) {
                            if (pack != null) {
                                c.getSession().write(pack);
                            }
                        }
                    }
                } else { //guild not found, change guild id
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }
            if (player.getFamilyId() > 0) {
                World.Family.setFamilyMemberOnline(player.getMFC(), true, c.getChannel());
            }
            c.getSession().write(FamilyPacket.getFamilyData());
            c.getSession().write(FamilyPacket.getFamilyInfo(player));
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
        player.getClient().getSession().write(MaplePacketCreator.serverMessage(channelServer.getServerMessage()));
        player.sendMacros();
        player.showNote();
        player.sendImp();
        player.updatePartyMemberHP();
        player.startFairySchedule(false);
        player.baseSkills(); //fix people who've lost skills.

        c.getSession().write(MaplePacketCreator.getKeymap(player.getKeyLayout()));
        player.updatePetAuto();
        player.expirationTask(true, transfer == null);
        if (player.getJob() == 132) { // DARKKNIGHT
            player.checkBerserk();
        }
        player.spawnClones();
        player.spawnSavedPets();
        if (player.getStat().equippedSummon > 0) {
            SkillFactory.getSkill(player.getStat().equippedSummon).getEffect(1).applyTo(player);
        }
        if (GameConstants.GMS) {
            c.getSession().write(MaplePacketCreator.getFamiliarInfo(player));
        }
    }

    public static final void ChangeChannel(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "The server is busy at the moment. Please try again in a less than a minute.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final int chc = slea.readByte() + 1;
        if (!World.isChannelAvailable(chc)) {
            chr.dropMessage(1, "The channel is full at the moment.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.changeChannel(chc);
    }
}
