package tools.packet;

import client.BuddylistEntry;
import client.MapleBeans;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleKeyLayout;
import client.MapleQuestStatus;
import client.MapleStat;
import client.MonsterFamiliar;
import client.SkillMacro;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleImp;
import client.inventory.MapleImp.ImpFlag;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.channel.handler.BeanGame;
import handling.channel.handler.InventoryHandler;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.exped.MapleExpedition;
import handling.world.exped.PartySearch;
import handling.world.exped.PartySearchType;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildSkill;
import handling.world.sidekick.MapleSidekick;
import handling.world.sidekick.MapleSidekickCharacter;
import java.awt.Point;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleTrade;
import server.StructFamiliar;
import server.events.MapleSnowball.MapleSnowballs;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.AttackPair;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.PacketHelper;

public class MaplePacketCreator {

    public final static Map<MapleStat, Integer> EMPTY_STATUPDATE = new EnumMap<MapleStat, Integer>(MapleStat.class);
    public final static int[] SecondaryStatRemote = new int[GameConstants.MAX_BUFFSTAT];

    static {
        SecondaryStatRemote[MapleBuffStat.ENERGY_CHARGE.getPosition(true)] |= MapleBuffStat.ENERGY_CHARGE.getValue();
        SecondaryStatRemote[MapleBuffStat.DASH_SPEED.getPosition(true)] |= MapleBuffStat.DASH_SPEED.getValue();
        SecondaryStatRemote[MapleBuffStat.DASH_JUMP.getPosition(true)] |= MapleBuffStat.DASH_JUMP.getValue();
        SecondaryStatRemote[MapleBuffStat.MONSTER_RIDING.getPosition(true)] |= MapleBuffStat.MONSTER_RIDING.getValue();
        SecondaryStatRemote[MapleBuffStat.SPEED_INFUSION.getPosition(true)] |= MapleBuffStat.SPEED_INFUSION.getValue();
        SecondaryStatRemote[MapleBuffStat.HOMING_BEACON.getPosition(true)] |= MapleBuffStat.HOMING_BEACON.getValue();
        SecondaryStatRemote[MapleBuffStat.DEFAULT_BUFFSTAT.getPosition(true)] |= MapleBuffStat.DEFAULT_BUFFSTAT.getValue();
    }

    public static final byte[] getServerIP(final MapleClient c, final int port, final int clientId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        //if (c.getTempIP().length() > 0) {
        //    for (String s : c.getTempIP().split(",")) {
        //        mplew.write(Integer.parseInt(s));
        //    }
        //} else {
        mplew.write(ServerConstants.Gateway_IP_);
        //}
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(0);
        mplew.writeZeroBytes(5);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static final byte[] getChannelChange(final MapleClient c, final int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        //if (c.getTempIP().length() > 0) {
        //    for (String s : c.getTempIP().split(",")) {
        //       mplew.write(Integer.parseInt(s));
        //    }
        //} else {
        mplew.write(ServerConstants.Gateway_IP_);
        //}
        mplew.writeShort(port);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static final byte[] getCharInfo(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        PacketHelper.addCharacterInfo(mplew, chr);
        mplew.writeLong(KoreanDateUtil.getFileTimestamp(System.currentTimeMillis()));

        return mplew.getPacket();
    }

    public static final byte[] enableActions() {
        return updatePlayerStats(EMPTY_STATUPDATE, true, 0);
    }

    public static final byte[] updatePlayerStats(final Map<MapleStat, Integer> stats, final int evan) {
        return updatePlayerStats(stats, false, evan);
    }

    public static final byte[] updatePlayerStats(final Map<MapleStat, Integer> mystats, final boolean itemReaction, final int evan) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        long updateMask = 0;
        for (MapleStat statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt((int) updateMask); // todo jump

        for (final Entry<MapleStat, Integer> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey()) {
                case SKIN:
                case LEVEL:
                    mplew.write((statupdate.getValue()).byteValue());
                    break;
                case JOB:
                case STR:
                case DEX:
                case INT:
                case LUK:
                case HP:
                case MAXHP:
                case MP:
                case MAXMP:
                case AVAILABLEAP:
                case FAME:
                    mplew.writeShort((statupdate.getValue()).shortValue());
                    break;
                //case AVAILABLESP:
                //mplew.writeShort(chr.getRemainingSp(0));
                //break;
                case EXP:
                case FACE:
                case HAIR:
                case MESO:
                    mplew.writeInt((statupdate.getValue()));
                    break;
                default:
                    mplew.writeInt((statupdate.getValue()));
                    break;
            }
        }
        if ((updateMask == 0L) && (!itemReaction)) {
            mplew.write(1);
        }

        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static final byte[] updateSp(MapleCharacter chr, final boolean itemReaction) { //this will do..
        return updateSp(chr, itemReaction, false);
    }

    public static final byte[] updateSp(MapleCharacter chr, final boolean itemReaction, final boolean overrideJob) { //this will do..
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        mplew.writeInt(0x20000); //todo jump
        mplew.writeShort(chr.getRemainingSp(0));
        mplew.writeShort(0);
        return mplew.getPacket();

    }

    public static final byte[] getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {

        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(3);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getStat().getHp());
        mplew.writeLong(KoreanDateUtil.getFileTimestamp(System.currentTimeMillis()));

        return mplew.getPacket();
    }

    public static final byte[] instantMapWarp(final byte portal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        mplew.write(0);
        mplew.write(portal); // 6

        return mplew.getPacket();
    }

    public static final byte[] spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (townId != 999999999 && targetId != 999999999) {
            //mplew.writeInt(skillId);
            mplew.writePos(pos);
        }

        return mplew.getPacket();
    }

    public static final byte[] spawnDoor(final int oid, final Point pos, final boolean animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(oid);
        mplew.writePos(pos);

        return mplew.getPacket();
    }

    public static byte[] removeDoor(int oid, boolean animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(summon.getSkillLevel());
        mplew.writePos(summon.getPosition());
        if (summon.isPuppet()) {
            mplew.write(summon.isFacingLeft() ? 4 : 5);
        } else {
            mplew.write(summon.isFacingLeft() ? 5 : 4);
        }
        mplew.writeShort(0/*summon.getFh()*/);
        mplew.write(summon.getMovementType().getValue());
        mplew.write(summon.getSummonType()); // 0 = Summon can't attack - but puppets don't attack with 1 either ^.-
        mplew.write(/*animated ? 1 : */0);
        mplew.write(0); //no idea todo jump
        //final MapleCharacter chr = summon.getOwner();
        //mplew.write(summon.getSkill() == 4341006 && chr != null ? 1 : 0); //mirror target
        //if (summon.getSkill() == 4341006 && chr != null) {
        //    PacketHelper.addCharLook(mplew, chr, true);
        //}
        // if (summon.getSkill() == 35111002) {
        //     mplew.write(0);
        //}
        mplew.writeZeroBytes(8);

        return mplew.getPacket();
    }

    public static byte[] removeSummon(int ownerId, int objId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(ownerId);
        mplew.writeInt(objId);
        mplew.write(10);
        return mplew.getPacket();
    }

    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        if (animated) {
            switch (summon.getSkill()) {
                case 35121003:
                    mplew.write(10);
                    break;
                case 35111001:
                case 35111010:
                case 35111009:
                case 35111002:
                case 35111005:
                case 35111011:
                case 35121009:
                case 35121010:
                case 35121011:
                case 33101008:
                    mplew.write(5);
                    break;
                default:
                    mplew.write(4);
                    break;
            }
        } else {
            mplew.write(1);
        }
        return mplew.getPacket();
    }

    /**
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading shop is currently unavailable, please try again
     * later.<br>
     * 4: You cannot go into the trade shop, due to the limitation of user
     * count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade
     * Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] serverBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    //9 = cannot join due to party, 1 = cannot join at this time, sry
    public static byte[] pvpBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] serverMessage(String message) {
        return serverMessage(4, 0, message, false);
    }

    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar);
    }

    private static byte[] serverMessage(int type, int channel, String message, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        /*	* 0: [Notice]<br>
         * 1: Popup<br>
         * 2: Megaphone<br>
         * 3: Super Megaphone<br>
         * 4: Scrolling message at top<br>
         * 5: Pink Text<br>
         * 6: Lightblue Text
         * 8: Item megaphone
         * 9: Heart megaphone
         * 10: Skull Super megaphone
         * 11: Green megaphone message?
         * 12: Three line of megaphone text
         * 13: End of file =.="
         * 14: Ani msg
         * 15: Red Gachapon box
         * 18: Blue Notice (again)*/
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        if ((type != 23) && (type != 24)) {
            mplew.writeMapleAsciiString(message);
        }

        switch (type) {
            case 3:
            case 22:
            case 25:
            case 26:
                mplew.write(channel - 1);
                mplew.write(megaEar ? 1 : 0);
                break;
            case 9:
                mplew.write(channel - 1);
                break;
            case 12:
                mplew.writeInt(channel - 1);
                break;
            case 6:
            case 11:
            case 20:
                mplew.writeInt((channel >= 1000000) && (channel < 6000000) ? channel : 0);
                break;
            case 24:
                mplew.writeShort(0);
            case 4:
            case 5:
            case 7:
            case 8:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
        }
        return mplew.getPacket();
    }

    public static byte[] getGachaponMega(final String name, final String message, final Item item, final byte rareness) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(13); // legend
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(0); // 0~3 i think
        //mplew.writeMapleAsciiString(name);
        PacketHelper.addItemInfo(mplew, item, true, true);

        return mplew.getPacket();
    }

    public static byte[] getAniMsg(final int questID, final int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(14);
        mplew.writeShort(questID);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(12);

        if (message.get(0) != null) {
            mplew.writeMapleAsciiString(message.get(0));
        }
        mplew.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                mplew.writeMapleAsciiString(message.get(i));
            }
        }
        mplew.write(channel - 1);
        mplew.write(ear ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getAvatarMega(MapleCharacter chr, int channel, int itemId, String message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        PacketHelper.addCharLook(mplew, chr, true);

        return mplew.getPacket();
    }

    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);

        if (item == null) {
            mplew.write(0);
        } else {
            PacketHelper.addItemInfo(mplew, item, false, false, true);
        }
        return mplew.getPacket();
    }

    public static byte[] echoMegaphone(String name, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ECHO_MESSAGE.getValue());
        mplew.write(0); //1 = Your echo message has been successfully sent
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeMapleAsciiString(name); //name
        mplew.writeMapleAsciiString(message); //message

        return mplew.getPacket();
    }

    public static byte[] spawnNPC(MapleNPC life, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(show ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] removeNPC(final int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    public static byte[] removeNPCController(final int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write(npc.getF() == 1 ? 0 : 1);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.writeInt(0); //job lol
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte) (position.getKey() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, position.getValue());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos = (byte) (pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, position.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, position.getValue());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        mplew.write(0); // mercedes ears
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(npc.getPet(i));
        }
        return mplew.getPacket();
    }

    public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);

        return mplew.getPacket();
    }

    public static byte[] GameMaster_Func(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);

        return mplew.getPacket();
    }

    public static byte[] testCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] rechargeCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    public static final byte[] GainEXP_Monster(final int gain, final boolean white, final int partyinc, final int Class_Bonus_EXP, final int Equipment_Bonus_EXP, final int Premium_Bonus_EXP, final int Sidekick_Bonus_EXP, int parrete, int Marriage_Bonus_EXP, int Friendship_Bonus_EXP) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        byte v2 = 0;
        mplew.write(v2);//是否为任务
        mplew.writeInt(Friendship_Bonus_EXP);//活动奖励
        byte v6 = 0;
        mplew.write(0);//事件百分比
        mplew.write(parrete);//组队经验百分比
        mplew.writeInt(Marriage_Bonus_EXP);//婚礼额外经验
        if (v6 > 0) {
            mplew.write(0);
        }
        if (v2 > 0) {
            byte v2a = 0;
            mplew.write(v2a);
            if (v2a > 0) {
                byte v2aa = 0;
                mplew.write(v2aa);
            }

        }
        mplew.write(0);//派对奖金事件率
        //mplew.writeInt(partyinc); // Bonus EXP for PARTY (+%d) || Bonus Event Party EXP (+%d) x%d
        // Class_Bonus_EXP
        mplew.writeInt(Equipment_Bonus_EXP); // Equip Item Bonus EXP
        //mplew.writeInt(Premium_Bonus_EXP); // Party Ring Bonus EXP(+%d)
        mplew.writeInt(0);
        mplew.writeInt(0);//网吧经验
        mplew.writeInt(0);//彩虹周奖励经验

        return mplew.getPacket();
    }

    public static final byte[] GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(inChat ? 1 : 0);
        mplew.writeZeroBytes(64);
        if (inChat) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static final byte[] getShowFameGain(final int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);

        return mplew.getPacket();
    }

    public static final byte[] showMesoGain(final int gain, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
            mplew.writeInt(gain);
            mplew.writeShort(0); // inet cafe meso gain ?.o
        } else {
            mplew.write(5);
            mplew.writeInt(gain);
            mplew.writeInt(-1);
        }

        return mplew.getPacket();
    }

    public static byte[] getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (inChat) {
            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1); // item count
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        }
        return mplew.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0xF);
        mplew.writeInt(itemId);
        mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }

        return mplew.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(0xF);
        mplew.writeInt(itemId);
        mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }

        return mplew.getPacket();
    }

    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod); // 1 animation, 2 no animation, 3 spawn disappearing item [Fade], 4 spawn disappearing item
        mplew.writeInt(drop.getObjectId()); // item owner id
        mplew.write(drop.getMeso() > 0 ? 1 : 0); // 1 mesos, 0 item, 2 and above all item meso bag,
        mplew.writeInt(drop.getItemId()); // drop object ID
        mplew.writeInt(drop.getOwner()); // owner charid
        mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
        mplew.writePos(dropto);
        mplew.writeInt(0);

        if (mod != 2) {
            mplew.writePos(dropfrom);
            mplew.writeShort(0);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        mplew.write(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeMapleAsciiString(chr.getName());
        if (chr.getGuildId() <= 0) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        final List<Pair<Integer, Integer>> buffvalue = new ArrayList<Pair<Integer, Integer>>();
        int[] mask = SecondaryStatRemote.clone();
        //NOT SURE: FINAL_CUT, OWL_SPIRIT, SPARK
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null || chr.isHidden()) {
            mask[MapleBuffStat.DARKSIGHT.getPosition(true)] |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            mask[MapleBuffStat.SOULARROW.getPosition(true)] |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            mask[MapleBuffStat.COMBO.getPosition(true)] |= MapleBuffStat.COMBO.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()), 1));
        }
        //if (chr.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
        //   mask[MapleBuffStat.WK_CHARGE.getPosition(true)] |= MapleBuffStat.WK_CHARGE.getValue();
        //    buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WK_CHARGE).intValue()), 2));
        //    buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffSource(MapleBuffStat.WK_CHARGE)), 3));
        //}
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            mask[MapleBuffStat.SHADOWPARTNER.getPosition(true)] |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        //---------------------------------------------------------------
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            mask[MapleBuffStat.MORPH.getPosition(true)] |= MapleBuffStat.MORPH.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.MORPH).getMorph(chr)), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
            mask[MapleBuffStat.DIVINE_BODY.getPosition(true)] |= MapleBuffStat.DIVINE_BODY.getValue();
            buffvalue.add(new Pair(chr.getBuffedValue(MapleBuffStat.DIVINE_BODY), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
            mask[MapleBuffStat.BERSERK_FURY.getPosition(true)] |= MapleBuffStat.BERSERK_FURY.getValue();
        }
        //---------------------------------------------------------------
        if (chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) {
            mask[MapleBuffStat.WIND_WALK.getPosition(true)] |= MapleBuffStat.WIND_WALK.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {
            mask[MapleBuffStat.PYRAMID_PQ.getPosition(true)] |= MapleBuffStat.PYRAMID_PQ.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.PYRAMID_PQ)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.SOARING) != null) {
            mask[MapleBuffStat.SOARING.getPosition(true)] |= MapleBuffStat.SOARING.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SOARING).intValue()), 2));
        }
        if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {
            mask[MapleBuffStat.OWL_SPIRIT.getPosition(true)] |= MapleBuffStat.OWL_SPIRIT.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.OWL_SPIRIT)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.FINAL_CUT) != null) {
            mask[MapleBuffStat.FINAL_CUT.getPosition(true)] |= MapleBuffStat.FINAL_CUT.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FINAL_CUT).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.FINAL_CUT)), 3));
        }
        //---------------------------------------------------------------
        if (chr.getBuffedValue(MapleBuffStat.TORNADO) != null) {
            mask[MapleBuffStat.TORNADO.getPosition(true)] |= MapleBuffStat.TORNADO.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.TORNADO).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TORNADO)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.INFILTRATE) != null) {
            mask[MapleBuffStat.INFILTRATE.getPosition(true)] |= MapleBuffStat.INFILTRATE.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null) {
            mask[MapleBuffStat.MECH_CHANGE.getPosition(true)] |= MapleBuffStat.MECH_CHANGE.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MECH_CHANGE).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.MECH_CHANGE)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.DARK_AURA) != null) {
            mask[MapleBuffStat.DARK_AURA.getPosition(true)] |= MapleBuffStat.DARK_AURA.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.DARK_AURA).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DARK_AURA)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.BLUE_AURA) != null) {
            mask[MapleBuffStat.BLUE_AURA.getPosition(true)] |= MapleBuffStat.BLUE_AURA.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BLUE_AURA).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BLUE_AURA)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.YELLOW_AURA) != null) {
            mask[MapleBuffStat.YELLOW_AURA.getPosition(true)] |= MapleBuffStat.YELLOW_AURA.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.YELLOW_AURA).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.YELLOW_AURA)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_SHIELD) != null) {
            mask[MapleBuffStat.DIVINE_SHIELD.getPosition(true)] |= MapleBuffStat.DIVINE_SHIELD.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.GIANT_POTION) != null) {
            mask[MapleBuffStat.GIANT_POTION.getPosition(true)] |= MapleBuffStat.GIANT_POTION.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.GIANT_POTION).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.GIANT_POTION)), 3));
        }
        //---------------------------------------------------------------
        //---------------------------------------------------------------
        if (chr.getBuffedValue(MapleBuffStat.WATER_SHIELD) != null) {
            mask[MapleBuffStat.WATER_SHIELD.getPosition(true)] |= MapleBuffStat.WATER_SHIELD.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SPIRIT_SURGE) != null) {
            mask[MapleBuffStat.SPIRIT_SURGE.getPosition(true)] |= MapleBuffStat.SPIRIT_SURGE.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SPIRIT_SURGE).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffSource(MapleBuffStat.SPIRIT_SURGE)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.SPIRIT_LINK) != null) {
            mask[MapleBuffStat.SPIRIT_LINK.getPosition(true)] |= MapleBuffStat.SPIRIT_SURGE.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SPIRIT_LINK).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.SPIRIT_LINK)), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.FAMILIAR_SHADOW) != null) {
            mask[MapleBuffStat.FAMILIAR_SHADOW.getPosition(true)] |= MapleBuffStat.FAMILIAR_SHADOW.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FAMILIAR_SHADOW).intValue()), 3));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.FAMILIAR_SHADOW).getCharColor()), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.DARK_METAMORPHOSIS) != null) {
            mask[MapleBuffStat.DARK_METAMORPHOSIS.getPosition(true)] |= MapleBuffStat.DARK_METAMORPHOSIS.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.DARK_METAMORPHOSIS).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.DARK_METAMORPHOSIS).getCharColor()), 3));
        }
        for (int i = 0; i < mask.length; i++) {
            mplew.writeInt(mask[i]);
        }
        //TODO: convert this into proper format
        //AFTERSHOCK: extra int here
        for (Pair<Integer, Integer> i : buffvalue) {
            if (i.right == 3) {
                mplew.writeInt(i.left.intValue());
            } else if (i.right == 2) {
                mplew.writeShort(i.left.shortValue());
            } else if (i.right == 1) {
                mplew.write(i.left.byteValue());
            }
        }
        mplew.write(0);//nDefenseAtt_CS
        mplew.write(0);//nDefenseAtt_State
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        //ENERGY_CHARGE
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//1
        mplew.writeShort(0);
        //DASH_SPEED
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//2
        mplew.writeShort(0);
        //DASH_JUMP
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//3
        mplew.writeShort(0);
        //MONSTER_RIDIN
        int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        if (buffSrc > 0) {
            final Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (-118));
            final Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (-18));
            if (GameConstants.getMountItem(buffSrc, chr) == 0 && c_mount != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -119) != null) {
                mplew.writeInt(c_mount.getItemId());
            } else if (GameConstants.getMountItem(buffSrc, chr) == 0 && mount != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null) {
                mplew.writeInt(mount.getItemId());
            } else {
                mplew.writeInt(GameConstants.getMountItem(buffSrc, chr));
            }
            mplew.writeInt(buffSrc);
        } else {
            mplew.writeLong(0);
        }
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//4
        //SPEED_INFUSION
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//5
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//6
        mplew.writeShort(0);
        //HOMING_BEACON
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//7
        mplew.writeInt(0);//dwMobID
        //DEFAULT_BUFFSTAT
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//8
        mplew.writeShort(0);//结束
        mplew.writeShort(chr.getJob());
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //max is like 100. but w/e
        mplew.writeInt(chr.getItemEffect());
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("");
        mplew.writeShort(-1);
        mplew.writeShort(-1);
        mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        mplew.writePos(chr.getTruePosition());
        mplew.write(chr.getStance());
        mplew.writeShort(0); //FH
        mplew.write(0); //pets, follows same structure as charinfo/spawninfo
        mplew.writeInt(chr.getMount().getLevel()); // mount lvl
        mplew.writeInt(chr.getMount().getExp()); // exp
        mplew.writeInt(chr.getMount().getFatigue()); // tiredness
        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getMid());
        addMRingInfo(mplew, rings.getRight(), chr);
        int v5 = 0;
        mplew.write(0);
        if (v5 == 1) {
            mplew.writeInt(0);
        }
        mplew.write(0);

        if (chr.getCarnivalParty() != null) {
            mplew.write(chr.getCarnivalParty().getTeam());
        } else if (chr.getMapId() == 109080000 || chr.getMapId() == 109080010) {
            mplew.write(chr.getTeam());
        }
        return mplew.getPacket();
    }

    public static byte[] removePlayerFromMap(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        mplew.writeInt(-1); //itemid of expression use
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writePos(startPos);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writePos(startPos);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] summonAttack(final int cid, final int summonSkillId, final byte animation, final List<Pair<Integer, Integer>> allDamage, final int level, final boolean darkFlare) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        //mplew.write(level - 1); //? guess
        mplew.write(animation);
        mplew.write(allDamage.size());

        for (final Pair<Integer, Integer> attackEntry : allDamage) {
            mplew.writeInt(attackEntry.left); // oid
            mplew.write(7); // who knows
            mplew.writeInt(attackEntry.right); // damage
        }
        mplew.write(darkFlare ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] closeRangeAttack(int cid, int tbyte, int skill, int level, int display, byte speed, List<AttackPair> damage, final boolean energy, int lvl, byte mastery, byte unk, int charge) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(level); //?
        if (level > 0) {
            mplew.writeInt(skill);
        }
        mplew.write(unk); // Added on v.82
        mplew.writeShort(display);
        mplew.write(speed);
        //mplew.write(mastery); // Mastery
        mplew.writeInt(charge);

        if (skill == 4211006) {
            for (AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(0x07);
                    mplew.write(oned.attack.size());
                    for (Pair<Integer, Boolean> eachd : oned.attack) {
                        if (eachd.right) {
                            mplew.writeInt((eachd.left) + 0x80000000);//暴击
                        } else {
                            mplew.writeInt(eachd.left); //m.e. is never crit
                        }
                    }
                }
            }
        } else {
            for (AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(0x07);
                    for (Pair<Integer, Boolean> eachd : oned.attack) {
                        if (eachd.right) {
                            mplew.writeInt((eachd.left) + 0x80000000);
                        } else {
                            mplew.writeInt(eachd.left);
                        }
                    }
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] strafeAttack(int cid, byte tbyte, int skill, int level, int display, byte speed, int itemid, List<AttackPair> damage, final Point pos, int lvl, byte mastery, byte unk, int ultLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(level);
        if (level > 0) {
            mplew.writeInt(skill);
        }
        mplew.write(unk);
        mplew.writeShort(display);
        mplew.write(speed);
        //mplew.write(mastery);
        mplew.writeInt(itemid);

        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(0x07);
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        mplew.writeInt((eachd.left) + 0x80000000);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        mplew.writePos(pos); // Position

        return mplew.getPacket();
    }

    public static byte[] rangedAttack(int cid, byte tbyte, int skill, int level, int display, byte speed, int itemid, List<AttackPair> damage, final Point pos, int lvl, byte mastery, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(level);
        if (skill > 0) {
            mplew.writeInt(skill);
        }
        mplew.write(unk); // Added on v.82
        mplew.writeShort(display);
        mplew.write(speed);
        //mplew.write(mastery); 
        mplew.writeInt(itemid);

        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(0x07); //
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        mplew.writeInt((eachd.left) + 0x80000000);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        mplew.writePos(pos); // Position

        return mplew.getPacket();
    }

    public static byte[] magicAttack(int cid, int tbyte, int skill, int level, int display, byte speed, List<AttackPair> damage, int charge, int lvl, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(level);
        mplew.writeInt(skill);
        mplew.write(unk); // Added on v.82
        mplew.writeShort(display);
        mplew.write(speed);
        mplew.writeInt(0);

        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(0x07); //
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        mplew.writeInt((eachd.left) + 0x80000000);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        if (charge > 0) {
            mplew.writeInt(charge);
        }
        return mplew.getPacket();
    }

    public static byte[] getNPCShop(int sid, MapleShop shop, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        PacketHelper.addShopInfo(mplew, shop, c);
        return mplew.getPacket();
    }

    public static byte[] confirmShopTransaction(byte code, MapleShop shop, MapleClient c, int indexBought) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code); // 8 = sell, 0 = buy, 0x20 = due to an error
        return mplew.getPacket();
    }

    public static byte[] addInventorySlot(MapleInventoryType type, Item item) {
        return addInventorySlot(type, item, false);
    }

    public static byte[] addInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1); // how many items to add
        mplew.write(item.getPosition() > 100 && type == MapleInventoryType.ETC ? 9 : 0);
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        PacketHelper.addItemInfo(mplew, item, true, false);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1); //how many items to update
        mplew.write(item.getPosition() > 100 && type == MapleInventoryType.ETC ? 6 : 1); //bag
        mplew.write(type.getType()); // iv type
        mplew.writeShort(item.getPosition()); // slot id
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, boolean bag, boolean bothBag) {
        return moveInventoryItem(type, src, dst, (byte) -1, bag, bothBag);
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator, boolean bag, boolean bothBag) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.write(bag ? (bothBag ? 8 : 5) : 2);
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.writeShort(equipIndicator);
        }
        return mplew.getPacket();
    }

    public static byte[] moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total, boolean bag, boolean switchSrcDst, boolean bothBag) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(2);
        mplew.write(bag && (switchSrcDst || bothBag) ? 7 : 3);
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.write(bag && (!switchSrcDst || bothBag) ? 6 : 1); // merge mode?
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);

        return mplew.getPacket();
    }

    public static byte[] moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ, short dstQ, boolean bag, boolean switchSrcDst, boolean bothBag) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(2);
        mplew.write(bag && (switchSrcDst || bothBag) ? 6 : 1);
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(bag && (!switchSrcDst || bothBag) ? 6 : 1);
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);

        return mplew.getPacket();
    }

    public static byte[] dropInventoryItem(MapleInventoryType type, short src) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.write(3);
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }

    public static byte[] dropInventoryItemUpdate(MapleInventoryType type, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());

        return mplew.getPacket();
    }

    public static byte[] clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);
        mplew.write(slot > 100 && type == MapleInventoryType.ETC ? 7 : 3); //bag
        mplew.write(type.getType());
        mplew.writeShort(slot);

        return mplew.getPacket();
    }

    public static byte[] updateSpecialItemUse(Item item, byte invType, MapleCharacter chr) {
        return updateSpecialItemUse(item, invType, item.getPosition(), false, chr);
    }

    public static byte[] updateSpecialItemUse(Item item, byte invType, short pos, boolean theShort, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(2); // always 2?
        mplew.write(invType == MapleInventoryType.ETC.getType() && pos >= 101 && pos <= 512 ? 7 : 3); // quantity > 0 (?)
        mplew.write(invType); // Inventory type
        mplew.writeShort(pos); // item slot
        mplew.write(0);
        mplew.write(invType);
        if (item.getType() == 1 || theShort) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true, false, false, chr);
        if (pos < 0) {
            mplew.write(2); //?
        }

        return mplew.getPacket();
    }

    public static byte[] updateSpecialItemUse_(Item item, byte invType, MapleCharacter chr) {
        return updateSpecialItemUse_(item, invType, item.getPosition(), chr);
    }

    public static byte[] updateSpecialItemUse_(Item item, byte invType, short pos, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(1); // always 2
        mplew.write(0); // quantity > 0 (?)
        mplew.write(invType); // Inventory type
        if (item.getType() == 1) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true, false, false, chr);
        if (pos < 0) {
            mplew.write(1); //?
        }

        return mplew.getPacket();
    }

    public static byte[] scrolledItem(Item scroll, Item item, boolean destroyed, boolean potential) {
        return scrolledItem(scroll, item, destroyed, potential, false);
    }

    public static byte[] scrolledItem(Item scroll, Item item, boolean destroyed, boolean potential, boolean glass) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); // fromdrop always true
        mplew.write(destroyed ? 2 : 3);
        mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
        mplew.write(GameConstants.getInventoryType(scroll.getItemId()).getType()); //can be cash
        mplew.writeShort(scroll.getPosition());

        if (scroll.getQuantity() > 0) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (!potential) {
            mplew.write(1);
        }
        if (glass) {
            mplew.write(3);
        }
        return mplew.getPacket();
    }

    public static byte[] moveAndUpgradeItem(MapleInventoryType type, Item item, short oldpos, short newpos, MapleCharacter chr) {//equipping some items  
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); //fromdrop
        mplew.write(3);
        mplew.write(type == MapleInventoryType.ETC && newpos > 100 ? 7 : 3);
        mplew.write(type.getType());
        mplew.writeShort(oldpos);
        mplew.write(0);
        mplew.write(1);
        mplew.writeShort(oldpos);
        PacketHelper.addItemInfo(mplew, item, true, true, false, false, chr);
        mplew.write(2);
        mplew.write(type.getType());
        mplew.writeShort(oldpos);//oldslot
        mplew.writeShort(newpos);//new slot
        mplew.write(0);//?
        return mplew.getPacket();
    }

    public static byte[] getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll, int scroll, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            default:
                throw new IllegalArgumentException("effect in illegal range");
        }
        mplew.write(0); //? pam's song?
        return mplew.getPacket();
    }

    //miracle cube?
    public static byte[] getPotentialReset(final boolean failed, final int chr, final int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
        mplew.writeInt(chr);
        mplew.write(failed ? 0 : 1);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    //magnify glass
    public static byte[] getMagnifyingGlass(final int chr, final short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MAGNIFY_GLASS.getValue());
        mplew.writeInt(chr);
        mplew.writeShort(pos);
        return mplew.getPacket();
    }

    public static final byte[] ItemMaker_Success() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x11); //bb +2
        mplew.writeZeroBytes(4);

        return mplew.getPacket();
    }

    public static final byte[] ItemMaker_Success_3rdParty(final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(0x11);
        mplew.writeZeroBytes(4);

        return mplew.getPacket();
    }

    public static byte[] explodeDrop(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4); // 4 = Explode
        mplew.writeInt(oid);
        mplew.writeShort(655);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // 0 = Expire, 1 = without animation, 2 = pickup, 4 = explode, 5 = pet pickup
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (animation == 5) { // allow pet pickup?
                mplew.writeInt(slot);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false);
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getMid());
        addMRingInfo(mplew, rings.getRight(), chr);
        mplew.writeInt(0); // -> charid to follow (4)
        return mplew.getPacket();
    }

    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
        mplew.write(rings.size() > 0 ? 1 : 0);
        if (rings.size() > 0) {
            mplew.writeInt(rings.size());
            for (MapleRing ring : rings) {
                mplew.writeLong(ring.getRingId());
                mplew.writeLong(ring.getPartnerRingId());
                mplew.writeInt(ring.getItemId());
            }
        }
    }

    public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
        mplew.write(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            //mplew.writeInt(ring.getItemId());
        }
    }

    public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, byte direction, int reflect, boolean is_pg, int oid, int pos_x, int pos_y, int offset) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);

        if (skill >= -1) {
            mplew.writeInt(monsteridfrom);
            mplew.write(direction);
            if (reflect > 0) {
                mplew.write(reflect);
                mplew.write(is_pg ? 1 : 0);
                mplew.writeInt(oid);
                mplew.write(6);
                mplew.writeShort(pos_x);
                mplew.writeShort(pos_y);
                mplew.write(0);
            } else {
                mplew.writeShort(0);
            }
        }
        //mplew.write(offset);
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }

    public static final byte[] updateQuest(final MapleQuestStatus quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest.getQuest().getId());
        mplew.write(quest.getStatus());
        switch (quest.getStatus()) {
            case 0:
                mplew.writeZeroBytes(10);
                break;
            case 1:
                mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
                break;
            case 2:
                mplew.writeLong(KoreanDateUtil.getFileTimestamp(System.currentTimeMillis()));
                break;
        }

        return mplew.getPacket();
    }

    public static final byte[] updateInfoQuest(final int quest, final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x0A); //AFTERSHOCK: 0x0C
        mplew.writeShort(quest);
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress); //bb - 10
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8); //bb - 10
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);
        return mplew.getPacket();
    }

    public static final byte[] charInfo(final MapleCharacter chr, final boolean isSelf) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.getMarriageId() > 0 ? 1 : 0); // heart red or gray
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("-");
            mplew.writeMapleAsciiString("");
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    final MapleGuildAlliance allianceName = World.Alliance.getAlliance(gs.getAllianceId());
                    if (allianceName != null) {
                        mplew.writeMapleAsciiString(allianceName.getName());
                    } else {
                        mplew.writeMapleAsciiString("");
                    }
                } else {
                    mplew.writeMapleAsciiString("");
                }
            } else {
                mplew.writeMapleAsciiString("-");
                mplew.writeMapleAsciiString("");
            }

        }

        byte index = 0;
        mplew.write(chr.getPet(0) != null ? 1 : 0);
        for (final MaplePet pet : chr.getSummonedPets()) {
            if (pet.getSummoned()) {
                mplew.writeInt(pet.getPetItemId()); // petid
                mplew.writeMapleAsciiString(pet.getName());
                mplew.write(pet.getLevel()); // pet level
                mplew.writeShort(pet.getCloseness()); // pet closeness
                mplew.write(pet.getFullness()); // pet fullness
                mplew.writeShort(pet.getFlags()); // pet flag
                final Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (index == 1 ? -114 : (index == 2 ? -122 : -124)));
                mplew.writeInt(inv == null ? 0 : inv.getItemId());
                mplew.write(chr.getSummonedPets().size() - (index + 1));
                index++;
            }
        }

        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            mplew.write(canwear ? 1 : 0);
            if (canwear) {
                mplew.writeInt(mount.getLevel());
                mplew.writeInt(mount.getExp());
                mplew.writeInt(mount.getFatigue());
            }
        } else {
            mplew.write(0);
        }

        final int wishlistSize = chr.getWishlistSize();
        mplew.write(wishlistSize);
        if (wishlistSize > 0) {
            final int[] wishlist = chr.getWishlist();
            for (int x = 0; x < wishlistSize; x++) {
                mplew.writeInt(wishlist[x]);
            }
        }
        chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), mplew);

        Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -21);
        mplew.writeInt(medal == null ? 0 : medal.getItemId());
        List<Pair<Integer, Long>> medalQuests = chr.getCompletedMedals();
        mplew.writeShort(medalQuests.size());
        for (Pair<Integer, Long> x : medalQuests) {
            mplew.writeShort(x.left);
            mplew.writeLong(x.right);
        }

        return mplew.getPacket();
    }

    public static byte[] giveDice(int buffid, int skillid, int duration, Map<MapleBuffStat, Integer> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        PacketHelper.writeBuffMask(mplew, statups);

        mplew.writeShort(Math.max(buffid / 100, Math.max(buffid / 10, buffid % 10))); // 1-6

        mplew.writeInt(skillid); // skillid
        mplew.writeInt(duration);
        mplew.writeShort(0);
        mplew.writeInt(GameConstants.getDiceStat(buffid, 3));
        mplew.writeInt(GameConstants.getDiceStat(buffid, 3));
        mplew.writeInt(GameConstants.getDiceStat(buffid, 4));
        mplew.writeZeroBytes(20); //idk
        mplew.writeInt(GameConstants.getDiceStat(buffid, 2));
        mplew.writeZeroBytes(12); //idk
        mplew.writeInt(GameConstants.getDiceStat(buffid, 5));
        mplew.writeZeroBytes(16); //idk
        mplew.writeInt(GameConstants.getDiceStat(buffid, 6));
        mplew.writeZeroBytes(16);
        mplew.write(1);
        mplew.write(4); // Total buffed times

        return mplew.getPacket();
    }

    public static byte[] giveMount(int buffid, int skillid, Map<MapleBuffStat, Integer> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        PacketHelper.writeBuffMask(mplew, statups);

        mplew.writeShort(0);
        mplew.writeInt(buffid); // 1902000 saddle
        mplew.writeInt(skillid); // skillid
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0xFF);

        return mplew.getPacket();
    }

    //monster oid, %damage increase
    public static byte[] giveArcane(Map<Integer, Integer> statups, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.ARCANE_AIM);

        mplew.writeShort(0);
        mplew.writeInt(statups.size());
        for (Entry<Integer, Integer> stat : statups.entrySet()) {
            mplew.writeInt(stat.getKey());
            mplew.writeLong(stat.getValue());
            mplew.writeInt(duration);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] givePirate(Map<MapleBuffStat, Integer> statups, int duration, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005 || skillid % 10000 == 8006;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeBuffMask(mplew, statups);

        mplew.writeShort(0);
        for (Integer stat : statups.values()) {
            mplew.writeInt(stat.intValue());
            mplew.writeLong(skillid);
            mplew.writeZeroBytes(infusion ? 6 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(1); //does this only come in dash?
        return mplew.getPacket();
    }

    public static byte[] giveForeignPirate(Map<MapleBuffStat, Integer> statups, int duration, int cid, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005 || skillid % 10000 == 8006;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        PacketHelper.writeBuffMask(mplew, statups);
        mplew.writeShort(0);
        for (Integer stat : statups.values()) {
            mplew.writeInt(stat.intValue());
            mplew.writeLong(skillid);
            mplew.writeZeroBytes(infusion ? 6 : 1);
            mplew.writeShort(duration);//duration... seconds
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] giveHoming(int skillid, int mobid, int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.HOMING_BEACON);
        mplew.writeShort(0);
        mplew.writeInt(1);
        mplew.writeLong(skillid);
        mplew.write(0);
        mplew.writeLong(mobid);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] giveEnergyChargeTest(int bar, int bufflength) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.ENERGY_CHARGE);
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        mplew.writeLong(0); //skillid, but its 0 here
        mplew.write(0);
        mplew.writeInt(bar >= 10000 ? bufflength : 0);//short - bufflength...50
        return mplew.getPacket();
    }

    public static byte[] giveEnergyChargeTest(int cid, int bar, int bufflength) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.ENERGY_CHARGE);
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        mplew.writeLong(0); //skillid, but its 0 here
        mplew.write(0);
        mplew.writeInt(bar >= 10000 ? bufflength : 0);//short - bufflength...50
        return mplew.getPacket();
    }

    public static byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        PacketHelper.writeBuffMask(mplew, statups);
        boolean stacked = false;
        boolean isAura = false;
        for (Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
            if (stat.getKey() == MapleBuffStat.YELLOW_AURA || stat.getKey() == MapleBuffStat.BLUE_AURA) {
                isAura = true;
            }
            if (stat.getKey().canStack()) {
                if (!stacked) {
                    mplew.writeZeroBytes(2);
                    stacked = true;
                }
                mplew.writeInt(1);
                mplew.writeInt(buffid);
                mplew.writeLong(stat.getValue().intValue());
            } else {
                if (statups.containsKey(MapleBuffStat.BODY_BOOST)) {
                    mplew.writeInt(0);
                }
                if (stat.getKey() == MapleBuffStat.SPIRIT_SURGE) {
                    mplew.writeInt(stat.getValue().intValue());
                } else {
                    mplew.writeShort(stat.getValue().intValue());
                }
                mplew.writeInt(buffid);
            }
            mplew.writeInt(bufflength);
        }
        if (!isAura) {
            mplew.writeShort(0);
            if (effect != null && effect.isDivineShield()) {
                mplew.writeInt(effect.getEnhancedWatk());
            } else if (effect != null && effect.getCharColor() > 0) {
                mplew.writeInt(effect.getCharColor());
            } else if (effect != null && effect.isInflation()) {
                mplew.writeInt(effect.getInflation());
            }
        }
        mplew.writeShort(0); // delay
        mplew.write(1);
        mplew.write(effect != null && effect.isShadow() ? 1 : 4); // Test
        if (isAura) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] giveDebuff(MapleDisease statups, int x, int skillid, int level, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        PacketHelper.writeSingleMask(mplew, statups);

        mplew.writeShort(x);
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeInt(duration);
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(0); //Delay
        mplew.write(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] giveForeignDebuff(int cid, final MapleDisease statups, int skillid, int level, int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        PacketHelper.writeSingleMask(mplew, statups);
        if (skillid == 125) {
            mplew.writeShort(0);
        }
        mplew.writeShort(x);
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(0); //Delay
        mplew.write(1);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] cancelForeignDebuff(int cid, MapleDisease mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        PacketHelper.writeSingleMask(mplew, mask);
        mplew.write(3);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showMonsterRiding(int cid, Map<MapleBuffStat, Integer> statups, int itemId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        PacketHelper.writeBuffMask(mplew, statups);

        mplew.writeShort(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(4);

        return mplew.getPacket();
    }

    public static byte[] giveForeignBuff(int cid, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        PacketHelper.writeBuffMask(mplew, statups);
        for (Entry<MapleBuffStat, Integer> statup : statups.entrySet()) {
            if (statup.getKey() == MapleBuffStat.SHADOWPARTNER || statup.getKey() == MapleBuffStat.DARK_METAMORPHOSIS || statup.getKey() == MapleBuffStat.MECH_CHANGE || statup.getKey() == MapleBuffStat.DARK_AURA || statup.getKey() == MapleBuffStat.YELLOW_AURA || statup.getKey() == MapleBuffStat.BLUE_AURA || statup.getKey() == MapleBuffStat.GIANT_POTION || statup.getKey() == MapleBuffStat.SPIRIT_LINK || statup.getKey() == MapleBuffStat.PYRAMID_PQ || statup.getKey() == MapleBuffStat.WK_CHARGE || statup.getKey() == MapleBuffStat.SPIRIT_SURGE || statup.getKey() == MapleBuffStat.MORPH) {
                mplew.writeShort(statup.getValue().shortValue());
                mplew.writeInt(effect.isSkill() ? effect.getSourceId() : -effect.getSourceId());
            } else if (statup.getKey() == MapleBuffStat.FAMILIAR_SHADOW) {
                mplew.writeInt(statup.getValue().intValue());
                mplew.writeInt(effect.getCharColor());
            } else {
                mplew.writeShort(statup.getValue().shortValue());
            }
        }
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        PacketHelper.writeMask(mplew, statups);
        mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] cancelBuff(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        PacketHelper.writeMask(mplew, statups);
        for (MapleBuffStat z : statups) {
            if (z.canStack()) {
                mplew.writeInt(0); //amount of buffs still in the stack? dunno mans
            }
        }
        mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] cancelHoming() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        PacketHelper.writeSingleMask(mplew, MapleBuffStat.HOMING_BEACON);

        return mplew.getPacket();
    }

    public static byte[] cancelDebuff(MapleDisease mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        PacketHelper.writeSingleMask(mplew, mask);
        mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] mountInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());

        return mplew.getPacket();
    }

    public static byte[] getTradeInvite(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2);
        mplew.write(3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(0); // Trade ID

        return mplew.getPacket();
    }

    public static byte[] getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0E);
        mplew.write(number);
        mplew.writeInt(meso);

        return mplew.getPacket();
    }

    public static byte[] getTradeItemAdd(byte number, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xD);
        mplew.write(number);
        PacketHelper.addItemInfo(mplew, item, false, false, true);

        return mplew.getPacket();
    }

    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(3); // 6 for cash trade
        mplew.write(2);
        mplew.write(number);

        if (number == 1) {
            mplew.write(0);
            PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            //mplew.writeShort(trade.getPartner().getChr().getJob());
        }
        mplew.write(number);
        PacketHelper.addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        //mplew.writeShort(c.getPlayer().getJob());
        mplew.write(0xFF);

        return mplew.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0F);

        return mplew.getPacket();
    }

    public static byte[] TradeMessage(final byte UserSlot, final byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(UserSlot);
        mplew.write(message);
        //0x02 = cancelled
        //0x07 = success [tax is automated]
        //0x08 = unsuccessful
        //0x09 = "You cannot make the trade because there are some items which you cannot carry more than one."
        //0x0A = "You cannot make the trade because the other person's on a different map."

        return mplew.getPacket();
    }

    public static byte[] getTradeCancel(final byte UserSlot, final int unsuccessful) { //0 = canceled 1 = invent space 2 = pickuprestricted
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(UserSlot);
        mplew.write(unsuccessful == 0 ? 2 : (unsuccessful == 1 ? 9 : 10));

        return mplew.getPacket();
    }

    public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
        return getNPCTalk(npc, msgType, talk, endBytes, type, npc);
    }

    public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int diffNPC) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));

        return mplew.getPacket();
    }

    public static final byte[] getMapSelection(final int npcid, final String sel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npcid);
        mplew.write(0xD);
        mplew.writeInt(npcid == 2083006 ? 1 : 0); //neo city
        mplew.writeInt(npcid == 9010022 ? 1 : 0); //dimensional
        mplew.writeMapleAsciiString(sel);

        return mplew.getPacket();
    }

    public static byte[] getNPCTalkStyle(int npc, String talk, boolean android, int... args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(android ? 8 : 7);
        mplew.writeMapleAsciiString(talk);
        mplew.write(args.length);

        for (int i = 0; i < args.length; i++) {
            mplew.writeInt(args[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(3);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getNPCTalkText(int npc, String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(2);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showForeignEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect); // 0 = Level up, 8 = job change

        return mplew.getPacket();
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel) {
        return showBuffeffect(cid, skillid, effectid, playerLevel, skillLevel, (byte) 3);
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid); //ehh?
        mplew.writeInt(skillid);
        //mplew.write(playerLevel - 1); //player level
        mplew.write(skillLevel); //skill level
        if (direction != (byte) 3) {
            mplew.write(direction);
        }
        mplew.writeZeroBytes(20);
        return mplew.getPacket();
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
        return showOwnBuffEffect(skillid, effectid, playerLevel, skillLevel, (byte) 3);
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        //mplew.write(playerLevel - 1); //player level
        mplew.write(skillLevel); //skill level
        if (direction != (byte) 3) {
            mplew.write(direction);
        }

        return mplew.getPacket();
    }

    public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(3);
        mplew.writeInt(effectid);
        mplew.writeInt(effectid2); //TODO JUMP lol
        mplew.writeInt(skillid);
        mplew.write(level);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] showDiceEffect(int cid, int skillid, int effectid, int effectid2, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(3);
        mplew.writeInt(effectid);
        mplew.writeInt(effectid2); //TODO JUMP lol
        mplew.writeInt(skillid);
        mplew.write(level);
        mplew.write(0);
        mplew.writeZeroBytes(100);

        return mplew.getPacket();
    }

    public static byte[] showItemLevelupEffect() {
        return showSpecialEffect(0x10); //bb +2
    }

    public static byte[] showForeignItemLevelupEffect(int cid) {
        return showSpecialEffect(cid, 0x10); //bb +2
    }

    public static byte[] showSpecialEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);

        return mplew.getPacket();
    }

    public static byte[] showSpecialEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);

        return mplew.getPacket();
    }

    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        //PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(4); // 2?

        return mplew.getPacket();
    }

    public static final byte[] updateQuestMobKills(final MapleQuestStatus status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);

        final StringBuilder sb = new StringBuilder();
        for (final int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        mplew.writeMapleAsciiString(sb.toString());
        mplew.writeZeroBytes(8);

        return mplew.getPacket();
    }

    public static byte[] getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);

        return mplew.getPacket();
    }

    public static byte[] getKeymap(MapleKeyLayout layout) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());

        layout.writeData(mplew);

        return mplew.getPacket();
    }

    public static byte[] petAutoHP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] petAutoMP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);//  0x0 = cannot find char, 0x1 = success

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMap(String target, int mapid, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeZeroBytes(8); // ?? official doesn't send zeros here but whatever

        return mplew.getPacket();
    }

    public static byte[] getFindReply(String target, int channel, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);

        return mplew.getPacket();
    }

    public static byte[] getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0); // fix dc issue for nl.

        return mplew.getPacket();
    }

    public static byte[] getInventoryStatus() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.writeShort(0);
        mplew.write(0); // fix dc issue for nl.

        return mplew.getPacket();
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x16);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(126);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.write((byte) items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getStorageFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x11);

        return mplew.getPacket();
    }

    public static byte[] mesoStorage(byte slots, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x13);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);

        return mplew.getPacket();
    }

    public static byte[] arrangeStorage(byte slots, Collection<Item> items, boolean changed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x0F);
        mplew.write(slots);
        mplew.writeShort(124);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x0D);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }

    public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }

    public static byte[] fairyPendantMessage(int hours, int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        //mplew.writeInt(GameConstants.getExpRingType(itemId)); // type
        mplew.writeInt(21); // fairys
        mplew.writeInt(hours); // hours
        mplew.writeInt(percent); // percent

        return mplew.getPacket();
    }

    public static byte[] giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeInt(newfame);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        /*	* 0: ok, use giveFameResponse<br>
         * 1: the username is incorrectly entered<br>
         * 2: users under level 15 are unable to toggle with fame.<br>
         * 3: can't raise or drop fame anymore today.<br>
         * 4: can't raise or drop fame for this character for this month anymore.<br>
         * 5: received fame, use receiveFame()<br>
         * 6: level of fame neither has been raised nor dropped due to an unexpected error*/
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);

        return mplew.getPacket();
    }

    public static byte[] receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] partyCreated(int partyid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(partyid);
        mplew.writeInt(999999999);
        mplew.writeInt(999999999);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sidekickInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(0x41);
        mplew.writeInt(from.getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] disbandSidekick(MapleSidekick s) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(0x4B);
        mplew.writeInt(s.getId());
        mplew.writeInt(s.getCharacter(0).getId());
        mplew.write(0);
        mplew.writeInt(s.getCharacter(1).getId());

        return mplew.getPacket();
    }

    public static byte[] updateSidekick(MapleCharacter first, MapleSidekick s, boolean f) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SIDEKICK_OPERATION.getValue());
        mplew.write(f ? 0x4E : 0x46);
        final MapleSidekickCharacter second = s.getCharacter(s.getCharacter(0).getId() == first.getId() ? 1 : 0);
        final boolean online = first.getMap().getCharacterById(second.getId()) != null;
        mplew.writeInt(s.getId());
        if (f) {
            mplew.writeMapleAsciiString(second.getName());
        }
        final List<String> msg = s.getSidekickMsg(online);
        mplew.writeInt(msg.size());
        for (String m : msg) {
            mplew.writeMapleAsciiString(m);
        }

        mplew.writeInt(first.getId());
        mplew.writeInt(second.getId());

        mplew.writeAsciiString(first.getName(), 13);
        mplew.writeAsciiString(second.getName(), 13);

        mplew.writeInt(first.getJob());
        mplew.writeInt(second.getJobId());

        mplew.writeInt(first.getLevel());
        mplew.writeInt(second.getLevel());

        mplew.writeInt(first.getClient().getChannel() - 1);
        mplew.writeInt(online ? (first.getClient().getChannel() - 1) : 0);

        mplew.writeLong(0);

        mplew.writeInt(first.getId());
        if (f) {
            mplew.writeInt(first.getId());
        }
        mplew.writeInt(second.getId());
        if (!f) {
            mplew.writeInt(first.getId());
        }

        mplew.writeInt(first.getMapId());
        mplew.writeInt(online ? first.getMapId() : 999999999);

        mplew.writeInt(1); //??? random bytes after 1

        mplew.write(Math.abs(first.getLevel() - second.getLevel()));
        mplew.writeInt(0); //can be 1 or 0
        mplew.writeInt(0); //time left til next buff
        mplew.writeInt(Integer.MAX_VALUE); //dunno, random
        mplew.writeInt(1); //can be 1 or 0
        //TODO FAMILIAR

        return mplew.getPacket();
    }

    public static byte[] partyInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty() == null ? 0 : from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] partyRequestInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(7);
        mplew.writeInt(from.getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob());

        return mplew.getPacket();
    }

    public static byte[] partyStatusMessage(int message, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        /*	* 10: A beginner can't create a party.
         * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
         * 13: You have yet to join a party.
         * 16: Already have joined a party.
         * 17: The party you're trying to join is already in full capacity.
         * 19: Unable to find the requested character in this channel.*/
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        if ((message == 0x15) || (message == 0x16) || (message == 0x17)) {
            mplew.writeMapleAsciiString(charname);
        }
        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter lew, boolean leaving) {
        addPartyStatus(forchannel, party, lew, leaving, false);
    }

    private static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter lew, boolean leaving, boolean exped) {
        List<MaplePartyCharacter> partymembers;
        if (party == null) {
            partymembers = new ArrayList<MaplePartyCharacter>();
        } else {
            partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        }
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(partychar.getName(), 13);
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party == null ? 0 : party.getLeader().getId());
        if (exped) {
            return;
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            } else {
                lew.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
            } else {
                lew.writeInt(leaving ? 999999999 : 0);
                lew.writeInt(leaving ? 999999999 : 0);
                lew.writeLong(leaving ? -1 : 0);
            }
        }
    }

    public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0xC);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.DISBAND ? 0 : 1);
                if (op == PartyOperation.DISBAND) {
                    mplew.writeInt(target.getId());
                } else {
                    mplew.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                }
                break;
            case JOIN:
                mplew.write(0xF);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            case CHANGE_LEADER:
            case CHANGE_LEADER_DC:
                mplew.write(0x1A);
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
                break;
            //1D = expel function not available in this map.
        }
        return mplew.getPacket();
    }

    public static byte[] partyPortal(int townId, int targetId, int skillId, Point position, boolean animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(0x23);
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);

        return mplew.getPacket();
    }

    public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);

        return mplew.getPacket();
    }

    public static byte[] multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode); //  0 buddychat; 1 partychat; 2 guildchat
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);

        return mplew.getPacket();
    }

    public static byte[] getClock(int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);

        return mplew.getPacket();
    }

    public static byte[] spawnMessage(MapleCharacter chr, int itemId, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MESSAGE.getValue());
        mplew.writeInt(1); // not sure
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(chr.getName()); // owner name
        mplew.writePos(chr.getPosition());

        return mplew.getPacket();
    }

    public static byte[] spawnMist(final MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist()); //2 = invincible, so put 1 for recovery aura
        mplew.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeRect(mist.getBox());
        mplew.writeInt(0); //dunno
        return mplew.getPacket();
    }

    public static byte[] removeMist(final int oid, final boolean eruption) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        mplew.write(eruption ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] buddylistMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
        return updateBuddylist(buddylist, 7);
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int deleted) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(deleted);
        mplew.write(buddylist.size());

        for (BuddylistEntry buddy : buddylist) {
            mplew.writeInt(buddy.getCharacterId());
            mplew.writeAsciiString(buddy.getName(), 13);
            mplew.write(buddy.isVisible() ? 0 : 1);
            mplew.writeInt(buddy.getChannel() == -1 ? -1 : (buddy.getChannel() - 1));
            mplew.writeAsciiString(buddy.getGroup(), 17);
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(nameFrom, 13);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeAsciiString("群未定", 17);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateBuddyChannel(int characterid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);

        return mplew.getPacket();
    }

    public static byte[] itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("");
        mplew.writeShort(-1);
        mplew.writeShort(-1);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);

        return mplew.getPacket();
    }

    public static byte[] showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        return mplew.getPacket();
    }

    public static byte[] spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getTruePosition());
        mplew.write(reactor.getFacingDirection()); // stance
        mplew.writeMapleAsciiString(reactor.getName());

        return mplew.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getTruePosition());
        mplew.writeInt(stance);
        return mplew.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());

        return mplew.getPacket();
    }

    public static byte[] musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static byte[] environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);

        return mplew.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);

        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, false);
    }

    public static byte[] showGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x1A);

        if (c == null || c.getMGC() == null) { //show empty guild (used for leaving, expelled)
            mplew.write(0);
            return mplew.getPacket();
        }
        MapleGuild g = World.Guild.getGuild(c.getGuildId());
        if (g == null) { //failed to read from DB - don't show a guild
            mplew.write(0);
            return mplew.getPacket();
        }
        mplew.write(1); //bInGuild
        getGuildInfo(mplew, g);

        return mplew.getPacket();
    }

    private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP()); //written twice, aftershock?
        mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
    }

    public static byte[] guildSkillPurchased(int gid, int sid, int level, long expiration, String purchase, String activate) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x55); //0x55 aftershock
        mplew.writeInt(gid);
        mplew.writeInt(sid);
        mplew.writeShort(level);
        mplew.writeLong(PacketHelper.getTime(expiration));
        mplew.writeMapleAsciiString(purchase);
        mplew.writeMapleAsciiString(activate);

        return mplew.getPacket();
    }

    public static byte[] guildLeaderChanged(int gid, int oldLeader, int newLeader, int allianceId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x59); //0x59 aftershock
        mplew.writeInt(gid);
        //01 36 00 00 00
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        mplew.write(1); //new rank lol
        mplew.writeInt(allianceId);

        return mplew.getPacket();
    }

    public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] guildContribution(int gid, int cid, int c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.writeInt(c);

        return mplew.getPacket();
    }

    public static byte[] guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);

        return mplew.getPacket();
    }

    public static byte[] denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    public static byte[] genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);

        return mplew.getPacket();
    }

    public static byte[] newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(mgc.getName(), 13);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(mgc.getAllianceRank()); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(mgc.getGuildContribution()); //should always 3

        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 0x2F : 0x2C);

        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());

        return mplew.getPacket();
    }

    public static byte[] changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());

        return mplew.getPacket();
    }

    public static byte[] guildNotice(int gid, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);

        return mplew.getPacket();
    }

    public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3C);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        return mplew.getPacket();
    }

    public static byte[] rankTitleChange(int gid, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3E);
        mplew.writeInt(gid);

        for (String r : ranks) {
            mplew.writeMapleAsciiString(r);
        }
        return mplew.getPacket();
    }

    public static byte[] guildDisband(int gid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x32);
        mplew.writeInt(gid);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x42);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);

        return mplew.getPacket();
    }

    public static byte[] guildCapacityChange(int gid, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3A);
        mplew.writeInt(gid);
        mplew.write(capacity);

        return mplew.getPacket();
    }

    public static byte[] removeGuildFromAlliance(MapleGuildAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(16);
        addAllianceInfo(mplew, alliance);
        getGuildInfo(mplew, expelledGuild);
        mplew.write(expelled ? 1 : 0); //1 = expelled, 0 = left
        return mplew.getPacket();
    }

    public static byte[] changeAlliance(MapleGuildAlliance alliance, final boolean in) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x01);
        mplew.write(in ? 1 : 0);
        mplew.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.write(noGuilds);
        for (int i = 0; i < noGuilds; i++) {
            mplew.writeInt(g[i].getId());
            //must be world
            Collection<MapleGuildCharacter> members = g[i].getMembers();
            mplew.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                mplew.writeInt(mgc.getId());
                mplew.write(in ? mgc.getAllianceRank() : 0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x02);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        return mplew.getPacket();
    }

    public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(25);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        return mplew.getPacket();
    }

    public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x03);
        mplew.writeInt(inviter.getGuildId());
        mplew.writeMapleAsciiString(inviter.getName());
        //alliance invite did NOT change
        mplew.writeMapleAsciiString(allianceName);
        return mplew.getPacket();
    }

    public static byte[] changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, final boolean add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x04);
        mplew.writeInt(add ? alliance.getId() : 0);
        mplew.writeInt(guild.getId());
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.writeInt(members.size());
        for (MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
            mplew.write(add ? mgc.getAllianceRank() : 0);
        }
        return mplew.getPacket();
    }

    public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(allianceid);
        mplew.writeInt(player.getId());
        mplew.writeInt(player.getAllianceRank());
        return mplew.getPacket();
    }

    public static byte[] createGuildAlliance(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(15);
        addAllianceInfo(mplew, alliance);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        for (MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        return mplew.getPacket();
    }

    public static byte[] getAllianceInfo(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(12);
        mplew.write(alliance == null ? 0 : 1); //in an alliance
        if (alliance != null) {
            addAllianceInfo(mplew, alliance);
        }
        return mplew.getPacket();
    }

    public static byte[] getAllianceUpdate(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(23);
        addAllianceInfo(mplew, alliance);
        return mplew.getPacket();
    }

    public static byte[] getGuildAlliance(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(13);
        if (alliance == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.writeInt(noGuilds);
        for (MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        return mplew.getPacket();
    }

    public static byte[] addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(18);
        addAllianceInfo(mplew, alliance);
        mplew.writeInt(newGuild.getId()); //???
        getGuildInfo(mplew, newGuild);
        mplew.write(0); //???
        return mplew.getPacket();
    }

    private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleGuildAlliance alliance) {
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRank(i));
        }
        mplew.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            mplew.writeInt(alliance.getGuildId(i));
        }
        mplew.writeInt(alliance.getCapacity()); // ????
        mplew.writeMapleAsciiString(alliance.getNotice());
    }

    public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(14);
        mplew.writeInt(alliance);
        mplew.writeInt(gid);
        mplew.writeInt(id);
        mplew.write(online ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(24);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        return mplew.getPacket();
    }

    public static byte[] updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(27);
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getAllianceRank());

        return mplew.getPacket();
    }

    public static byte[] disbandAlliance(int alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(29);
        mplew.writeInt(alliance);

        return mplew.getPacket();
    }

    public static byte[] BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(6);

        if (bbs == null) {
            mplew.write(0);
            mplew.writeLong(0);
            return mplew.getPacket();
        }
        int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (MapleBBSThread b : bbs) {
            if (b.isNotice()) { //notice
                notice = b;
                break;
            }
        }
        mplew.write(notice == null ? 0 : 1);
        if (notice != null) { //has a notice
            addThread(mplew, notice);
        }
        if (threadCount < start) { //seek to the thread before where we start
            //uh, we're trying to start at a place past possible
            start = 0;
        }
        //each page has 10 threads, start = page # in packet but not here
        mplew.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);

        for (int i = 0; i < pages; i++) {
            addThread(mplew, bbs.get(start + i)); //because 0 = notice
        }
        return mplew.getPacket();
    }

    private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }

    public static byte[] showThread(MapleBBSThread thread) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(7);

        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        return mplew.getPacket();
    }

    public static byte[] showGuildRanks(int npcid, List<GuildRankingInfo> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        //this error 38s and official servers have it removed
        mplew.writeInt(all.size());

        for (GuildRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getGP());
            mplew.writeInt(info.getLogo());
            mplew.writeInt(info.getLogoColor());
            mplew.writeInt(info.getLogoBg());
            mplew.writeInt(info.getLogoBgColor());
        }

        return mplew.getPacket();
    }

    public static byte[] updateGP(int gid, int GP, int glevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP); //2nd int = guild level or something

        return mplew.getPacket();
    }

    public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, byte flags, byte speed, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);

        return mplew.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] showMagnet(int mobid, byte success) { // Monster Magnet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);

        return mplew.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int height) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0x00);
        mplew.writeInt(messengerid);
        mplew.write(0x00);

        return mplew.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);

        return mplew.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);

        return mplew.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] messengerChat(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithCS(String target, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMTS(String target, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);
        return mplew.getPacket();
    }

    public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);

        return mplew.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count); // number of macros
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateAriantPQRanking(String name, int score, boolean empty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARIANT_PQ_START.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }

    public static byte[] catchMonster(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);

        return mplew.getPacket();
    }

    public static byte[] catchMob(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CATCH_MOB.getValue());
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.writeInt(mobid);

        return mplew.getPacket();
    }

    public static byte[] showAriantScoreBoard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

        return mplew.getPacket();
    }

    public static byte[] boatPacket(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had 3: boat leaves

        return mplew.getPacket();
    }

    public static byte[] boatEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(SendPacketOpcode.BOAT_EFF.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had the other ones o.o

        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(0x18);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);

        return mplew.getPacket();
    }

    public static byte[] Mulung_DojoUp2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x7); //AFTERSHOCK: 10? MAYBE

        return mplew.getPacket();
    }

    public static byte[] showQuestMsg(final String msg) {
        return serverNotice(5, msg);
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg("You have received " + recv + " training points, for the accumulated total of " + total + " training points.");
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }

    public static byte[] leftKnockBack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
        return mplew.getPacket();
    }

    public static byte[] rollSnowball(int type, MapleSnowballs ball1, MapleSnowballs ball2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
        mplew.write(type); // 0 = normal, 1 = rolls from start to end, 2 = down disappear, 3 = up disappear, 4 = move
        mplew.writeInt(ball1 == null ? 0 : (ball1.getSnowmanHP() / 75));
        mplew.writeInt(ball2 == null ? 0 : (ball2.getSnowmanHP() / 75));
        mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);
        return mplew.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

    public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);
        return mplew.getPacket();
    }

    public static byte[] snowballMessage(int team, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);
        return mplew.getPacket();
    }

    public static byte[] finishedSort(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(1);
        mplew.write(type);
        return mplew.getPacket();
    }

    // 00 01 00 00 00 00
    public static byte[] coconutScore(int[] coconutscore) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);
        return mplew.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        // FF 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(0x80);
        } else {
            mplew.writeInt(id);
            mplew.write(type); // What action to do for the coconut.
        }
        return mplew.getPacket();
    }

    public static byte[] finishedGather(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
        mplew.write(1);
        mplew.write(type);
        return mplew.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
        mplew.write(-1); //could be something like mob displaying message.
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2, int ourItem) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PIGMI_REWARD.getValue());
        mplew.writeInt(itemId);
        mplew.writeShort(quantity);
        mplew.writeInt(ourItem);
        mplew.writeInt(itemId2);
        mplew.writeInt(quantity2);

        return mplew.getPacket();
    }

    public static byte[] sendLevelup(boolean family, int level, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendJobup(boolean family, int jobid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid); //or is this a short
        mplew.writeMapleAsciiString((GameConstants.GMS && !family ? "> " : "") + name);

        return mplew.getPacket();
    }

    public static byte[] showHorntailShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] showChaosZakumShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] stopClock() {
        return getPacketFromHexString(Integer.toHexString(SendPacketOpcode.STOP_CLOCK.getValue()) + " 00"); //does the header not work?
    }

    public static byte[] spawnDragon(MapleDragon d) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(d.getPosition().x);
        mplew.writeInt(d.getPosition().y);
        mplew.write(d.getStance()); //stance?
        mplew.writeShort(0);
        mplew.writeShort(d.getJobId());
        return mplew.getPacket();
    }

    public static byte[] removeDragon(int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
        mplew.writeInt(chrid);
        return mplew.getPacket();
    }

    public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue()); //not sure
        mplew.writeInt(d.getOwner());
        mplew.writePos(startPos);
        mplew.writeInt(0);

        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static final byte[] temporaryStats_Aran() {
        final Map<MapleStat.Temp, Integer> stats = new EnumMap<MapleStat.Temp, Integer>(MapleStat.Temp.class);
        stats.put(MapleStat.Temp.STR, 999);
        stats.put(MapleStat.Temp.DEX, 999);
        stats.put(MapleStat.Temp.INT, 999);
        stats.put(MapleStat.Temp.LUK, 999);
        stats.put(MapleStat.Temp.WATK, 255);
        stats.put(MapleStat.Temp.ACC, 999);
        stats.put(MapleStat.Temp.AVOID, 999);
        stats.put(MapleStat.Temp.SPEED, 140);
        stats.put(MapleStat.Temp.JUMP, 120);
        return temporaryStats(stats);
    }

    public static final byte[] temporaryStats_Balrog(final MapleCharacter chr) {
        final Map<MapleStat.Temp, Integer> stats = new EnumMap<MapleStat.Temp, Integer>(MapleStat.Temp.class);
        int offset = 1 + (chr.getLevel() - 90) / 20;
        //every 20 levels above 90, +1
        stats.put(MapleStat.Temp.STR, chr.getStat().getTotalStr() / offset);
        stats.put(MapleStat.Temp.DEX, chr.getStat().getTotalDex() / offset);
        stats.put(MapleStat.Temp.INT, chr.getStat().getTotalInt() / offset);
        stats.put(MapleStat.Temp.LUK, chr.getStat().getTotalLuk() / offset);
        stats.put(MapleStat.Temp.WATK, chr.getStat().getTotalWatk() / offset);
        stats.put(MapleStat.Temp.MATK, chr.getStat().getTotalMagic() / offset);
        return temporaryStats(stats);
    }

    public static final byte[] temporaryStats(final Map<MapleStat.Temp, Integer> mystats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        //str 0x1, dex 0x2, int 0x4, luk 0x8
        //level 0x10 = 255
        //0x100 = 999
        //0x200 = 999
        //0x400 = 120
        //0x800 = 140
        int updateMask = 0;
        for (MapleStat.Temp statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        Integer value;

        for (final Entry<MapleStat.Temp, Integer> statupdate : mystats.entrySet()) {
            value = statupdate.getKey().getValue();

            if (value >= 1) {
                if (value <= 0x200) { //level 0x10 - is this really short or some other? (FF 00)
                    mplew.writeShort(statupdate.getValue().shortValue());
                } else {
                    mplew.write(statupdate.getValue().byteValue());
                }
            }
        }
        return mplew.getPacket();
    }

    public static final byte[] temporaryStats_Reset() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        return mplew.getPacket();
    }

    //its likely that durability items use this
    public static final byte[] showHpHealed(final int cid, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(0x6); //bb +2
        mplew.writeInt(amount);
        return mplew.getPacket();
    }

    public static final byte[] showOwnHpHealed(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x06);  //bb +2
        mplew.writeInt(amount);
        return mplew.getPacket();
    }

    public static final byte[] sendRepairWindow(int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REPAIR_WINDOW.getValue());
        mplew.writeInt(0x21); //sending 0x20 here opens evan skill window o.o
        mplew.writeInt(npc);
        return mplew.getPacket();
    }

    public static final byte[] sendProfessionWindow(int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REPAIR_WINDOW.getValue());
        mplew.writeInt(0x2E); //sending 0x20 here opens evan skill window o.o
        mplew.writeInt(npc);
        return mplew.getPacket();
    }

    public static final byte[] sendPVPWindow() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_UI.getValue());
        mplew.writeInt(0x32);

        return mplew.getPacket();
    }

    public static final byte[] sendPVPMaps() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PVP_INFO.getValue());
        mplew.write(1); //max amount of players
        for (int i = 0; i < 17; i++) {
            mplew.writeInt(10); //how many peoples in each map
        }
        mplew.writeZeroBytes(124);
        mplew.writeShort(150); ////PVP 1.5 EVENT!
        mplew.write(0);
        return mplew.getPacket();
    }

    public static final byte[] sendPyramidUpdate(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount); //1-132 ?
        return mplew.getPacket();
    }

    public static final byte[] sendPyramidResult(final byte rank, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount); //1-132 ?
        return mplew.getPacket();
    }

    //show_status_info - 01 53 1E 01
    //10/08/14/19/11
    //update_quest_info - 08 53 1E 00 00 00 00 00 00 00 00
    //show_status_info - 01 51 1E 01 01 00 30
    //update_quest_info - 08 51 1E 00 00 00 00 00 00 00 00
    public static final byte[] sendPyramidEnergy(final String type, final String amount) {
        return sendString(1, type, amount);
    }

    public static final byte[] sendString(final int type, final String object, final String amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        switch (type) {
            case 1:
                mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
                break;
            case 2:
                mplew.writeShort(SendPacketOpcode.GHOST_POINT.getValue());
                break;
            case 3:
                mplew.writeShort(SendPacketOpcode.GHOST_STATUS.getValue());
                break;
        }
        mplew.writeMapleAsciiString(object); //massacre_hit, massacre_cool, massacre_miss, massacre_party, massacre_laststage, massacre_skill
        mplew.writeMapleAsciiString(amount);
        return mplew.getPacket();
    }

    public static final byte[] sendGhostPoint(final String type, final String amount) {
        return sendString(2, type, amount); //PRaid_Point (0-1500???)
    }

    public static final byte[] sendGhostStatus(final String type, final String amount) {
        return sendString(3, type, amount); //Red_Stage(1-5), Blue_Stage, blueTeamDamage, redTeamDamage
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static byte[] getEvanTutorial(String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());

        mplew.write(8);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] showEventInstructions() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getOwlOpen() { //best items! hardcoded
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(7);
        mplew.write(GameConstants.owlItems.length);
        for (int i : GameConstants.owlItems) {
            mplew.writeInt(i);
        } //these are the most searched items. too lazy to actually make
        return mplew.getPacket();
    }

    public static byte[] getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(8);
        mplew.writeInt(0);
        mplew.writeInt(itemSearch);
        int size = 0;

        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (HiredMerchant hm : hms) {
            final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.item.getQuantity()); //I THINK.
                mplew.writeInt(item.bundles); //I THINK.
                mplew.writeInt(item.price);
                switch (InventoryHandler.OWL_ID) {
                    case 0:
                        mplew.writeInt(hm.getOwnerId()); //store ID
                        break;
                    case 1:
                        mplew.writeInt(hm.getStoreId());
                        break;
                    default:
                        mplew.writeInt(hm.getObjectId());
                        break;
                }
                mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
                mplew.write(GameConstants.getInventoryType(itemSearch).getType()); //position?
                if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, item.item, true, true);
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6: { //not enough mesos
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                }
                break;
            }
            case 8: { //open (npc)
                mplew.writeInt(9000019);
                break;
            }
            case 11: { //selection vs answer
                mplew.write(selection);
                mplew.write(answer); // FF = lose, or if selection = answer then lose ???
                break;
            }
        }
        return mplew.getPacket();
    }

    public static final byte[] getSlotUpdate(byte invType, byte newSlots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_INVENTORY_SLOT.getValue());
        mplew.write(invType);
        mplew.write(newSlots);
        return mplew.getPacket();
    }

    public static byte[] followRequest(int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);
        return mplew.getPacket();
    }

    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        if (replier == 0) { //cancel
            mplew.write(toMap == null ? 0 : 1); //1 -> x (int) y (int) to change map
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getFollowMsg(int opcode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
        mplew.writeLong(opcode); //5 = canceled request.
        return mplew.getPacket();
    }

    public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
        mplew.writePos(otherStart);
        mplew.writePos(myStart);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.write(0x11); //what? could relate to movePlayer
        for (int i = 0; i < 8; i++) {
            mplew.write(0); //?? sometimes 0x44 sometimes 0x88 sometimes 0x4.. etc.. buffstat or what
        }
        mplew.write(0); //?
        mplew.writePos(otherEnd);
        mplew.writePos(otherStart);

        return mplew.getPacket();
    }

    public static final byte[] getFollowMessage(final String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(0x0B); //?
        mplew.writeMapleAsciiString(msg); //white in gms, but msea just makes it pink.. waste
        return mplew.getPacket();
    }

    public static final byte[] getNodeProperties(final MapleMonster objectid, final MapleMap map) {
        //idk.
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
        mplew.writeInt(objectid.getObjectId()); //?
        mplew.writeInt(map.getNodes().size());
        mplew.writeInt(objectid.getPosition().x);
        mplew.writeInt(objectid.getPosition().y);
        for (MapleNodeInfo mni : map.getNodes()) {
            mplew.writeInt(mni.x);
            mplew.writeInt(mni.y);
            mplew.writeInt(mni.attr);
            if (mni.attr == 2) { //msg
                mplew.writeInt(500); //? talkMonster
            }
        }
        mplew.writeZeroBytes(6);
        objectid.setNodePacket(mplew.getPacket());
        return objectid.getNodePacket();
    }

    public static final byte[] getMovingPlatforms(final MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                mplew.writeInt(mp.SN.get(x));
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);//?
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        return mplew.getPacket();
    }

    public static final byte[] getUpdateEnvironment(final MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString(mp.getKey());
            mplew.writeInt(mp.getValue());
        }
        return mplew.getPacket();
    }

    public static byte[] sendEngagementRequest(String name, int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        mplew.write(0); //mode, 0 = engage, 1 = cancel, 2 = answer.. etc
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(cid); // playerid
        return mplew.getPacket();
    }

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static byte[] trembleEffect(int type, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        return mplew.getPacket();
    }

    public static byte[] sendEngagement(final byte msg, final int item, final MapleCharacter male, final MapleCharacter female) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // 0B = Engagement has been concluded.
        // 0D = The engagement is cancelled.
        // 0E = The divorce is concluded.
        // 10 = The marriage reservation has been successsfully made.
        // 12 = Wrong character name
        // 13 = The party in not in the same map.
        // 14 = Your inventory is full. Please empty your E.T.C window.
        // 15 = The person's inventory is full.
        // 16 = The person cannot be of the same gender.
        // 17 = You are already engaged.
        // 18 = The person is already engaged.
        // 19 = You are already married.
        // 1A = The person is already married.
        // 1B = You are not allowed to propose.
        // 1C = The person is not allowed to be proposed to.
        // 1D = Unfortunately, the one who proposed to you has cancelled his proprosal.
        // 1E = The person had declined the proposal with thanks.
        // 1F = The reservation has been cancelled. Try again later.
        // 20 = You cannot cancel the wedding after reservation.
        // 22 = The invitation card is ineffective.
        mplew.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
        mplew.write(msg); // 1103 custom quest
        switch (msg) {
            case 11:
            case 12: {
                mplew.writeInt(0); // ringid or uniqueid
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(1); //always
                mplew.writeInt(item);
                mplew.writeInt(item); // wtf?repeat?
                mplew.writeAsciiString(male.getName(), 13);
                mplew.writeAsciiString(female.getName(), 13);
                break;
            }
            case 15: { // i guess this is useless
                mplew.writeAsciiString(male.getName(), 13);
                mplew.writeAsciiString(female.getName(), 13);
                mplew.writeShort(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getPartyListing(final PartySearchType pst) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x93 : 0x4D);
        mplew.writeInt(pst.id);
        final List<PartySearch> parties = World.Party.searchParty(pst);
        mplew.writeInt(parties.size());
        for (PartySearch party : parties) {
            mplew.writeInt(0); //ive no clue,either E8 72 94 00 or D8 72 94 00 
            mplew.writeInt(2); //again, no clue, seems to remain constant?
            if (pst.exped) {
                MapleExpedition me = World.Party.getExped(party.getId());
                mplew.writeInt(me.getType().maxMembers);
                mplew.writeInt(party.getId());
                mplew.writeAsciiString(party.getName(), 48);
                for (int i = 0; i < 5; i++) { //all parties in the exped other than the leader
                    if (i < me.getParties().size()) {
                        MapleParty part = World.Party.getParty(me.getParties().get(i));
                        if (part != null) {
                            addPartyStatus(-1, part, mplew, false, true);
                        } else {
                            mplew.writeZeroBytes(202); //length of the addPartyStatus.
                        }
                    } else {
                        mplew.writeZeroBytes(202); //length of the addPartyStatus.
                    }
                }
            } else {
                mplew.writeInt(0);
                mplew.writeInt(party.getId());
                mplew.writeAsciiString(party.getName(), 48);
                addPartyStatus(-1, World.Party.getParty(party.getId()), mplew, false, true); //if exped, send 0, if not then skip
            }

            mplew.writeShort(0); //wonder if this goes here or at bottom
        }

        return mplew.getPacket();
    }

    public static byte[] partyListingAdded(final PartySearch ps) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x91 : 0x4B);
        mplew.writeInt(ps.getType().id);
        mplew.writeInt(0); //ive no clue,either 48 DB 60 00 or 18 DB 60 00
        mplew.writeInt(1);
        if (ps.getType().exped) {
            MapleExpedition me = World.Party.getExped(ps.getId());
            mplew.writeInt(me.getType().maxMembers);
            mplew.writeInt(ps.getId());
            mplew.writeAsciiString(ps.getName(), 48);
            for (int i = 0; i < 5; i++) { //all parties in the exped other than the leader
                if (i < me.getParties().size()) {
                    MapleParty party = World.Party.getParty(me.getParties().get(i));
                    if (party != null) {
                        addPartyStatus(-1, party, mplew, false, true);
                    } else {
                        mplew.writeZeroBytes(202); //length of the addPartyStatus.
                    }
                } else {
                    mplew.writeZeroBytes(202); //length of the addPartyStatus.
                }
            }
        } else {
            mplew.writeInt(0); //doesn't matter
            mplew.writeInt(ps.getId());
            mplew.writeAsciiString(ps.getName(), 48);
            addPartyStatus(-1, World.Party.getParty(ps.getId()), mplew, false, true); //if exped, send 0, if not then skip
        }
        mplew.writeShort(0); //wonder if this goes here or at bottom

        return mplew.getPacket();
    }

    public static byte[] expeditionStatus(final MapleExpedition me, boolean created) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(created ? (GameConstants.GMS ? 0x81 : 0x49) : (GameConstants.GMS ? 0x7F : 0x47));
        mplew.writeInt(me.getType().exped);
        mplew.writeInt(0); //eh?
        for (int i = 0; i < 5; i++) { //all parties in the exped other than the leader
            if (i < me.getParties().size()) {
                MapleParty party = World.Party.getParty(me.getParties().get(i));
                if (party != null) {
                    addPartyStatus(-1, party, mplew, false, true);
                } else {
                    mplew.writeZeroBytes(202); //length of the addPartyStatus.
                }
            } else {
                mplew.writeZeroBytes(202); //length of the addPartyStatus.
            }
        }
        mplew.writeShort(0); //wonder if this goes here or at bottom

        return mplew.getPacket();
    }

    public static byte[] expeditionError(final int errcode, final String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x8F : 0x57);
        mplew.writeInt(errcode); //0 = not found, 1 = admin, 2 = already in a part, 3 = not right lvl, 4 = blocked, 5 = taking another, 6 = already in, 7 = all good
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] expeditionJoined(final String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x7C : 0x4A);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] expeditionLeft(final String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x82 : 0x4E);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] expeditionMessage(final int code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(code + (GameConstants.GMS ? 76 : 0)); //0x3B = left, 0x3E = disbanded

        return mplew.getPacket();
    }

    public static byte[] expeditionLeaderChanged(final int newLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x85 : 0x53);
        mplew.writeInt(newLeader);
        return mplew.getPacket();
    }

    //can only update one party in the expedition.
    public static byte[] expeditionUpdate(final int partyIndex, final MapleParty party) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x86 : 0x54);
        mplew.writeInt(0); //lol?
        mplew.writeInt(partyIndex);
        if (party == null) {
            mplew.writeZeroBytes(178); //length of the addPartyStatus.
        } else {
            addPartyStatus(-1, party, mplew, false, true);
        }
        return mplew.getPacket();
    }

    public static byte[] expeditionInvite(MapleCharacter from, int exped) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        mplew.write(GameConstants.GMS ? 0x88 : 0x56);
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(exped);

        return mplew.getPacket();
    }

    public static byte[] updateJaguar(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_JAGUAR.getValue());
        PacketHelper.addJaguarInfo(mplew, from);

        return mplew.getPacket();
    }

    public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TESLA_TRIANGLE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(sum1);
        mplew.writeInt(sum2);
        mplew.writeInt(sum3);
        return mplew.getPacket();
    }

    public static byte[] mechPortal(Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_PORTAL.getValue());
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.writePos(md.getTruePosition());
        mplew.write(md.getId());
        mplew.writeInt(md.getPartyId());
        return mplew.getPacket();
    }

    public static byte[] removeMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.write(md.getId());
        return mplew.getPacket();
    }

    public static byte[] useSPReset(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SP_RESET.getValue());
        mplew.write(1);
        mplew.writeInt(cid);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] playerDamaged(int cid, int dmg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_DAMAGED.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(dmg);

        return mplew.getPacket();
    }

    public static byte[] pamsSongEffect(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PAMS_SONG.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] pamsSongUI() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PAMS_SONG.getValue());
        mplew.writeShort(0); //doesn't seem to change it

        return mplew.getPacket();
    }

    public static byte[] englishQuizMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ENGLISH_QUIZ.getValue());
        mplew.writeInt(20); //?
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] report(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REPORT.getValue());
        mplew.write(err); //0 = success
        if (GameConstants.GMS && err == 2) {
            mplew.write(0);
            mplew.writeInt(1);
        }
        return mplew.getPacket();
    }

    public static byte[] ultimateExplorer() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ULTIMATE_EXPLORER.getValue());

        return mplew.getPacket();
    }

    public static byte[] GMPoliceMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GM_POLICE.getValue());
        mplew.write(0); // no clue
        mplew.writeInt(0); //type
        return mplew.getPacket();
    }

    public static byte[] pamSongUI() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PAM_SONG.getValue());

        return mplew.getPacket();
    }

    public static byte[] dragonBlink(int portalId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_BLINK.getValue());
        mplew.write(portalId);
        return mplew.getPacket();
    }

    public static byte[] harvestMessage(int oid, int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.HARVEST_MESSAGE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(msg);
        return mplew.getPacket();
    }

    public static byte[] showHarvesting(int cid, int tool) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_HARVEST.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.writeInt(tool);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] harvestResult(int cid, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.HARVESTED.getValue());
        mplew.writeInt(cid);
        mplew.write(success ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
        mplew.writeInt(cid);
        mplew.writeMapleAsciiString(cname);
        mplew.writeInt(pos.x);
        mplew.writeInt(pos.y);
        mplew.writeShort(timeLeft); //fh or time left, dunno
        mplew.writeInt(itemId); //3049000, 3049001...
        mplew.writeInt(fee);
        return mplew.getPacket();
    }

    public static byte[] removeExtractor(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(1); //probably 1 = animation, 2 = make something?
        return mplew.getPacket();
    }

    public static byte[] spouseMessage(String msg, boolean white) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPOUSE_MESSAGE.getValue());
        mplew.writeShort(white ? (GameConstants.GMS ? 11 : 10) : 6); //12 = the blue message thing, 7/8 = yellow
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] openBag(int index, int itemId, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
        mplew.writeInt(index);
        mplew.writeInt(itemId);
        mplew.writeShort(firstTime ? 1 : 0); //this might actually be 2 bytes
        return mplew.getPacket();
    }

    public static byte[] showOwnCraftingEffect(String effect, int time, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x20);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1); // hmm
        mplew.writeInt(time);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] showCraftingEffect(int cid, String effect, int time, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(0x20);
        mplew.writeMapleAsciiString(effect);
        mplew.write(1); // hmm
        mplew.writeInt(time);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] craftMake(int cid, int something, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CRAFT_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(something);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] craftFinished(int cid, int craftID, int ranking, int itemId, int quantity, int exp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(craftID);
        mplew.writeInt(ranking);
        mplew.writeInt(itemId);
        mplew.writeInt(quantity);
        mplew.writeInt(exp);
        return mplew.getPacket();
    }

    public static byte[] shopDiscount(int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOP_DISCOUNT.getValue());
        mplew.write(percent);
        return mplew.getPacket();
    }

    public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_SPAWN.getValue());
        mplew.writeInt(cid.getId());
        mplew.write(android.getType()); //type of android, 1-5
        mplew.writePos(android.getPos());
        mplew.write(android.getStance());
        mplew.writeShort(0); //no clue, FH or something
        mplew.writeShort(0);
        mplew.writeShort(android.getSkin());
        mplew.writeShort(android.getHair() - 30000);
        mplew.writeShort(android.getFace() - 20000);
        mplew.writeMapleAsciiString(android.getName());

        for (short i = -1200; i > -1207; i--) {
            final Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
            mplew.writeInt(item != null ? item.getItemId() : 0);
        }
        return mplew.getPacket();
    }

    public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_MOVE.getValue());
        mplew.writeInt(cid);
        mplew.writePos(pos);
        mplew.writeInt(Integer.MAX_VALUE); //time left in milliseconds? this appears to go down...slowly 1377440900
        PacketHelper.serializeMovementList(mplew, res);
        return mplew.getPacket();
    }

    public static byte[] showAndroidEmotion(int cid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_EMOTION.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(animation); //1234567 = default smiles, 8 = throwing up, 11 = kiss, 14 = googly eyes, 17 = wink...
        return mplew.getPacket();
    }

    public static byte[] updateAndroidLook(int cid, int flag, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_UPDATE.getValue());
        mplew.writeInt(cid);
        mplew.write(flag);
        mplew.writeInt(itemId); // cash item
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] deactivateAndroid(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_DEACTIVATED.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    public static byte[] removeAndroidHeart() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x15); // 0x16 for resting -fatigue
        return mplew.getPacket();
    }

    public static byte[] changeCardSet(int set) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CARD_SET.getValue());
        mplew.writeInt(set);
        return mplew.getPacket();
    }

    public static byte[] getCard(boolean full, int cardid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GET_CARD.getValue());
        mplew.write(full ? 0 : 1);
        if (!full) {
            mplew.writeInt(cardid);
            mplew.writeInt(level);
        }

        return mplew.getPacket();
    }

    public static byte[] upgradeBook(Item book, MapleCharacter chr) { //slot -55
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOK_STATS.getValue());
        mplew.writeInt(book.getPosition()); //negative or not
        PacketHelper.addItemInfo(mplew, book, true, true, false, false, chr);
        return mplew.getPacket();
    }

    public static byte[] pendantSlot(boolean p) { //slot -59
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PENDANT_SLOT.getValue());
        mplew.write(p ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] getMonsterBookInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOK_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getLevel());
        //chr.getMonsterBook().writeCharInfoPacket(mplew);
        return mplew.getPacket();
    }

    public static byte[] getBuffBar(long millis) { //You can use the buff again _ seconds later. + bar above head
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUFF_BAR.getValue());
        mplew.writeLong(millis);
        return mplew.getPacket();
    }

    /**
     * Makes any NPC in the game scriptable.
     *
     * @param npcId - The NPC's ID, found in WZ files/MCDB
     * @param description - If the NPC has quests, this will be the text of the
     * menu item
     * @return
     */
    public static byte[] setNPCScriptable(List<Pair<Integer, String>> npcs) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
        mplew.write(npcs.size());
        for (Pair<Integer, String> s : npcs) {
            mplew.writeInt(s.left);
            mplew.writeMapleAsciiString(s.right);
            mplew.writeInt(0); // start time
            mplew.writeInt(Integer.MAX_VALUE); // end time
        }
        return mplew.getPacket();
    }

    public static byte[] showMidMsg(String s, int l) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(l); //i think this is the line.. or soemthing like that. 1 = lower than 0
        mplew.writeMapleAsciiString(s);
        mplew.write(s.length() > 0 ? 0 : 1); //remove?
        return mplew.getPacket();
    }

    public static byte[] showMemberSearch(List<MapleCharacter> chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MEMBER_SEARCH.getValue());
        mplew.write(chr.size());
        for (MapleCharacter c : chr) {
            mplew.writeInt(c.getId());
            mplew.writeMapleAsciiString(c.getName());
            mplew.writeShort(c.getJob());
            mplew.write(c.getLevel());
        }
        return mplew.getPacket();
    }

    public static byte[] showPartySearch(List<MapleParty> chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_SEARCH.getValue());
        mplew.write(chr.size());
        for (MapleParty c : chr) {
            mplew.writeInt(c.getId());
            mplew.writeMapleAsciiString(c.getLeader().getName());
            mplew.write(c.getLeader().getLevel());
            mplew.write(c.getLeader().isOnline() ? 1 : 0);
            mplew.write(c.getMembers().size());
            for (MaplePartyCharacter ch : c.getMembers()) {
                mplew.writeInt(ch.getId());
                mplew.writeMapleAsciiString(ch.getName());
                mplew.writeShort(ch.getJobId());
                mplew.write(ch.getLevel());
                mplew.write(ch.isOnline() ? 1 : 0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] showBackgroundEffect(String eff, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VISITOR.getValue());
        mplew.writeMapleAsciiString(eff); //"Visitor"
        mplew.write(value);
        return mplew.getPacket();
    }

    public static byte[] loadGuildName(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
        mplew.writeInt(chr.getId());

        if (chr.getGuildId() <= 0) {
            mplew.writeShort(0);
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
            } else {
                mplew.writeShort(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] loadGuildIcon(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
        mplew.writeInt(chr.getId());

        if (chr.getGuildId() <= 0) {
            mplew.writeZeroBytes(6);
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeZeroBytes(6);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_GENDER.getValue());
        mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static byte[] registerFamiliar(MonsterFamiliar mf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REGISTER_FAMILIAR.getValue());
        mplew.writeLong(mf.getId());
        mf.writeRegisterPacket(mplew, false);
        mplew.writeShort(mf.getVitality() >= 3 ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] touchFamiliar(int cid, byte unk, int objectid, int type, int delay, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.TOUCH_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(unk);
        mplew.writeInt(objectid);
        mplew.writeInt(type);
        mplew.writeInt(delay);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }

    public static byte[] familiarAttack(int cid, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ATTACK_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(unk);
        mplew.write(attackPair.size());
        for (Triple<Integer, Integer, List<Integer>> s : attackPair) {
            mplew.writeInt(s.left);
            mplew.write(s.mid);
            mplew.write(s.right.size());
            for (int damage : s.right) {
                mplew.writeInt(damage);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateFamiliar(MonsterFamiliar mf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.writeInt(mf.getFamiliar());
        mplew.writeInt(mf.getFatigue());
        mplew.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2));
        return mplew.getPacket();
    }

    public static byte[] removeFamiliar(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.writeShort(spawn ? 1 : 0);
        mplew.write(0);
        if (spawn) {
            mplew.writeInt(mf.getFamiliar());
            mplew.writeInt(mf.getFatigue());
            mplew.writeInt(mf.getVitality() * 300); //max fatigue
            mplew.writeMapleAsciiString(mf.getName());
            mplew.writePos(mf.getTruePosition());
            mplew.write(mf.getStance());
            mplew.writeShort(mf.getFh());
        }
        return mplew.getPacket();
    }

    public static byte[] moveFamiliar(int cid, Point startPos, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_FAMILIAR.getValue()); //not sure
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);

        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] achievementRatio(int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue()); //not sure
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] createUltimate(int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CREATE_ULTIMATE.getValue());
        mplew.writeInt(amount); //2 = no slots, 1 = success, 0 = failed

        return mplew.getPacket();
    }

    public static byte[] professionInfo(String skil, int level1, int level2, int chance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PROFESSION_INFO.getValue());
        mplew.writeMapleAsciiString(skil);
        mplew.writeInt(level1);
        mplew.writeInt(level2);
        mplew.write(1);
        mplew.writeInt(skil.startsWith("9200") || skil.startsWith("9201") ? 100 : chance); //100% chance

        return mplew.getPacket();
    }

    public static byte[] quickSlot(String qs) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
        mplew.write((qs == null || qs.equals("")) ? 0 : 1);
        if (qs != null && !qs.equals("")) {
            final String[] slots = qs.split(",");
            for (int i = 0; i < 8; i++) {
                mplew.writeInt(Integer.parseInt(slots[i]));
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getFamiliarInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAMILIAR_INFO.getValue());
        mplew.writeInt(chr.getFamiliars().size()); //size
        for (MonsterFamiliar mf : chr.getFamiliars().values()) {
            mf.writeRegisterPacket(mplew, true);
        }
        List<Pair<Integer, Long>> size = new ArrayList<Pair<Integer, Long>>();
        for (Item i : chr.getInventory(MapleInventoryType.USE).list()) {
            if (i.getItemId() / 10000 == 287) { //expensif
                StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(i.getItemId());
                if (f != null) {
                    size.add(new Pair<Integer, Long>(f.familiar, i.getInventoryId()));
                }
            }
        }
        mplew.writeInt(size.size());
        for (Pair<Integer, Long> s : size) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(s.left);
            mplew.writeLong(s.right);
            mplew.write(0); //activated or not, troll
        }
        size.clear();
        return mplew.getPacket();
    }

    public static byte[] updateImp(MapleImp imp, int mask, int index, boolean login) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ITEM_POT.getValue());
        mplew.write(login ? 0 : 1); //0 = unchanged, 1 = changed
        mplew.writeInt(index + 1);
        mplew.writeInt(mask);
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0) {
            final Pair<Integer, Integer> i = MapleItemInformationProvider.getInstance().getPot(imp.getItemId());
            if (i == null) {
                return enableActions();
            }
            mplew.writeInt(i.left);
            mplew.write(imp.getLevel()); //probably type
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.STATE.getValue()) != 0) {
            mplew.write(imp.getState());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.FULLNESS.getValue()) != 0) {
            mplew.writeInt(imp.getFullness());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CLOSENESS.getValue()) != 0) {
            mplew.writeInt(imp.getCloseness());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CLOSENESS_LEFT.getValue()) != 0) {
            mplew.writeInt(1); //how much closeness is available to get right now
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MINUTES_LEFT.getValue()) != 0) {
            mplew.writeInt(0); //how much mins till next closeness
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.LEVEL.getValue()) != 0) {
            mplew.write(1); //k idk
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.FULLNESS_2.getValue()) != 0) {
            mplew.writeInt(imp.getFullness()); //idk
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.UPDATE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CREATE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.AWAKE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.SLEEP_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_CLOSENESS.getValue()) != 0) {
            mplew.writeInt(100); //max closeness available to be gotten
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_DELAY.getValue()) != 0) {
            mplew.writeInt(1000); //idk, 1260?
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_FULLNESS.getValue()) != 0) {
            mplew.writeInt(1000);
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_ALIVE.getValue()) != 0) {
            mplew.writeInt(1); //k ive no idea
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_MINUTES.getValue()) != 0) {
            mplew.writeInt(10); //max minutes?
        }
        mplew.write(0); //or 1 then lifeID of affected pot, OR IS THIS 0x80000?
        return mplew.getPacket();
    }

    public static final byte[] spawnFlags(List<Pair<String, Integer>> flags) { //Flag_R_1 to 0, etc
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_WELCOME.getValue());
        mplew.write(flags == null ? 0 : flags.size());
        if (flags != null) {
            for (Pair<String, Integer> f : flags) {
                mplew.writeMapleAsciiString(f.left);
                mplew.write(f.right);
            }
        }

        return mplew.getPacket();
    }

    public static final byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) { //Flag_R_1 to 0, etc
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_SCOREBOARD.getValue());
        mplew.writeShort(flags.size());
        for (Pair<Integer, MapleCharacter> f : flags) {
            mplew.writeInt(f.right.getId());
            mplew.writeMapleAsciiString(f.right.getName());
            mplew.writeInt(f.left);
            mplew.write(type == 0 ? 0 : (f.right.getTeam() + 1));
        }

        return mplew.getPacket();
    }

    public static final byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam, int playerTeam) { //Flag_R_1 to 0, etc
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_RESULT.getValue());
        mplew.writeInt(flags.size());
        for (Pair<Integer, MapleCharacter> f : flags) {
            mplew.writeInt(f.right.getId());
            mplew.writeMapleAsciiString(f.right.getName());
            mplew.writeInt(f.left);
            mplew.writeShort(f.right.getTeam() + 1); //??? 1 = bold
            if (GameConstants.GMS) {
                mplew.writeInt(0); //??? delay 600
            }
        }
        mplew.writeZeroBytes(24); //BONUS: ___
        mplew.writeInt(exp);
        mplew.write(0);
        if (GameConstants.GMS) {
            mplew.writeShort(100); //exp modifier
            mplew.writeInt(0); //delay 600
        }
        mplew.write(winningTeam); //losing team?
        mplew.write(playerTeam); //player's team

        return mplew.getPacket();
    }

    public static final byte[] showStatusMessage(final String info, final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x15);
        mplew.writeMapleAsciiString(info); //name got Shield.
        mplew.writeMapleAsciiString(data); //Shield applied to name.

        return mplew.getPacket();
    }

    public static final byte[] showOwnChampionEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(GameConstants.GMS ? 0x22 : 0x20); //i think
        mplew.writeInt(30000); //seconds

        return mplew.getPacket();
    }

    public static final byte[] showChampionEffect(final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(GameConstants.GMS ? 0x22 : 0x20);
        mplew.writeInt(30000);

        return mplew.getPacket();
    }

    public static final byte[] enablePVP(final boolean enabled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ENABLED.getValue());
        mplew.write(enabled ? 1 : 2);

        return mplew.getPacket();
    }

    public static final byte[] getPVPMode(final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_MODE.getValue());
        mplew.write(mode); //11 = starting, 0 = started, 4 = ended??? 8 = blue team win???

        return mplew.getPacket();
    }

    public static final byte[] getPVPType(final int type, List<Pair<Integer, String>> players1, final int team, boolean enabled, final int lvl) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TYPE.getValue());
        mplew.write(type); //type is really a byte
        mplew.write(lvl); //0 = rookie, 1 = gladiator, etc
        mplew.write(enabled ? 1 : 0); //no idea
        if (GameConstants.GMS) {
            mplew.write(0);
        }
        if (type > 0) {
            mplew.write(team); //the team of the player
            mplew.writeInt(players1.size());
            for (Pair<Integer, String> pl : players1) {
                mplew.writeInt(pl.left);
                mplew.writeMapleAsciiString(pl.right);
                mplew.writeShort(2660); //?
            }
        }

        return mplew.getPacket();
    }

    public static final byte[] PVPType(final int type, boolean enabled, final int lvl) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TYPE.getValue());
        mplew.write(type); //type is really a byte
        mplew.write(lvl); //0 = rookie, 1 = gladiator, etc
        mplew.write(enabled ? 1 : 0); //no idea
        if (GameConstants.GMS) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static final byte[] getPVPTeam(List<Pair<Integer, String>> players) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TEAM.getValue());
        mplew.writeInt(players.size());
        for (Pair<Integer, String> pl : players) {
            mplew.writeInt(pl.left);
            mplew.writeMapleAsciiString(pl.right);
            mplew.writeShort(2660); //?
        }

        return mplew.getPacket();
    }

    public static final byte[] getPVPScore(int score, boolean kill) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_SCORE.getValue());
        mplew.writeInt(score);
        mplew.write(kill ? 1 : 0);

        return mplew.getPacket();
    }

    public static final byte[] getPVPIceGage(int score) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ICEGAGE.getValue());
        mplew.writeInt(score);

        return mplew.getPacket();
    }

    public static final byte[] getPVPKilled(String lastWords) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_KILLED.getValue());
        mplew.writeMapleAsciiString(lastWords); //____ defeated ____.

        return mplew.getPacket();
    }

    public static final byte[] getPVPPoints(int p1, int p2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_POINTS.getValue());

        mplew.writeInt(p1);
        mplew.writeInt(p2);

        return mplew.getPacket();
    }

    public static final byte[] getPVPHPBar(int cid, int hp, int maxHp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_HP.getValue());

        mplew.writeInt(cid);
        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static final byte[] getPVPIceHPBar(int hp, int maxHp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ICEKNIGHT.getValue());

        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static final byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_MIST.getValue());
        //DOT
        mplew.writeInt(cid);
        mplew.writeInt(mistSkill);
        mplew.write(mistLevel);
        mplew.writeInt(damage);
        mplew.write(8); //skill delay
        mplew.writeInt(1000);

        return mplew.getPacket();
    }

    public static final byte[] getCaptureFlags(MapleMap map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CAPTURE_FLAGS.getValue());
        mplew.writeRect(map.getArea(0));
        mplew.writeInt(map.getGuardians().get(0).left.x);
        mplew.writeInt(map.getGuardians().get(0).left.y);
        mplew.writeRect(map.getArea(1));
        mplew.writeInt(map.getGuardians().get(1).left.x);
        mplew.writeInt(map.getGuardians().get(1).left.y);
        return mplew.getPacket();
    }

    public static final byte[] getCapturePosition(MapleMap map) { //position of flags if they are still at base
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        final Point p1 = map.getPointOfItem(2910000);
        final Point p2 = map.getPointOfItem(2910001);
        mplew.writeShort(SendPacketOpcode.CAPTURE_POSITION.getValue());
        mplew.write(p1 == null ? 0 : 1);
        if (p1 != null) {
            mplew.writeInt(p1.x);
            mplew.writeInt(p1.y);
        }
        mplew.write(p2 == null ? 0 : 1);
        if (p2 != null) {
            mplew.writeInt(p2.x);
            mplew.writeInt(p2.y);
        }

        return mplew.getPacket();
    }

    public static final byte[] resetCapture() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CAPTURE_RESET.getValue());

        return mplew.getPacket();
    }

    public static final byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery, int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill, int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget, List<AttackPair> attack) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(playerLevel);
        mplew.writeInt(skill);
        mplew.write(skillLevel);
        mplew.writeInt(linkSkill != skill ? linkSkill : 0);
        mplew.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
        mplew.write(direction);
        mplew.write(movementSkill ? 1 : 0);
        mplew.write(pushTarget ? 1 : 0);
        mplew.write(pullTarget ? 1 : 0); //afaik only chains of hell does chains
        mplew.write(0); //unk
        mplew.writeShort(stance); //display
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(projectile);
        mplew.writeInt(chargeTime);
        mplew.writeInt(range);
        mplew.writeShort(attack.size());
        if (GameConstants.GMS) {
            mplew.writeInt(0);
        }
        mplew.write(attackCount);
        mplew.write(0); //idk: probably does something like immobilize target
        for (AttackPair p : attack) {
            mplew.writeInt(p.objectid);
            if (GameConstants.GMS) {
                mplew.writeInt(0);
            }
            mplew.writePos(p.point);
            mplew.writeZeroBytes(5);
            for (Pair<Integer, Boolean> atk : p.attack) {
                mplew.writeInt(atk.left);
                if (GameConstants.GMS) {
                    mplew.writeInt(0);
                }
                mplew.write(atk.right ? 1 : 0);
                mplew.writeShort(0); //1 = no hit
            }
        }

        return mplew.getPacket();
    }

    public static final byte[] pvpSummonAttack(int cid, int playerLevel, int oid, int animation, Point pos, List<AttackPair> attack) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.write(playerLevel);
        mplew.write(animation);
        mplew.writePos(pos);
        mplew.writeInt(0); //<-- delay
        mplew.write(attack.size());
        for (AttackPair p : attack) {
            mplew.writeInt(p.objectid);
            mplew.writePos(p.point);
            mplew.writeShort(p.attack.size());
            for (Pair<Integer, Boolean> atk : p.attack) {
                mplew.writeInt(atk.left);
            }
        }

        return mplew.getPacket();
    }

    public static final byte[] pvpCool(int cid, List<Integer> attack) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_COOL.getValue());
        mplew.writeInt(cid);
        mplew.write(attack.size());
        for (int b : attack) {
            mplew.writeInt(b);
        }
        return mplew.getPacket();
    }

    public static byte[] getPVPClock(int type, int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(3);
        mplew.write(type);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getPVPTransform(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TRANSFORM.getValue());
        mplew.write(type); //2?

        return mplew.getPacket();
    }

    public static byte[] changeTeam(int cid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_TEAM.getValue());
        mplew.writeInt(cid);
        mplew.write(type); //2?

        return mplew.getPacket();
    }

    public static byte[] getPublicNPCInfo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PUBLIC_NPC.getValue());
        mplew.write(GameConstants.publicNpcIds.length);

        for (int i = 0; i < GameConstants.publicNpcIds.length; i++) {
            mplew.writeShort(0); // the name?string
            mplew.writeInt(GameConstants.publicNpcIds[i]);
            mplew.writeLong(i); //0, level needed
            mplew.writeMapleAsciiString(GameConstants.publicNpcs[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] gainForce(int oid, int gain, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(1);
        mplew.writeInt(oid);
        mplew.write(1);
        mplew.writeInt(gain); //total
        mplew.writeInt(max); //gained
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] witchTowerKey(byte key) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WITCH_TOWER_KEY.getValue());
        mplew.write(key);

        return mplew.getPacket();
    }

    public static byte[] MonsterBox(int box) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_BOX.getValue());
        mplew.writeInt(box);

        return mplew.getPacket();
    }

    public static byte[] skillExpired(int skillid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(15);
        mplew.write(1);
        mplew.writeInt(skillid);

        return mplew.getPacket();
    }

    public static byte[] showGainCard(final int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] getOwlMessage(int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.OWL_RESULT.getValue());
        mplew.write(msg);
        return mplew.getPacket();
    }

    public static final byte[] sendMarrageEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MARRAGE_EFFECT.getValue());
        return mplew.getPacket();
    }

    public static byte[] openWeb(String web) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_WEB.getValue());
        mplew.writeMapleAsciiString(web);

        return mplew.getPacket();
    }

    public static byte[] showSpecialAttack(int chrId, int pot_x, int pot_y, int display, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_ATTACK.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(pot_x);
        mplew.writeInt(pot_y);
        mplew.writeInt(display);
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] BeansGameMessage(int cid, int x, String laba) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TIP_BEANS.getValue());
        mplew.writeInt(cid);
        //mplew.write(x);
        mplew.writeMapleAsciiString(laba);
        return mplew.getPacket();
    }

    public static byte[] updateBeansMSG(int beansCount) {
        return updateBeansMSG(0, beansCount);
    }

    public static byte[] updateBeansMSG(int cid, int beansCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(21);
        mplew.writeInt(beansCount);
        return mplew.getPacket();
    }

    public static byte[] updateBeans(int beansCount) {
        return updateBeans(0, beansCount);
    }

    public static byte[] updateBeans(int cid, int beansCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_BEANS.getValue());
        mplew.writeInt(beansCount);
        return mplew.getPacket();
    }

    public static byte[] 能量储存器(int beansCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_BEANS.getValue());//0x253
        mplew.writeInt(beansCount);
        return mplew.getPacket();
    }

    public static byte[] openBeans(int beansCount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_BEANS.getValue());
        mplew.writeInt(beansCount);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] BeansZJgeidd(boolean type, int a) {//豆豆进洞后奖励的
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(type ? BeanGame.BeansType.奖励豆豆效果.getType() : BeanGame.BeansType.奖励豆豆效果B.getType());//类型 05   08  都是加豆豆···
        mplew.writeInt(a);//奖励豆豆的数量
        mplew.write(5);
        return mplew.getPacket();
    }

    public static byte[] BeansZJgeiddB(int a) {//豆豆进洞后奖励的
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.奖励豆豆效果B.getType());//类型 05   08  都是加豆豆···
        mplew.writeInt(a);//奖励豆豆的数量
        mplew.write(0);//未知效果
        return mplew.getPacket();
    }

    public static byte[] BeansHJG(byte type) {//黄金狗
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.黄金狗.getType());//类型
        mplew.write(type);//改变模式
        return mplew.getPacket();
    }

    public static byte[] BeansJDCS(int a, int 加速旋转, int 蓝, int 绿, int 红) {//进洞次数 最多有7个
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.颜色求进洞.getType());
        mplew.write(a);//
        mplew.write(加速旋转);//快速转动
        mplew.write(蓝);// 蓝？
        mplew.write(绿);// 绿？
        mplew.write(红);// 红？
        return mplew.getPacket();
    }

    public static byte[] BeansJDXZ(int a, int 第一排, int 第三排, int 第二排, int 启动打怪效果, int 中奖率, int 加速旋转, boolean 关闭打击效果A, boolean 关闭打击效果B) {//进洞后开始旋转图片
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.进洞旋转.getType());//类型
        mplew.write(a);
        mplew.write(第一排);//第一排 
        mplew.write(第三排);//第三排
        mplew.write(第二排);//第二排
        mplew.write(启动打怪效果);//开启情况下出现怪物打框
        if (启动打怪效果 > 0) {
            mplew.write(中奖率);//中奖率？？%
            mplew.writeInt(0);//未知
        }
        mplew.write(加速旋转);//加速旋转
        mplew.writeBoolean(关闭打击效果A);//boolean
        mplew.writeBoolean(关闭打击效果B);//boolean
        return mplew.getPacket();
    }

    public static byte[] Beans_why() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.未知效果.getType());//类型
        return mplew.getPacket();
    }

    public static byte[] BeansUP(int ITEM) {//%s。请拿到凯瑟琳处确认。
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.领奖NPC.getType());//类型
        mplew.writeInt(ITEM);
        return mplew.getPacket();
    }

    /**
     *
     * @param beansInfo
     * @return
     */
    public static byte[] showBeans(List<MapleBeans> beansInfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeanGame.BeansType.开始打豆豆.getType());
        mplew.write(beansInfo.size());
        for (MapleBeans bean : beansInfo) {
            mplew.writeShort(bean.getPos());
            mplew.write(bean.getType());
            mplew.writeInt(bean.getNumber());
        }
        return mplew.getPacket();
    }

    public static byte[] bombLieDetector(boolean error, int mapid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(error ? 2 : 1);
        mplew.writeInt(mapid);
        mplew.writeInt(channel);

        return mplew.getPacket();
    }

    public static byte[] sendLieDetector(byte[] image, int attempt) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(6); // 1 = not attacking, 2 = tested, 3 = going through 
        mplew.write(4); // 2 give invalid pointer (suppose to be admin macro) 
        mplew.write(2 - attempt); //次数
        if (image == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(image.length);
        mplew.write(image);

        return mplew.getPacket();
    }

    public static byte[] LieDetectorResponse(final byte msg) {
        return LieDetectorResponse(msg, (byte) 0);
    }

    public static byte[] LieDetectorResponse(final byte msg, final byte msg2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(msg); // 1 = not attacking, 2 = tested, 3 = going through 
        mplew.write(msg2);

        return mplew.getPacket();
    }

    public static byte[] getLieDetector(byte type, String tester) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue()); // 2A 00 01 00 00 00  
        mplew.write(type); // 1 = not attacking, 2 = tested, 3 = going through, 4 save screenshot 
        switch (type) {
            case 4: //save screen shot 
                mplew.write(0);
                mplew.writeMapleAsciiString(""); // file name 
                break;
            case 5:
                mplew.write(1); // 2 = save screen shot 
                mplew.writeMapleAsciiString(tester); // me or file name 
                break;
            case 6:
                mplew.write(4); // 2 or anything else, 2 = with maple admin picture, basicaly manager's skill? 
                mplew.write(1); // if > 0, then time = 60,000..maybe try < 0? 
                //mplew.writeInt(size);
                //mplew.write(byte); // bytes 
                break;
            case 7://send this if failed 
                // 2 = You have been appointed as a auto BOT program user and will be restrained. 
                mplew.write(4); // default 
                break;
            case 9:
                // 0 = passed lie detector test 
                // 1 = reward 5000 mesos for not botting. 
                // 2 = thank you for your cooperation with administrator. 
                mplew.write(0);
                break;
            case 8: // save screen shot.. it appears that you may be using a macro-assisted program
                mplew.write(0); // 2 or anything else , 2 = show msg, 0 = none 
                mplew.writeMapleAsciiString(""); // file name 
                break;
            case 10: // no save 
                mplew.write(0); // 2 or anything else, 2 = show msg 
                mplew.writeMapleAsciiString(""); // ?? // hi_You have passed the lie detector test 
                break;
            default:
                mplew.write(0);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] lieDetector(byte mode, byte action, byte[] image, String str1, String str2, String str3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(mode);
        mplew.write(action); //2 = show msg/save screenshot/maple admin picture(mode 6)
        if (mode == 6) {
            mplew.write(1); //if true time is 60:00
            PacketHelper.addImageInfo(mplew, image);
        }
        if (mode == 7 || mode == 9) {
        }
        if (mode == 4) { //save screenshot
            mplew.writeMapleAsciiString(str1); //file name
        }
        if (mode != 5) {
            if (mode == 10) {
                mplew.writeMapleAsciiString(str2); //passed lie detector message
            } else {
                if (mode != 8) {
                }
                mplew.writeMapleAsciiString(str2); //failed lie detector, file name (for screenshot)
            }
        }
        mplew.writeMapleAsciiString(str3); //file name for screenshot

        return mplew.getPacket();
    }
}
