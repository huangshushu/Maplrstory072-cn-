package tools;

import client.MapleCharacter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FileoutputUtil {

    public static final String Acc_Stuck = "logs/Log_AccountStuck.rtf",
            Login_Error = "logs/Log_Login_Error.rtf",
            //          IP_Log = "Log_AccountIP.rtf",
            //          GMCommand_Log = "Log_GMCommand.rtf",
            //          Zakum_Log = "Log_Zakum.rtf",
            //          Horntail_Log = "Log_Horntail.rtf",
            Pinkbean_Log = "logs/Log_Pinkbean.rtf",
            ScriptEx_Log = "logs/Log_Script_Except.rtf",
            PacketEx_Log = "logs/Log_Packet_Except.rtf", // I cba looking for every error, adding this back in.
            Donator_Log = "logs/Log_Donator.rtf",
            Hacker_Log = "logs/Log_Hacker.rtf",
            Movement_Log = "logs/Log_Movement.rtf",
            CommandEx_Log = "logs/Log_Command_Except.rtf";
//	    PQ_Log = "Log_PQ.rtf"

    // End
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");
    private static final String FILE_PATH = "logs/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String ERROR = "error/";

    static {
        sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void log(final String file, final String msg) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
            out.write(("\n------------------------ " + CurrentReadable_Time() + " ------------------------\n").getBytes());
            out.write(msg.getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void outputFileError(final String file, final Throwable t) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
            out.write(("\n------------------------ " + CurrentReadable_Time() + " ------------------------\n").getBytes());
            out.write(getString(t).getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static String CurrentReadable_Date() {
        return sdf_.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_Time() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_TimeGMT() {
        return sdfGMT.format(new Date());
    }

    public static String NowTime() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式    
        String hehe = dateFormat.format(now);
        return hehe;
    }

    public static String getString(final Throwable e) {
        String retValue = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
            }
        }
        return retValue;
    }

    public static void logToFile(final String file, final String msg, boolean notExists, boolean size) {
        FileOutputStream out = null;
        try {
            File outputFile = new File("logs/" + file);
            if (outputFile.exists() && outputFile.isFile() && outputFile.length() >= 1024000 && size) {
                String sub = file.substring(0, file.indexOf("/") + 1) + "old/" + file.substring(file.indexOf("/") + 1, file.length() - 4);
                String time = sdfGMT.format(Calendar.getInstance().getTime());
                String sub2 = file.substring(file.length() - 4, file.length());
                String output = "logs/" + sub + "_" + time + sub2;
                if (new File(output).getParentFile() != null) {
                    new File(output).getParentFile().mkdirs();
                }
                outputFile.renameTo(new File(output));
                outputFile = new File("logs/" + file);
            }
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream("logs/" + file, true);
            if (!out.toString().contains(msg) || !notExists) {
                OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
                osw.write(msg);
                osw.flush();
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void logToFile(final String file, final String msg, boolean notExists) {
        logToFile(file, msg, notExists, true);
    }

    public static void logToFile_chr(MapleCharacter chr, final String file, final String msg) {
        logToFile(file, "\r\n" + CurrentReadable_Time() + " 账号 " + chr.getClient().getAccountName() + " 名称 " + chr.getName() + " (" + chr.getId() + ") 等级 " + chr.getLevel() + " 地图 " + chr.getMapId() + " " + msg, false);
    }

    public static void logToFile(final String file, final String msg) {
        logToFile(file, msg, false);
    }
    
    public static void printError(final String name, final Throwable t) {
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(getString(t).getBytes());
            out.write("\n---------------------------------\r\n".getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void printError(String file, Throwable t, String info) {
        FileOutputStream out = null;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(("\r\n------------------------ " + CurrentReadable_Time() + " ------------------------\r\n").getBytes());
            out.write((info + "\r\n").getBytes());
            out.write(getString(t).getBytes());
        } catch (IOException ignore) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
}
