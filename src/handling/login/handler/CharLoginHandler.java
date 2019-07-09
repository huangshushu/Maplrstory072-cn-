package handling.login.handler;

import client.LoginCrypto;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldOption;
import database.DBConPool;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginInformationProvider.JobType;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.data.LittleEndianAccessor;
import tools.packet.LoginPacket;
import tools.packet.PacketHelper;

public class CharLoginHandler {

    private static final boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        if (c.loginAttempt > 5) {
            return true;
        }
        return false;
    }

    public static final void ClientHello(final LittleEndianAccessor slea, final MapleClient c) {
        if (slea.readByte() != 4 || slea.readShort() != ServerConfig.MAPLE_VERSION || !String.valueOf(slea.readShort()).equals(ServerConfig.MAPLE_PATCH)) {
            c.getSession().close();
        }
    }

    public static void CheckAccount(final LittleEndianAccessor slea, final MapleClient c) {
        String accountName = slea.readMapleAsciiString();
        c.getSession().write(LoginPacket.CheckAccount(accountName, c.isAccountNameUsed(accountName)));
    }

    public static void RegisterAccount(LittleEndianAccessor slea, MapleClient c) {
        String accountName = slea.readMapleAsciiString();
        String password = slea.readMapleAsciiString();
        String realName = slea.readMapleAsciiString();
        String birthDay = slea.readMapleAsciiString();
        String homeNo = slea.readMapleAsciiString();
        String questionOne = slea.readMapleAsciiString();
        String answerOne = slea.readMapleAsciiString();
        String questionTwo = slea.readMapleAsciiString();
        String answerTwo = slea.readMapleAsciiString();
        String email = slea.readMapleAsciiString();
        String IDCard = slea.readMapleAsciiString();
        String telNo = slea.readMapleAsciiString();
        byte gender = slea.readByte();
        int mPoints = 10000;

        boolean result = false;
        if (!c.isAccountNameUsed(accountName)) {
            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (`name`, password, birthday,email,gender,mPoints) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, accountName);
                    ps.setString(2, LoginCrypto.hexSha1(password));
                    ps.setString(3, birthDay);
                    ps.setString(4, email);
                    ps.setByte(5, gender);
                    ps.setInt(6, mPoints);
                    ps.executeUpdate();
                    ps.close();
                    result = true;
                }
            } catch (SQLException ex) {
                System.err.println("RegisterAccount" + ex);
                FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
            }
        }
        c.getSession().write(LoginPacket.RegisterAccount(result));
    }

    public static void setGender(final LittleEndianAccessor slea, final MapleClient c) {
        byte gender = slea.readByte();
        String username = slea.readMapleAsciiString();
        if (c.getAccountName().equals(username)) {
            c.setGender(gender);
            c.getSession().write(LoginPacket.genderChanged(c));
            c.getSession().write(MaplePacketCreator.serverNotice(1, "设置性别成功请重新登录。"));
        } else {
            c.getSession().close();
        }
    }

    public static final void login(final LittleEndianAccessor slea, final MapleClient c) {
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();

        final boolean ipBan = c.hasBannedIP();
        final boolean macBan = c.hasBannedMac();

        int loginok = c.login(login, pwd, ipBan || macBan);
        final Calendar tempbannedTill = c.getTempBanCalendar();

        if (loginok == 0 && (ipBan || macBan) && !c.isGm()) {
            loginok = 3;
            if (macBan) {
                // this is only an ipban o.O" - maybe we should refactor this a bit so it's more readable
                MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false, 4, false);
            }
        }
        if (ServerConfig.autoRegister) {
            if (loginok == 5) {
                //账号不存在
                c.getSession().write(LoginPacket.RegisterInfo(true));
                loginok = 1;
            }
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getLoginFailed(loginok));
            } else {
                c.getSession().close();
            }
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
                c.getSession().close();
            }
        } else if (c.getGender() == 2) {
            c.getSession().write(LoginPacket.genderNeeded(c));
        } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
        }
    }

    public static final void ServerListRequest(final MapleClient c) {
        for (WorldOption servers : WorldOption.values()) {
            if (WorldOption.getById(servers.getWorld()).show()) {
                c.getSession().write(LoginPacket.getServerList(servers.getWorld(), LoginServer.getLoad()));
            }
        }
        c.getSession().write(LoginPacket.getEndOfServerList());
    }

    public static final void ServerStatusRequest(final MapleClient c) {
        final int numPlayer = LoginServer.getUsersOn();
        final int userLimit = LoginServer.getUserLimit();
        if (numPlayer >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(2));
        } else if (numPlayer * 2 >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(1));
        } else {
            c.getSession().write(LoginPacket.getServerStatus(0));
        }
    }

    public static final void CharlistRequest(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel) || !WorldOption.isExists(server)) { //TODOO: MULTI WORLDS
            c.getSession().write(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }

        if (!WorldOption.getById(server).isAvailable() && !(c.isGm() && server == WorldConstants.gmserver)) {
            c.getSession().write(MaplePacketCreator.serverNotice(1, "很抱歉, " + WorldConstants.getNameById(server) + "暂时未开放。\r\n请尝试连接其他服务器。"));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, but it is used to unstuck
            return;
        }

        System.out.println("[连接信息] " + c.getSession().getRemoteAddress().toString().split(":")[0] + " 连接到世界服务器: " + server + " 频道: " + channel);
        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            c.setChannel(channel);
            c.getSession().write(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }
    }

    public static final void CheckCharName(final String name, final MapleClient c) {
        c.getSession().write(LoginPacket.charNameResponse(name, !(MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()))));
    }

    public static final void CreateChar(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final String name = slea.readMapleAsciiString();
        if (!MapleCharacterUtil.canCreateChar(name, false)) {
            System.out.println("非法命名" + name);
            return;
        }
        final JobType jobType = JobType.getByType(1); // BIGBANG: 0 = Resistance, 1 = Adventurer, 2 = Cygnus, 3 = Aran, 4 = Evan
        final short db = 0; //whether dual blade = 1 or adventurer = 0
        final byte gender = c.getGender(); //??idk corresponds with the thing in addCharStats
        final int face = slea.readInt();
        final int hair = slea.readInt();
        final int top = slea.readInt();
        final int bottom = slea.readInt();
        final int shoes = slea.readInt();
        final int weapon = slea.readInt();
        MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setGender(gender);
        newchar.setName(name);

        if (!LoginInformationProvider.CheckCreate(gender, face, hair, weapon, top, bottom, shoes)) {
            System.out.println("无法创建角色: " + gender + " | " + 0 + " | " + jobType.type + " | ");
            return;
        }

        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);

        int[][] equips = new int[][]{{top, -5}, {bottom, -6}, {shoes, -7}, {weapon, -11},};
        for (int[] i : equips) {
            if (i[0] > 0) {
                Item item = li.getEquipById(i[0]);
                item.setPosition((byte) i[1]);
                item.setGMLog("创建人物");
                equip.addFromDB(item);
            }
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item gift = ii.getEquipById(1052170);
        gift.setExpiration(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000);
        newchar.getInventory(MapleInventoryType.EQUIP).addItem(gift, 1);

        Item gift2 = ii.getEquipById(1002824);
        gift2.setExpiration(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000);
        newchar.getInventory(MapleInventoryType.EQUIP).addItem(gift2, 2);

        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000013, (byte) 0, (short) 200, (byte) 0), 1);
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000014, (byte) 0, (short) 200, (byte) 0), 2);
        newchar.getInventory(MapleInventoryType.CASH).addItem(new Item(5030010, (byte) 0, (short) 1, (byte) 0), 1);
        //newchar.gainMeso(100000, false);
        switch (jobType) {
            case Adventurer: // Adventurer
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0), 1);
                break;
        }
        //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(3013002, (byte) 0, (short) 1, (byte) 0), 2);
        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
            MapleCharacter.saveNewCharToDB(newchar, jobType, db);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
    }

    public static final void CreateUltimate(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn() || c.getPlayer() == null || c.getPlayer().getLevel() < 120 || c.getPlayer().getMapId() != 130000000 || c.getPlayer().getQuestStatus(20734) != 0 || c.getPlayer().getQuestStatus(20616) != 2 || !GameConstants.isKOC(c.getPlayer().getJob()) || !c.canMakeCharacter(c.getPlayer().getWorld())) {
            c.getPlayer().dropMessage(1, "You have no character slots.");
            c.getSession().write(MaplePacketCreator.createUltimate(0));
            return;
        }
        final String name = slea.readMapleAsciiString();
        final int job = slea.readInt(); //job ID
        if (job < 110 || job > 520 || job % 10 > 0 || (job % 100 != 10 && job % 100 != 20 && job % 100 != 30) || job == 430) {
            c.getPlayer().dropMessage(1, "An error has occurred.");
            c.getSession().write(MaplePacketCreator.createUltimate(0));
            return;
        }
        final int face = slea.readInt();
        final int hair = slea.readInt();

        final int hat = slea.readInt();
        final int top = slea.readInt();
        final int glove = slea.readInt();
        final int shoes = slea.readInt();
        final int weapon = slea.readInt();

        final byte gender = c.getPlayer().getGender();
        JobType jobType = JobType.Adventurer;
        if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, jobType.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, jobType.type, hair)) {
            c.getPlayer().dropMessage(1, "An error occurred.");
            c.getSession().write(MaplePacketCreator.createUltimate(0));
            return;
        }

        jobType = JobType.UltimateAdventurer;
        if (!LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, hat) || !LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, top)
                || !LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, glove) || !LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, shoes)
                || !LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, weapon)) {
            c.getPlayer().dropMessage(1, "An error occured.");
            c.getSession().write(MaplePacketCreator.createUltimate(0));
            return;
        }

        MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);
        newchar.setJob(job);
        newchar.setWorld((byte) c.getPlayer().getWorld());
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor((byte) 3); //troll
        newchar.setLevel((short) 51);
        newchar.getStat().str = (short) 4;
        newchar.getStat().dex = (short) 4;
        newchar.getStat().int_ = (short) 4;
        newchar.getStat().luk = (short) 4;
        newchar.setRemainingAp((short) 254); //49*5 + 25 - 16
        newchar.setRemainingSp(job / 100 == 2 ? 128 : 122); //2 from job advancements. 120 from leveling. (mages get +6)
        newchar.getStat().maxhp += 150; //Beginner 10 levels
        newchar.getStat().maxmp += 125;
        switch (job) {
            case 110:
            case 120:
            case 130:
                newchar.getStat().maxhp += 600; //Job Advancement
                newchar.getStat().maxhp += 2000; //Levelup 40 times
                newchar.getStat().maxmp += 200;
                break;
            case 210:
            case 220:
            case 230:
                newchar.getStat().maxmp += 600;
                newchar.getStat().maxhp += 500; //Levelup 40 times
                newchar.getStat().maxmp += 2000;
                break;
            case 310:
            case 320:
            case 410:
            case 420:
            case 520:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 900; //Levelup 40 times
                newchar.getStat().maxmp += 600;
                break;
            case 510:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 450; //Levelup 20 times
                newchar.getStat().maxmp += 300;
                newchar.getStat().maxhp += 800; //Levelup 20 times
                newchar.getStat().maxmp += 400;
                break;
            default:
                return;
        }
        for (int i = 2490; i < 2507; i++) {
            newchar.setQuestAdd(MapleQuest.getInstance(i), (byte) 2, null);
        }
        newchar.setQuestAdd(MapleQuest.getInstance(29947), (byte) 2, null);
        newchar.setQuestAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER), (byte) 0, c.getPlayer().getName());
        newchar.changeSkillLevel_Skip(SkillFactory.getSkill(1074 + (job / 100)), (byte) 5, (byte) 5);
        newchar.changeSkillLevel_Skip(SkillFactory.getSkill(80), (byte) 1, (byte) 1);
        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        int[] items = new int[]{1142257, hat, top, shoes, glove, weapon, hat + 1, top + 1, shoes + 1, glove + 1, weapon + 1}; //brilliant = fine+1
        for (byte i = 0; i < items.length; i++) {
            Item item = li.getEquipById(items[i]);
            item.setPosition((byte) (i + 1));
            newchar.getInventory(MapleInventoryType.EQUIP).addFromDB(item);
        }
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (byte) 0, (short) 100, (byte) 0));
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (byte) 0, (short) 100, (byte) 0));
        c.getPlayer().fakeRelog();
        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm())) {
            MapleCharacter.saveNewCharToDB(newchar, jobType, (short) 0);
            MapleQuest.getInstance(20734).forceComplete(c.getPlayer(), 1101000);
            c.getSession().write(MaplePacketCreator.createUltimate(1));
        } else {
            c.getSession().write(MaplePacketCreator.createUltimate(0));
        }
    }

    public static final void DeleteChar(final LittleEndianAccessor slea, final MapleClient c) {
        String Secondpw_Client = GameConstants.GMS ? slea.readMapleAsciiString() : null;
        if (Secondpw_Client == null) {
            if (slea.readByte() > 0) { // Specific if user have second password or not
                Secondpw_Client = slea.readMapleAsciiString();
            }
            slea.readMapleAsciiString();
        }

        final int Character_ID = slea.readInt();

        if (!c.login_Auth(Character_ID) || !c.isLoggedIn() || loginFailCount(c)) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        byte state = 0;

        if (c.getSecondPassword() != null) { // On the server, there's a second password
            if (Secondpw_Client == null) { // Client's hacking
                c.getSession().close();
                return;
            } else if (!c.CheckSecondPassword(Secondpw_Client)) { // Wrong Password
                state = 12;
            }
        }

        if (state == 0) {
            state = (byte) c.deleteCharacter(Character_ID);
        }
        c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));
    }

    public static final void Character_WithoutSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean haspic, final boolean view) {
        final int charId = slea.readInt();
        if (!c.isLoggedIn() || loginFailCount(c) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) { // TODOO: MULTI WORLDS
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        final String s = c.getSessionIPAddress();
        //LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        c.getSession().write(MaplePacketCreator.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
    }

    public static final void Character_WithSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean view) {
        final String password = slea.readMapleAsciiString();
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || c.getWorld() != 0) { // TODOO: MULTI WORLDS
            c.getSession().close();
            return;
        }
        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16) {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            final String s = c.getSessionIPAddress();
            //LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(MaplePacketCreator.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
        } else {
            c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
        }
    }

    public static void ViewChar(LittleEndianAccessor slea, MapleClient c) {
        Map<Byte, ArrayList<MapleCharacter>> worlds = new HashMap<Byte, ArrayList<MapleCharacter>>();
        List<MapleCharacter> chars = c.loadCharacters(0); //TODO multi world
        c.getSession().write(LoginPacket.showAllCharacter(chars.size()));
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                ArrayList<MapleCharacter> chrr;
                if (!worlds.containsKey(chr.getWorld())) {
                    chrr = new ArrayList<MapleCharacter>();
                    worlds.put(chr.getWorld(), chrr);
                } else {
                    chrr = worlds.get(chr.getWorld());
                }
                chrr.add(chr);
            }
        }
        for (Entry<Byte, ArrayList<MapleCharacter>> w : worlds.entrySet()) {
            c.getSession().write(LoginPacket.showAllCharacterInfo(w.getKey(), w.getValue(), c.getSecondPassword()));
        }
    }
}
