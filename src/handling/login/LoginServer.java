package handling.login;

import constants.GameConstants;
import constants.ServerConfig;
import handling.ServerType;
import handling.netty.ServerConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import server.ServerProperties;
import tools.Pair;

public class LoginServer {

    public static final int PORT = 8484;
    private static Map<Integer, Integer> load = new HashMap<>();
    private static String serverName, eventMessage;
    private static byte flag;
    private static int maxCharacters, userLimit, usersOn = 0;
    private static boolean finishedShutdown = true, adminOnly = false, logPackets = false;
    //private static final HashMap<Integer, Pair<String, String>> loginAuth = new HashMap<>();
    //private static final HashSet<String> loginIPAuth = new HashSet<>();
    private static ServerConnection serverConnection;

    //public static void putLoginAuth(int chrid, String ip, String tempIP) {
    //    loginAuth.put(chrid, new Pair<>(ip, tempIP));
    //    loginIPAuth.add(ip);
    //}

    //public static Pair<String, String> getLoginAuth(int chrid) {
    //    return loginAuth.remove(chrid);
    //}

    //public static boolean containsIPAuth(String ip) {
    //    return loginIPAuth.contains(ip);
    //}

    //public static void removeIPAuth(String ip) {
    //    loginIPAuth.remove(ip);
    //}

    //public static void addIPAuth(String ip) {
    //    loginIPAuth.add(ip);
    //}

    public static final void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public static final void removeChannel(final int channel) {
        load.remove(channel);
    }

    public static final void run_startup_configurations() {
        userLimit = ServerConfig.userLimit;
        serverName = ServerProperties.getProperty("login.serverName");
        eventMessage = ServerProperties.getProperty("login.eventMessage");
        flag = Byte.parseByte(ServerProperties.getProperty("login.flag"));
        adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("world.admin", "false"));
        logPackets = Boolean.parseBoolean(ServerProperties.getProperty("world.logpackets", "false"));
        maxCharacters = ServerConfig.maxCharacters;

        try {
            serverConnection = new ServerConnection(ServerType.登录服务器, PORT, 0, -1);
            serverConnection.run();
            System.out.println("绑定登录端口 " + PORT + ".");
        } catch (Exception e) {
            System.err.println("Binding to port " + PORT + " failed" + e);
        }
    }

    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("正在关闭登录服务器...");
        serverConnection.close();
        finishedShutdown = true; //nothing. lol
    }

    public static final String getServerName() {
        return serverName;
    }

    public static final String getTrueServerName() {
        return serverName.substring(0, serverName.length() - (GameConstants.GMS ? 2 : 3));
    }

    public static final String getEventMessage() {
        return eventMessage;
    }

    public static final byte getFlag() {
        return flag;
    }

    public static final int getMaxCharacters() {
        return maxCharacters;
    }

    public static final Map<Integer, Integer> getLoad() {
        return load;
    }

    public static void setLoad(final Map<Integer, Integer> load_, final int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public static final void setEventMessage(final String newMessage) {
        eventMessage = newMessage;
    }

    public static final void setFlag(final byte newflag) {
        flag = newflag;
    }

    public static final int getUserLimit() {
        return userLimit;
    }

    public static final int getUsersOn() {
        return usersOn;
    }

    public static final void setUserLimit(final int newLimit) {
        userLimit = newLimit;
    }

    public static final int getNumberOfSessions() {
        return serverConnection.getChannels();
    }

    public static final boolean isAdminOnly() {
        return adminOnly;
    }

    public static final boolean isLogPackets() {
        return logPackets;
    }

    public static final boolean isShutdown() {
        return finishedShutdown;
    }

    public static final void setOn() {
        finishedShutdown = false;
    }
}
