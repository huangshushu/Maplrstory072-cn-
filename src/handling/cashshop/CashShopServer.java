package handling.cashshop;

import constants.ServerConfig;
import constants.ServerConstants;
import handling.MapleServerHandler;
import handling.ServerType;
import handling.channel.PlayerStorage;
import handling.netty.ServerConnection;
import java.net.InetSocketAddress;

public class CashShopServer {

    private static String ip;
    private final static int PORT = 8600;
    private static PlayerStorage players/*, playersMTS*/;
    private static boolean finishedShutdown = false;
    private static ServerConnection serverConnection;

    public static final void run_startup_configurations() {
        ip = ServerConstants.ip_ + ":" + PORT;

        players = new PlayerStorage(-10);
        //playersMTS = new PlayerStorage(-20);
        try {
            serverConnection = new ServerConnection(ServerType.商城服务器, PORT, 0, -1);
            serverConnection.run();
            System.out.println("绑定商城端口 " + PORT + ".");
        } catch (final Exception e) {
            System.err.println("Binding to port " + PORT + " failed");
            e.printStackTrace();
            throw new RuntimeException("Binding failed.", e);
        }
    }

    public static final String getIP() {
        return ip;
    }

    public static final PlayerStorage getPlayerStorage() {
        return players;
    }

    /*public static final PlayerStorage getPlayerStorageMTS() {
        return playersMTS;
    }*/
    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("正在关闭商城服务器...");
        players.disconnectAll();
        //playersMTS.disconnectAll();
        //MTSStorage.getInstance().saveBuyNow(true);
        System.out.println("正在解除商城服务器端口绑定...");
        serverConnection.close();
        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }
}
