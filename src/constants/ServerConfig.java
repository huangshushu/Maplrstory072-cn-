/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author wubin
 */
public class ServerConfig {

    public static boolean USE_FIXED_IV = false;//false true
    public static boolean autoRegister = false;//false true
    public static final String SQL_PORT = "3306";
    public static final String SQL_DATABASE = "v072";
    public static final String SQL_USER = "root";
    public static final String SQL_PASSWORD = "root";
    public static final String ASCII = "GBK";
    public static final int userLimit = 1000;
    public static final int maxCharacters = 3;
    public static final byte[] Gateway_IP = new byte[]{(byte) 127, (byte) 0, (byte) 0, (byte) 1};
    public static final String interface_ = "127.0.0.1";
    public static final short MAPLE_VERSION = (short) 72;
    public static final String MAPLE_PATCH = "1";
    public static final int channelCount = 3;
    public static boolean EXP = true;//false true
    public static final int monsterSpawn = 3; //2 monsters in one spawn point instead of one
    public static final int maxlevel = 200;
    public static final boolean MoonlightRevamp = true;
}
