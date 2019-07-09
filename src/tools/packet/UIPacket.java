package tools.packet;

import constants.GameConstants;
import handling.SendPacketOpcode;
import tools.data.MaplePacketLittleEndianWriter;

public class UIPacket {

    public static final byte[] EarnTitleMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.EARN_TITLE_MSG.getValue());
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getSPMsg(byte sp, short job) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeShort(job);
        mplew.write(sp);

        return mplew.getPacket();
    }

    public static byte[] getGPMsg(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] getBPMsg(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(19);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] getGPContribution(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(6);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] getTopMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TOP_MSG.getValue());
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getMidMsg(String msg, boolean keep, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(index); //where the message should appear on the screen
        mplew.writeMapleAsciiString(msg);
        mplew.write(keep ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] clearMidMsg() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLEAR_MID_MSG.getValue());

        return mplew.getPacket();
    }

    public static byte[] getStatusMsg(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static final byte[] MapEff(final String path) {
        return MaplePacketCreator.environmentChange(path, 3);
    }

    public static final byte[] MapNameDisplay(final int mapid) {
        return MaplePacketCreator.environmentChange("maplemap/enter/" + mapid, 3);
    }

    public static final byte[] Aran_Start() {
        return MaplePacketCreator.environmentChange("Aran/balloon", 4);
    }

    public static final byte[] AranTutInstructionalBalloon(final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(GameConstants.GMS ? 0x1A : 0x19);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(1);

        return mplew.getPacket();
    }

    public static final byte[] ShowWZEffect(final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x13); //bb +2
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static final byte[] playMovie(final String data, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAY_MOVIE.getValue());
        mplew.writeMapleAsciiString(data);
        mplew.write(show ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] summonHelper(boolean summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
        mplew.write(summon ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] summonMessage(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(1);
        mplew.writeInt(type);
        mplew.writeInt(7000); // probably the delay

        return mplew.getPacket();
    }

    public static byte[] summonMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(200); // IDK
        mplew.writeShort(0);
        mplew.writeInt(10000); // Probably delay

        return mplew.getPacket();
    }

    public static byte[] IntroLock(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
        mplew.write(enable ? 1 : 0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionStatus(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DIRECTION_STATUS.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfo(int type, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(type);
        mplew.writeLong(value);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfo(String data, int value, int x, int y, int pro) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.writeShort(pro);
        mplew.writeInt(0); //only if pro is > 0

        return mplew.getPacket();
    }

    public static byte[] IntroEnableUI(int wtf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_ENABLE_UI.getValue());
        mplew.write(wtf > 0 ? 1 : 0);
        if (wtf > 0) {
            mplew.writeShort(wtf);
        }

        return mplew.getPacket();
    }

    public static byte[] IntroDisableUI(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] fishingUpdate(byte type, int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FISHING_BOARD_UPDATE.getValue());
        mplew.write(type);
        mplew.writeInt(id);

        return mplew.getPacket();
    }

    public static byte[] fishingCaught(int chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FISHING_CAUGHT.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }
}
