package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.HexTool;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

public class LoginPacket {

    public static final byte[] getHello(final short mapleVersion, final byte[] sendIv, final byte[] recvIv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(0x0E); // length of the packet
        mplew.writeShort(mapleVersion);
        mplew.writeMapleAsciiString(ServerConfig.MAPLE_PATCH);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(4); // 7 = MSEA, 8 = GlobalMS, 5 = Test Server

        return mplew.getPacket();
    }

    public static final byte[] getPing() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);

        mplew.writeShort(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

    public static byte[] RegisterInfo(boolean isAllow) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REGISTER_INFO.getValue());
        if (isAllow == true) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        return mplew.getPacket();
    }
    
    public static byte[] CheckAccount(String accountName, boolean isUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHECK_ACCOUNT_INFO.getValue());
        mplew.writeMapleAsciiString(accountName);
        mplew.write(isUsed ? 1 : 0);
        return mplew.getPacket();
    }
    
    public static byte[] RegisterAccount(boolean result) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REGISTER_ACCOUNT.getValue());
        mplew.write(result ? 0 : 1);
        if (result) {
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static final byte[] genderNeeded(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHOOSE_GENDER.getValue());
        mplew.writeMapleAsciiString(c.getAccountName());

        return mplew.getPacket();
    }

    public static final byte[] genderChanged(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GENDER_SET.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.writeMapleAsciiString(String.valueOf(c.getAccID()));

        return mplew.getPacket();
    }

    public static final byte[] getLoginAUTH() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_AUTH.getValue());
        final int rand = Randomizer.nextInt(5);
        mplew.writeMapleAsciiString("MapLogin" + (rand == 0 ? "" : String.valueOf(rand))); // wonder why map 0 dc
        return mplew.getPacket();
    }

    public static final byte[] StrangeDATA() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RSA_KEY.getValue());
        mplew.writeMapleAsciiString("30819F300D06092A864886F70D010101050003818D0030818902818100994F4E66B003A7843C944E67BE4375203DAA203C676908E59839C9BADE95F53E848AAFE61DB9C09E80F48675CA2696F4E897B7F18CCB6398D221C4EC5823D11CA1FB9764A78F84711B8B6FCA9F01B171A51EC66C02CDA9308887CEE8E59C4FF0B146BF71F697EB11EDCEBFCE02FB0101A7076A3FEB64F6F6022C8417EB6B87270203010001");
        return mplew.getPacket();
    }

    public static final byte[] getLoginFailed(final int reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        /* 3: ID deleted or blocked
         * 4: Incorrect password
         * 5: Not a registered id
         * 6: System error
         * 7: Already logged in
         * 8: System error
         * 9: System error
         * 10: Cannot process so many connections
         * 11: Only users older than 20 can use this channel
         * 13: Unable to log on as master at this ip
         * 14: Wrong gateway or personal info and weird korean button
         * 15: Processing request with that korean button!
         * 16: Please verify your account through email...
         * 17: Wrong gateway or personal info
         * 21: Please verify your account through email...
         * 23: License agreement
         * 25: Maple Europe notice
         * 27: Some weird full client notice, probably for trial versions
         * 32: IP blocked
         * 84: please revisit website for pass change --> 0x07 recv with response 00/01*/
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        if (reason == 84) {
            mplew.writeLong(PacketHelper.getTime(-2));
        } else if (reason == 7) { //prolly this
            mplew.writeZeroBytes(5);
        }

        return mplew.getPacket();
    }

    public static final byte[] getPermBan(final byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(2); // Account is banned
        if (GameConstants.GMS) {
            mplew.writeZeroBytes(5);
            mplew.writeShort(reason);
            mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
        } else {
            mplew.write(reason);
            PacketHelper.addExpirationTime(mplew, -1);
        }
        return mplew.getPacket();
    }

    public static final byte[] getTempBan(final long timestampTill, final byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        if (GameConstants.GMS) {
            mplew.writeZeroBytes(5);
        }
        mplew.write(reason);
        mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.

        return mplew.getPacket();
    }

    public static final byte[] getSecondAuthSuccess(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_SECOND.getValue());
        mplew.write(0);
        mplew.writeInt(client.getAccID());
        mplew.write(client.getGender());
        mplew.write(client.isGm() ? 1 : 0); // Admin byte - Find, Trade, etc.
        if (GameConstants.GMS) {
            mplew.writeShort(2);
        }
        mplew.write(client.isGm() ? 1 : 0); // Admin byte - Commands
        mplew.writeMapleAsciiString(GameConstants.GMS ? client.getAccountName() : "T13333333337W");
        mplew.writeInt(3);
        mplew.writeZeroBytes(GameConstants.GMS ? 6 : 8);
        if (GameConstants.GMS) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis())); //really create date
            mplew.writeInt(4); //idk
            mplew.writeLong(Randomizer.nextLong()); //randomizer.nextLong(), remote hack check.
        } else {
            mplew.writeInt(GameConstants.getCurrentDate()); //september..past date of creation
        }
        return mplew.getPacket();
    }

    public static final byte[] getAuthSuccessRequest(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);
        mplew.writeInt(client.getAccID());
        mplew.write(client.getGender());
        mplew.write(client.isGm() ? 1 : 0); // Admin byte - Find, Trade, etc.
        mplew.write(0); // Admin byte - Commands
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeShort(0); //writeMapleAsciiString  CInPacket::DecodeStr
        mplew.write(0);
        mplew.writeMapleAsciiString(String.valueOf(client.getAccID()));
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static final byte[] deleteCharResponse(final int cid, final int state) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);

        return mplew.getPacket();
    }

    public static final byte[] secondPwError(final byte mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        /*
         * 14 - Invalid password
         * 15 - Second password is incorrect
         */
        mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] enableRecommended() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENABLE_RECOMMENDED.getValue());
        mplew.writeInt(0); //worldID with most characters

        return mplew.getPacket();
    }

    public static byte[] sendRecommended(int world, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_RECOMMENDED.getValue());
        mplew.write(message != null && GameConstants.GMS ? 1 : 0); //amount of messages
        if (message != null && GameConstants.GMS) {
            mplew.writeInt(world);
            mplew.writeMapleAsciiString(message);
        }
        return mplew.getPacket();
    }

    public static final byte[] getServerList(final int serverId, final Map<Integer, Integer> channelLoad) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId); // 0 = Aquilla, 1 = bootes, 2 = cass, 3 = delphinus
        final String worldName = LoginServer.getTrueServerName(); //remove the SEA
        mplew.writeMapleAsciiString(worldName);
        mplew.write(LoginServer.getFlag());
        mplew.writeMapleAsciiString(LoginServer.getEventMessage());
        mplew.writeShort(100);
        mplew.writeShort(100);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        mplew.writeInt(300);

        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i);
            } else {
                load = 1200;
            }
            mplew.writeMapleAsciiString(worldName + "-" + i);
            mplew.writeInt(load);
            mplew.write(serverId);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0); //size: (short x, short y, string msg)
        return mplew.getPacket();
    }

    public static final byte[] getEndOfServerList() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);

        return mplew.getPacket();
    }

    public static final byte[] getLoginWelcome() {
        return MaplePacketCreator.spawnFlags(null);
    }

    public static final byte[] getServerStatus(final int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        /**
         * 0 - Normal 1 - Highly populated 2 - Full
         */
        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);

        return mplew.getPacket();
    }

    public static final byte[] getChannelSelected() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANNEL_SELECTED.getValue());
        mplew.writeZeroBytes(3);

        return mplew.getPacket();
    }

    public static final byte[] getCharList(final String secondpw, final List<MapleCharacter> chars, int charslots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(chars.size()); // 1

        for (final MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, !chr.isGM() && chr.getLevel() >= 30, false);
        }
        mplew.write(3);
        mplew.write(0);
        mplew.writeInt(charslots);
        return mplew.getPacket();
    }

    public static final byte[] addNewCharEntry(final MapleCharacter chr, final boolean worked) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr, false, false);

        return mplew.getPacket();
    }

    public static final byte[] charNameResponse(final String charname, final boolean nameUsed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    private static final void addCharEntry(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, boolean ranking, boolean viewAll) {
        PacketHelper.addCharStats(mplew, chr);
        PacketHelper.addCharLook(mplew, chr, true);
    }

    public static byte[] showAllCharacter(int chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(1); //bIsChar
        mplew.writeInt(chars);
        mplew.writeInt(chars + (3 - chars % 3)); //rowsize
        return mplew.getPacket();
    }

    public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars, String pic) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(chars.size() == 0 ? 5 : 0); //5 = cannot find any
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, true, true);
        }
        mplew.write(pic == null ? 0 : (pic.equals("") ? 2 : 1)); //writing 2 here disables PIC		
        return mplew.getPacket();
    }
}
