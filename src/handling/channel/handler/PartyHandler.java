package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import handling.world.exped.MapleExpedition;
import handling.world.exped.PartySearch;
import handling.world.exped.PartySearchType;
import handling.world.sidekick.MapleSidekick;
import java.util.ArrayList;
import java.util.List;
import server.maps.Event_DojoAgent;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.packet.MaplePacketCreator;
import tools.data.LittleEndianAccessor;

public class PartyHandler {

    public static final void DenyPartyRequest(final LittleEndianAccessor slea, final MapleClient c) {
        final int action = slea.readByte();
        if (action == 0x17) {
            MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if (cfrom != null) {
                cfrom.getClient().getSession().write(MaplePacketCreator.partyStatusMessage(23, c.getPlayer().getName()));
            }
        }
    }

    public static final void PartyOperation(final LittleEndianAccessor slea, final MapleClient c) {
        final int operation = slea.readByte();
        MapleParty party = c.getPlayer().getParty();
        MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());

        switch (operation) {
            case 1: // create
                if (party == null) {
                    party = World.Party.createParty(partyplayer);
                    c.getPlayer().setParty(party);
                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));

                } else {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    if (partyplayer.equals(party.getLeader()) && party.getMembers().size() == 1) { //only one, reupdate
                        c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                    } else {
                        c.getPlayer().dropMessage(5, "你已经加入队伍,无法再创建组队.");
                    }
                }
                break;
            case 2: // leave
                if (party != null) { //are we in a party? o.O"
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    if (partyplayer.equals(party.getLeader())) { // disband
                        if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                            Event_DojoAgent.failed(c.getPlayer());
                        }
                        if (c.getPlayer().getPyramidSubway() != null) {
                            c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                        }
                        World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                        if (c.getPlayer().getEventInstance() != null) {
                            c.getPlayer().getEventInstance().disbandParty();
                        }
                    } else {
                        if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                            Event_DojoAgent.failed(c.getPlayer());
                        }
                        if (c.getPlayer().getPyramidSubway() != null) {
                            c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                        }
                        World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                        if (c.getPlayer().getEventInstance() != null) {
                            c.getPlayer().getEventInstance().leftParty(c.getPlayer());
                        }
                    }
                    c.getPlayer().setParty(null);
                }
                break;
            case 3: // accept invitation
                final int partyid = slea.readInt();
                if (party == null) {
                    party = World.Party.getParty(partyid);
                    if (party != null) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                            return;
                        }
                        if (party.getMembers().size() < 6 && c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE)) == null) {
                            c.getPlayer().setParty(party);
                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            c.getSession().write(MaplePacketCreator.partyStatusMessage(0x11, null));
                            //c.getPlayer().dropMessage(5, "The party you're trying to join is already in full capacity.");
                        }
                    } else {
                        //c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
                        c.getSession().write(MaplePacketCreator.partyStatusMessage(0x0D, null));
                    }
                } else {
                    c.getSession().write(MaplePacketCreator.partyStatusMessage(0x10, null));
                    //c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
                }
                break;
            case 4: // invite
                if (party == null) {
                    party = World.Party.createParty(partyplayer);
                    c.getPlayer().setParty(party);
                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                }
                // TODO store pending invitations and check against them
                final String theName = slea.readMapleAsciiString();
                final int theCh = World.Find.findChannel(theName);
                if (theCh > 0) {
                    final MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(theName);
                    if (invited != null && invited.getParty() == null && invited.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE)) == null) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                            return;
                        }
                        if (party.getMembers().size() < 6) {
                            if (invited.getLevel() < 10) {
                                     c.getPlayer().dropMessage(5, "对方等级低于10级，无法进行组队。.");
                            } else {
                                //c.getSession().write(MaplePacketCreator.partyStatusMessage(26, invited.getName()));
                                invited.getClient().getSession().write(MaplePacketCreator.partyInvite(c.getPlayer()));
                            }
                        } else {
                            c.getSession().write(MaplePacketCreator.partyStatusMessage(0x11, null));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.partyStatusMessage(0x10, null));
                        //c.getPlayer().dropMessage(5, "The party you're trying to join is already in full capacity.");
                    }
                } else {
                    c.getSession().write(MaplePacketCreator.partyStatusMessage(0x13, null));
                }
                break;
            case 5: // expel
                if (party != null && partyplayer != null && partyplayer.equals(party.getLeader())) {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    final MaplePartyCharacter expelled = party.getMemberById(slea.readInt());
                    if (expelled != null) {
                        if (GameConstants.isDojo(c.getPlayer().getMapId()) && expelled.isOnline()) {
                            Event_DojoAgent.failed(c.getPlayer());
                        }
                        if (c.getPlayer().getPyramidSubway() != null && expelled.isOnline()) {
                            c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                        }
                        World.Party.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                        if (c.getPlayer().getEventInstance() != null) {
                            /*if leader wants to boot someone, then the whole party gets expelled
                            TODO: Find an easier way to get the character behind a MaplePartyCharacter
                            possibly remove just the expellee.*/
                            if (expelled.isOnline()) {
                                c.getPlayer().getEventInstance().disbandParty();
                            }
                        }
                    }
                }
                break;
            case 6: // change leader
                if (party != null) {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    final MaplePartyCharacter newleader = party.getMemberById(slea.readInt());
                    if (newleader != null && partyplayer.equals(party.getLeader())) {
                        World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newleader);
                    }
                }
                break;
            case 7: //request to  join a party
                if (party != null) {
                    if (c.getPlayer().getEventInstance() != null || c.getPlayer().getPyramidSubway() != null || party.getExpeditionId() > 0 || GameConstants.isDojo(c.getPlayer().getMapId())) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    if (partyplayer.equals(party.getLeader())) { // disband
                        World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                    } else {
                        World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                    }
                    c.getPlayer().setParty(null);
                }
                final int partyid_ = slea.readInt(); //TODO JUMP
                party = World.Party.getParty(partyid_);
                if (party != null && party.getMembers().size() < 6) {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "加入远征队伍的状态下无法进行此操作.");
                        return;
                    }
                    final MapleCharacter cfrom = c.getPlayer().getMap().getCharacterById(party.getLeader().getId());
                    if (cfrom != null && cfrom.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PARTY_REQUEST)) == null) {
                        c.getSession().write(MaplePacketCreator.partyStatusMessage(50, c.getPlayer().getName()));
                        cfrom.getClient().getSession().write(MaplePacketCreator.partyRequestInvite(c.getPlayer()));
                    } else {
                        c.getPlayer().dropMessage(5, "Player was not found or player is not accepting party requests.");
                    }
                }
                break;
            case 8: //allow party requests
                if (slea.readByte() > 0) {
                    c.getPlayer().getQuestRemove(MapleQuest.getInstance(GameConstants.PARTY_REQUEST));
                } else {
                    c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PARTY_REQUEST));
                }
                break;
            default:
                System.out.println("Unhandled Party function." + operation);
                break;
        }
    }

    public static final void AllowPartyInvite(final LittleEndianAccessor slea, final MapleClient c) {
        if (slea.readByte() > 0) {
            c.getPlayer().getQuestRemove(MapleQuest.getInstance(GameConstants.PARTY_INVITE));
        } else {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE));
        }
    }

    public static final void DenySidekickRequest(final LittleEndianAccessor slea, final MapleClient c) {
        final int action = slea.readByte();
        final int cid = slea.readInt();
        if (c.getPlayer().getSidekick() == null && action == 0x5A) { //accept
            MapleCharacter party = c.getPlayer().getMap().getCharacterById(cid);
            if (party != null) {
                if (party.getSidekick() != null || !MapleSidekick.checkLevels(c.getPlayer().getLevel(), party.getLevel())) {
                    return;
                }
                int sid = World.Sidekick.createSidekick(c.getPlayer().getId(), party.getId());
                if (sid <= 0) {
                    c.getPlayer().dropMessage(5, "Please try again.");
                } else {
                    MapleSidekick s = World.Sidekick.getSidekick(sid);
                    c.getPlayer().setSidekick(s);
                    c.getSession().write(MaplePacketCreator.updateSidekick(c.getPlayer(), s, true));
                    party.setSidekick(s);
                    party.getClient().getSession().write(MaplePacketCreator.updateSidekick(party, s, true));
                }
            } else {
                c.getPlayer().dropMessage(5, "The sidekick you are trying to join does not exist");
            }
        }

    }

    public static final void SidekickOperation(final LittleEndianAccessor slea, final MapleClient c) {
        final int operation = slea.readByte();

        switch (operation) {
            case 0x41: // create
                if (c.getPlayer().getSidekick() == null) {
                    final MapleCharacter other = c.getPlayer().getMap().getCharacterByName(slea.readMapleAsciiString());
                    if (other.getSidekick() == null && MapleSidekick.checkLevels(c.getPlayer().getLevel(), other.getLevel())) {
                        other.getClient().getSession().write(MaplePacketCreator.sidekickInvite(c.getPlayer()));
                        c.getPlayer().dropMessage(1, "You have sent the sidekick invite to " + other.getName() + ".");
                    }
                }
                break;
            case 0x3F: // leave
                if (c.getPlayer().getSidekick() != null) {
                    c.getPlayer().getSidekick().eraseToDB();
                }
                break;
        }
    }

    public static final void PartySearchStart(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().isInBlockedMap() || FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) {
            c.getPlayer().dropMessage(5, "You may not do party search here.");
            return;
        } else if (GameConstants.GMS) {
            //replaced with member search
            c.getSession().write(MaplePacketCreator.showMemberSearch(c.getPlayer().getMap().getCharactersThreadsafe()));
            return;
        } else if (c.getPlayer().getParty() == null) {
            MapleParty party = World.Party.createParty(new MaplePartyCharacter(c.getPlayer()));
            c.getPlayer().setParty(party);
            c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
        } else if (c.getPlayer().getParty().getExpeditionId() > 0) {
            c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
            return;
        }
        final int min = slea.readInt();
        final int max = slea.readInt();
        final int members = slea.readInt();
        final int jobs = slea.readInt();
        final List<Integer> jobsList = new ArrayList<Integer>();
        if (max <= min || max - min > 30 || members > 6 || min > c.getPlayer().getLevel() || max < c.getPlayer().getLevel() || jobs == 0) {
            c.getPlayer().dropMessage(1, "An error occurred.");
            return;
        }
        //all jobs = FF FF F7 0F
        //GMS - FF FF DF 7F!, no pirates = FE 7F D0 7F
        if ((jobs & 0x1) != 0) {
            //all jobs? skip check or what
            c.getPlayer().startPartySearch(jobsList, max, min, members);
            return;
        }
        if ((jobs & 0x2) != 0) { //beginner
            jobsList.add(0);
            jobsList.add(1);
            jobsList.add(1000);
            jobsList.add(2000);
            jobsList.add(2001);
            jobsList.add(3000);
        }
        if ((jobs & 0x4) != 0) { //aran
            jobsList.add(2100);
            jobsList.add(2110);
            jobsList.add(2111);
            jobsList.add(2112);
        }
        if ((jobs & 0x8) != 0) { //evan
            jobsList.add(2200);
            jobsList.add(2210);
            jobsList.add(2211);
            jobsList.add(2212);
            jobsList.add(2213);
            jobsList.add(2214);
            jobsList.add(2215);
            jobsList.add(2216);
            jobsList.add(2217);
            jobsList.add(2218);
        }
        if ((jobs & 0x10) != 0) { //swordman
            jobsList.add(100);
        }
        if ((jobs & 0x20) != 0) { //crusader
            jobsList.add(110);
            jobsList.add(111);
            jobsList.add(112);
        }
        if ((jobs & 0x40) != 0) { //knight
            jobsList.add(120);
            jobsList.add(121);
            jobsList.add(122);
        }
        if ((jobs & 0x80) != 0) { //dk
            jobsList.add(130);
            jobsList.add(131);
            jobsList.add(132);
        }
        if ((jobs & 0x100) != 0) { //soul
            jobsList.add(1100);
            jobsList.add(1110);
            jobsList.add(1111);
            jobsList.add(1112);
        }
        if ((jobs & 0x200) != 0) { //mage
            jobsList.add(200);
        }
        if ((jobs & 0x400) != 0) { //fp
            jobsList.add(210);
            jobsList.add(211);
            jobsList.add(212);
        }
        if ((jobs & 0x800) != 0) { //il
            jobsList.add(220);
            jobsList.add(221);
            jobsList.add(222);
        }
        if ((jobs & 0x1000) != 0) { //priest
            jobsList.add(230);
            jobsList.add(231);
            jobsList.add(232);
        }
        if ((jobs & 0x2000) != 0) { //flame
            jobsList.add(1200);
            jobsList.add(1210);
            jobsList.add(1211);
            jobsList.add(1212);
        }
        if ((jobs & 0x4000) != 0) { //battle mage <-- new
            jobsList.add(3200);
            jobsList.add(3210);
            jobsList.add(3211);
            jobsList.add(3212);
        }
        if ((jobs & 0x8000) != 0) { //pirate
            jobsList.add(500);
            jobsList.add(501);
        }
        if ((jobs & 0x10000) != 0) { //viper
            jobsList.add(510);
            jobsList.add(511);
            jobsList.add(512);
        }
        if ((jobs & 0x20000) != 0) { //gs
            jobsList.add(520);
            jobsList.add(521);
            jobsList.add(522);
        }
        if ((jobs & 0x40000) != 0) { //strikr
            jobsList.add(1500);
            jobsList.add(1510);
            jobsList.add(1511);
            jobsList.add(1512);
        }
        if ((jobs & 0x80000) != 0) { //mechanic <-- new
            jobsList.add(3500);
            jobsList.add(3510);
            jobsList.add(3511);
            jobsList.add(3512);
        }
        if ((jobs & 0x100000) != 0) { //teef
            jobsList.add(400);
        }
        //0x200000 doesn't exist in gms
        if ((jobs & 0x400000) != 0) { //hermit
            jobsList.add(410);
            jobsList.add(411);
            jobsList.add(412);
        }
        if ((jobs & 0x800000) != 0) { //cb
            jobsList.add(420);
            jobsList.add(421);
            jobsList.add(422);
        }
        if ((jobs & 0x1000000) != 0) { //nw
            jobsList.add(1400);
            jobsList.add(1410);
            jobsList.add(1411);
            jobsList.add(1412);
        }
        if ((jobs & 0x2000000) != 0) { //db
            jobsList.add(430);
            jobsList.add(431);
            jobsList.add(432);
            jobsList.add(433);
            jobsList.add(434);
        }
        if ((jobs & 0x4000000) != 0) { //archer
            jobsList.add(300);
        }
        if ((jobs & 0x8000000) != 0) { //ranger
            jobsList.add(310);
            jobsList.add(311);
            jobsList.add(312);
        }
        if ((jobs & 0x10000000) != 0) { //sniper
            jobsList.add(320);
            jobsList.add(321);
            jobsList.add(322);
        }
        if ((jobs & 0x20000000) != 0) { //wind breaker
            jobsList.add(1300);
            jobsList.add(1310);
            jobsList.add(1311);
            jobsList.add(1312);
        }
        if ((jobs & 0x40000000) != 0) { //wild hunter <-- new
            jobsList.add(3300);
            jobsList.add(3310);
            jobsList.add(3311);
            jobsList.add(3312);
        }
        if (jobsList.size() > 0) {
            c.getPlayer().startPartySearch(jobsList, max, min, members);
        } else {
            c.getPlayer().dropMessage(1, "An error occurred.");
        }
    }

    public static final void PartySearchStop(final LittleEndianAccessor slea, final MapleClient c) {
        if (GameConstants.GMS) {
            List<MapleParty> parties = new ArrayList<MapleParty>();
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (chr.getParty() != null && chr.getParty().getId() != c.getPlayer().getParty().getId() && !parties.contains(chr.getParty())) {
                    parties.add(chr.getParty());
                }
            }
            c.getSession().write(MaplePacketCreator.showPartySearch(parties));
        }
    }

    public static final void PartyListing(final LittleEndianAccessor slea, final MapleClient c) {
        final int mode = slea.readByte();
        PartySearchType pst;
        MapleParty party;
        switch (mode) {
            case 81: //make
            case 0x9F:
            case -97:
            case -105:
                pst = PartySearchType.getById(slea.readInt());
                if (pst == null || c.getPlayer().getLevel() > pst.maxLevel || c.getPlayer().getLevel() < pst.minLevel) {
                    return;
                }
                if (c.getPlayer().getParty() == null && World.Party.searchParty(pst).size() < 10) {
                    party = World.Party.createParty(new MaplePartyCharacter(c.getPlayer()), pst.id);
                    c.getPlayer().setParty(party);
                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                    final PartySearch ps = new PartySearch(slea.readMapleAsciiString(), pst.exped ? party.getExpeditionId() : party.getId(), pst);
                    World.Party.addSearch(ps);
                    if (pst.exped) {
                        c.getSession().write(MaplePacketCreator.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true));
                    }
                    c.getSession().write(MaplePacketCreator.partyListingAdded(ps));
                } else {
                    c.getPlayer().dropMessage(1, "Unable to create. Please leave the party.");
                }
                break;
            case 83: //display
            case 0xA1:
            case -95:
            case -103:
                pst = PartySearchType.getById(slea.readInt());
                if (pst == null || c.getPlayer().getLevel() > pst.maxLevel || c.getPlayer().getLevel() < pst.minLevel) {
                    return;
                }
                c.getSession().write(MaplePacketCreator.getPartyListing(pst));
                break;
            case 84: //close
            case 0xA2:
            case -94:
            case -102:
                break;
            case 85: //join
            case 0xA3:
            case -93:
            case -101:
                party = c.getPlayer().getParty();
                final MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());
                if (party == null) { //are we in a party? o.O"
                    final int theId = slea.readInt();
                    party = World.Party.getParty(theId);
                    if (party != null) {
                        PartySearch ps = World.Party.getSearchByParty(party.getId());
                        if (ps != null && c.getPlayer().getLevel() <= ps.getType().maxLevel && c.getPlayer().getLevel() >= ps.getType().minLevel && party.getMembers().size() < 6) {
                            c.getPlayer().setParty(party);
                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            //c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
                            c.getPlayer().dropMessage(5, "The party you're trying to join is already in full capacity.");
                        }
                    } else {
                        MapleExpedition exped = World.Party.getExped(theId);
                        if (exped != null) {
                            PartySearch ps = World.Party.getSearchByExped(exped.getId());
                            if (ps != null && c.getPlayer().getLevel() <= ps.getType().maxLevel && c.getPlayer().getLevel() >= ps.getType().minLevel && exped.getAllMembers() < exped.getType().maxMembers) {
                                int partyId = exped.getFreeParty();
                                if (partyId < 0) {
                                    //c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
                                    c.getPlayer().dropMessage(5, "The party you're trying to join is already in full capacity.");
                                } else if (partyId == 0) { //signal to make a new party
                                    party = World.Party.createPartyAndAdd(partyplayer, exped.getId());
                                    c.getPlayer().setParty(party);
                                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                                    c.getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionJoined(c.getPlayer().getName()), null);
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                } else {
                                    c.getPlayer().setParty(World.Party.getParty(partyId));
                                    World.Party.updateParty(partyId, PartyOperation.JOIN, partyplayer);
                                    c.getPlayer().receivePartyMemberHP();
                                    c.getPlayer().updatePartyMemberHP();
                                    c.getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionJoined(c.getPlayer().getName()), null);
                                }
                            } else {
                                c.getSession().write(MaplePacketCreator.expeditionError(0, c.getPlayer().getName()));
                            }
                        }
                    }
                }
                break;
            default:
                if (c.getPlayer().isGM()) {
                    System.out.println("Unknown PartyListing : " + mode + "\n" + slea);
                }
                break;
        }
    }

    public static final void Expedition(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        }
        final int mode = slea.readByte();
        MapleParty part, party;
        String name;
        switch (mode) {
            case 0x3F: //create [PartySearchID]
            case 119:
                final ExpeditionType et = ExpeditionType.getById(slea.readInt());
                if (et != null && c.getPlayer().getParty() == null && c.getPlayer().getLevel() <= et.maxLevel && c.getPlayer().getLevel() >= et.minLevel) {
                    party = World.Party.createParty(new MaplePartyCharacter(c.getPlayer()), et.exped);
                    c.getPlayer().setParty(party);
                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                    c.getSession().write(MaplePacketCreator.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true));
                } else {
                    c.getSession().write(MaplePacketCreator.expeditionError(0, ""));
                }
                break;
            case 0x32: //invite [name]
            case 0x40: // for msea
            case 120:
                name = slea.readMapleAsciiString();
                final int theCh = World.Find.findChannel(name);
                if (theCh > 0) {
                    final MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(name);
                    party = c.getPlayer().getParty();
                    if (invited != null && invited.getParty() == null && party != null && party.getExpeditionId() > 0) {
                        MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                        if (me != null && me.getAllMembers() < me.getType().maxMembers && invited.getLevel() <= me.getType().maxLevel && invited.getLevel() >= me.getType().minLevel) {
                            c.getSession().write(MaplePacketCreator.expeditionError(7, invited.getName()));
                            invited.getClient().getSession().write(MaplePacketCreator.expeditionInvite(c.getPlayer(), me.getType().exped));
                        } else {
                            c.getSession().write(MaplePacketCreator.expeditionError(3, invited.getName()));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.expeditionError(2, name));
                    }
                } else {
                    c.getSession().write(MaplePacketCreator.expeditionError(0, name));
                }
                break;
            case 0x33: //accept invite [name] [int - 7, then int 8? lol.]
            case 0x41: // msea
            case 121:
                name = slea.readMapleAsciiString();
                final int action = slea.readInt();
                final int theChh = World.Find.findChannel(name);
                if (theChh > 0) {
                    final MapleCharacter cfrom = ChannelServer.getInstance(theChh).getPlayerStorage().getCharacterByName(name);
                    if (cfrom != null && cfrom.getParty() != null && cfrom.getParty().getExpeditionId() > 0) {
                        party = cfrom.getParty();
                        MapleExpedition exped = World.Party.getExped(party.getExpeditionId());
                        if (exped != null && action == 8) {
                            if (c.getPlayer().getLevel() <= exped.getType().maxLevel && c.getPlayer().getLevel() >= exped.getType().minLevel && exped.getAllMembers() < exped.getType().maxMembers) {
                                int partyId = exped.getFreeParty();
                                if (partyId < 0) {
                                    //c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
                                    c.getPlayer().dropMessage(5, "The party you're trying to join is already in full capacity.");
                                } else if (partyId == 0) { //signal to make a new party
                                    party = World.Party.createPartyAndAdd(new MaplePartyCharacter(c.getPlayer()), exped.getId());
                                    c.getPlayer().setParty(party);
                                    c.getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                                    c.getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionJoined(c.getPlayer().getName()), null);
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                } else {
                                    c.getPlayer().setParty(World.Party.getParty(partyId));
                                    World.Party.updateParty(partyId, PartyOperation.JOIN, new MaplePartyCharacter(c.getPlayer()));
                                    c.getPlayer().receivePartyMemberHP();
                                    c.getPlayer().updatePartyMemberHP();
                                    c.getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionJoined(c.getPlayer().getName()), null);
                                }
                            } else {
                                c.getSession().write(MaplePacketCreator.expeditionError(3, cfrom.getName()));
                            }
                        } else if (action == 9) {
                            cfrom.getClient().getSession().write(MaplePacketCreator.partyStatusMessage(23, c.getPlayer().getName()));
                        }
                    }
                }
                break;
            case 0x34: //leaving
            case 0x42: // msea
            case 122:
                part = c.getPlayer().getParty();
                if (part != null && part.getExpeditionId() > 0) {
                    final MapleExpedition exped = World.Party.getExped(part.getExpeditionId());
                    if (exped != null) {
                        if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                            Event_DojoAgent.failed(c.getPlayer());
                        }
                        if (exped.getLeader() == c.getPlayer().getId()) { // disband
                            World.Party.disbandExped(exped.getId()); //should take care of the rest
                            if (c.getPlayer().getEventInstance() != null) {
                                c.getPlayer().getEventInstance().disbandParty();
                            }
                        } else if (part.getLeader().getId() == c.getPlayer().getId()) {
                            World.Party.updateParty(part.getId(), PartyOperation.DISBAND, new MaplePartyCharacter(c.getPlayer()));
                            if (c.getPlayer().getEventInstance() != null) {
                                c.getPlayer().getEventInstance().disbandParty();
                            }
                            World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionLeft(c.getPlayer().getName()), null);
                        } else {
                            World.Party.updateParty(part.getId(), PartyOperation.LEAVE, new MaplePartyCharacter(c.getPlayer()));
                            if (c.getPlayer().getEventInstance() != null) {
                                c.getPlayer().getEventInstance().leftParty(c.getPlayer());
                            }
                            World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionLeft(c.getPlayer().getName()), null);
                        }
                        if (c.getPlayer().getPyramidSubway() != null) {
                            c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                        }
                        c.getPlayer().setParty(null);
                    }
                }
                break;
            case 0x35: //kick [cid]
            case 0x43: // msea
            case 123:
                part = c.getPlayer().getParty();
                if (part != null && part.getExpeditionId() > 0) {
                    final MapleExpedition exped = World.Party.getExped(part.getExpeditionId());
                    if (exped != null && exped.getLeader() == c.getPlayer().getId()) {
                        final int cid = slea.readInt();
                        for (int i : exped.getParties()) {
                            final MapleParty par = World.Party.getParty(i);
                            if (par != null) {
                                final MaplePartyCharacter expelled = par.getMemberById(cid);
                                if (expelled != null) {
                                    if (expelled.isOnline() && GameConstants.isDojo(c.getPlayer().getMapId())) {
                                        Event_DojoAgent.failed(c.getPlayer());
                                    }
                                    World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                    if (c.getPlayer().getEventInstance() != null) {
                                        if (expelled.isOnline()) {
                                            c.getPlayer().getEventInstance().disbandParty();
                                        }
                                    }
                                    if (c.getPlayer().getPyramidSubway() != null && expelled.isOnline()) {
                                        c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                                    }
                                    World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionLeft(expelled.getName()), null);
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case 0x36: //give exped leader [cid]
            case 0x44: // msea
            case 124:
                part = c.getPlayer().getParty();
                if (part != null && part.getExpeditionId() > 0) {
                    final MapleExpedition exped = World.Party.getExped(part.getExpeditionId());
                    if (exped != null && exped.getLeader() == c.getPlayer().getId()) {
                        final MaplePartyCharacter newleader = part.getMemberById(slea.readInt());
                        if (newleader != null) {
                            World.Party.updateParty(part.getId(), PartyOperation.CHANGE_LEADER, newleader);
                            exped.setLeader(newleader.getId());
                            World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionLeaderChanged(0), null);
                        }
                    }
                }
                break;
            case 0x37: //give party leader [cid]
            case 0x45: // msea
            case 125:
                part = c.getPlayer().getParty();
                if (part != null && part.getExpeditionId() > 0) {
                    final MapleExpedition exped = World.Party.getExped(part.getExpeditionId());
                    if (exped != null && exped.getLeader() == c.getPlayer().getId()) {
                        final int cid = slea.readInt();
                        for (int i : exped.getParties()) {
                            final MapleParty par = World.Party.getParty(i);
                            if (par != null) {
                                final MaplePartyCharacter newleader = par.getMemberById(cid);
                                if (newleader != null && par.getId() != part.getId()) {
                                    World.Party.updateParty(par.getId(), PartyOperation.CHANGE_LEADER, newleader);
                                }
                            }
                        }
                    }
                }
                break;
            case 0x38: //change party of diff player [partyIndexTo] [cid]
            case 0x46: // msea
            case 126:
                part = c.getPlayer().getParty();
                if (part != null && part.getExpeditionId() > 0) {
                    final MapleExpedition exped = World.Party.getExped(part.getExpeditionId());
                    if (exped != null && exped.getLeader() == c.getPlayer().getId()) {
                        final int partyIndexTo = slea.readInt();
                        if (partyIndexTo < exped.getType().maxParty && partyIndexTo <= exped.getParties().size()) {
                            final int cid = slea.readInt();
                            for (int i : exped.getParties()) {
                                final MapleParty par = World.Party.getParty(i);
                                if (par != null) {
                                    final MaplePartyCharacter expelled = par.getMemberById(cid);
                                    if (expelled != null && expelled.isOnline()) {
                                        final MapleCharacter chr = World.getStorage(expelled.getChannel()).getCharacterById(expelled.getId());
                                        if (chr == null) {
                                            break;
                                        }
                                        if (partyIndexTo < exped.getParties().size()) { //already exists
                                            party = World.Party.getParty(exped.getParties().get(partyIndexTo));
                                            if (party == null || party.getMembers().size() >= 6) {
                                                c.getPlayer().dropMessage(5, "Invalid party.");
                                                break;
                                            }
                                        }
                                        if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                                            Event_DojoAgent.failed(c.getPlayer());
                                        }
                                        World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                        if (partyIndexTo < exped.getParties().size()) { //already exists
                                            party = World.Party.getParty(exped.getParties().get(partyIndexTo));
                                            if (party != null && party.getMembers().size() < 6) {
                                                World.Party.updateParty(party.getId(), PartyOperation.JOIN, expelled);
                                                chr.receivePartyMemberHP();
                                                chr.updatePartyMemberHP();
                                                chr.getClient().getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                            }
                                        } else {
                                            party = World.Party.createPartyAndAdd(expelled, exped.getId());
                                            chr.setParty(party);
                                            chr.getClient().getSession().write(MaplePacketCreator.partyCreated(party.getId()));
                                            chr.getClient().getSession().write(MaplePacketCreator.expeditionStatus(exped, true));
                                            World.Party.expedPacket(exped.getId(), MaplePacketCreator.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                        }
                                        if (c.getPlayer().getEventInstance() != null) {
                                            if (expelled.isOnline()) {
                                                c.getPlayer().getEventInstance().disbandParty();
                                            }
                                        }
                                        if (c.getPlayer().getPyramidSubway() != null) {
                                            c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                if (c.getPlayer().isGM()) {
                    System.out.println("Unknown Expedition : " + mode + "\n" + slea);
                }
                break;
        }
    }
}
