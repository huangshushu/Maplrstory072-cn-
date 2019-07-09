package constants;

import constants.gmbling.PrimaryBoxGamblingHandler;
import constants.gmbling.SeniorBoxGamblingHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public class ServerConstants {
    
    public static final SeniorBoxGamblingHandler  seniorBox = new SeniorBoxGamblingHandler("seniorBox");
    public static final PrimaryBoxGamblingHandler  primaryBox = new PrimaryBoxGamblingHandler("primaryBox");

    public static boolean loadop = true;
    public static boolean TESPIA = false; // true = uses GMS test server, for MSEA it does nothing though
    public static boolean Use_Localhost = false; // true = packets are logged, false = others can connect to server

    /*
     * Specifics which job gives an additional EXP to party
     * returns the percentage of EXP to increase
     */
    public static byte Class_Bonus_EXP(final int job) {
        switch (job) {
            case 501:
            case 530:
            case 531:
            case 532:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
            case 800:
            case 900:
            case 910:
                return 0;
        }
        return 0;
    }

    public static enum PlayerGMRank {
        NORMAL('@', 0),
        DONATOR('#', 1),
        SUPERDONATOR('$', 2),
        INTERN('%', 3),
        GM('!', 4),
        SUPERGM('!', 5),
        ADMIN('!', 6);

        private char commandPrefix;
        private int level;

        PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        public char getCommandPrefix() {
            return commandPrefix;
        }

        public int getLevel() {
            return level;
        }
    }

    public static enum CommandType {
        NORMAL(0),
        TRADE(1);

        private int level;

        CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return level;
        }
    }
    

    public static final String ip_ = ServerConfig.interface_;
    public static final byte[] Gateway_IP_ = ServerConfig.Gateway_IP;


}
