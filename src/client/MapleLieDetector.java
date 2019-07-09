/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import handling.world.World;
import java.util.concurrent.ScheduledFuture;
import scripting.LieDetectorScript;
import server.Timer;
import server.maps.MapleMap;
import tools.HexTool;
import tools.Pair;
import tools.packet.MaplePacketCreator;

/**
 *
 * @author wubin
 */
public class MapleLieDetector {

    //public MapleCharacter chr;
    public byte type;
    public int attempt;
    public int cid;
    public String tester;
    public String answer;
    public boolean inProgress;
    public boolean passed;
    public long lasttime;
    public ScheduledFuture<?> schedule;

    /*public MapleLieDetector(MapleCharacter c) {
        this.chr = c;
        reset();
    }*/
    public MapleLieDetector(int chid) {
        cid = chid;
        reset();
    }

    public final boolean startLieDetector(final String tester, final boolean isItem, final boolean anotherAttempt) {
        if ((!anotherAttempt) && (((isPassed()) && (isItem)) || (inProgress()) || (this.attempt == 3))) {
            return false;
        }
        Pair captcha = LieDetectorScript.getImageBytes();
        if (captcha == null) {
            return false;
        }
        byte[] image = HexTool.getByteArrayFromHexString((String) captcha.getLeft());
        this.answer = ((String) captcha.getRight());
        this.tester = tester;
        this.inProgress = true;
        this.type = (byte) (isItem ? 0 : 1);
        this.attempt += 1;
        MapleCharacter chrid = MapleCharacter.getOnlineCharacterById(cid);
        if (this.attempt < 3) {
            if (chrid != null) {
                chrid.getClient().getSession().write(MaplePacketCreator.sendLieDetector(image, this.attempt));
            }
        }
        schedule = Timer.EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
                MapleCharacter searchchr = MapleCharacter.getOnlineCharacterById(cid);
                if (((!MapleLieDetector.this.isPassed()) /*&& (!isItem)*/) && (searchchr != null)) {
                    if (MapleLieDetector.this.attempt >= 3) {
                        MapleCharacter search_chr = searchchr.getMap().getCharacterByName(tester);
                        if ((search_chr != null) && (search_chr.getId() != searchchr.getId())) {
                            search_chr.dropMessage(5, searchchr.getName() + " 沒有通過測謊儀。");
                            //FileoutputUtil.logToFile("logs/Data/測謊失敗.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + searchchr.getClient().getSession().remoteAddress().toString().split(":")[0] + " 帳號: " + searchchr.getClient().getAccountName() + " 玩家: " + searchchr.getClient().getPlayer().getName() + " 沒有通過測謊儀。");
                        }
                        MapleLieDetector.this.end();
                        searchchr.getClient().getSession().write(MaplePacketCreator.LieDetectorResponse((byte) 7, (byte) 0));
                        MapleMap map = searchchr.getMap().getReturnMap();
                        searchchr.changeMap(map, map.getPortal(0));
                        World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密語] 玩家: " + searchchr.getName() + " (等級 " + searchchr.getLevel() + ") 未通過測謊儀檢測，疑似使用腳本外掛！"));
                    } else {
                        MapleLieDetector.this.startLieDetector(tester, isItem, true);
                    }
                }
            }
        }, 60000);

        return true;
    }

    public final int getAttempt() {
        return this.attempt;
    }

    public final byte getLastType() {
        return this.type;
    }

    public final String getTester() {
        return this.tester;
    }

    public final String getAnswer() {
        return this.answer;
    }

    public final boolean inProgress() {
        return this.inProgress;
    }

    public final boolean isPassed() {
        return this.passed;
    }

    public void setPassed(boolean passedi) {
        passed = passedi;
    }

    public final boolean canDetector(long time) {
        return lasttime + 300000 > time;
    }

    public final void end() {
        this.inProgress = false;
        this.passed = true;
        this.attempt = 0;
        lasttime = System.currentTimeMillis();
        if (schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
    }

    public final void reset() {
        this.tester = "";
        this.answer = "";
        this.attempt = 0;
        this.inProgress = false;
        this.passed = false;
    }
}

/**
 *
 * @author wubin
 */
/*public class MapleLieDetector {

    //public MapleCharacter chr;
    private final WeakReference<MapleCharacter> chr;
    //private MapleCharacter chr;
    public ScheduledFuture<?> schedule;
    public byte type;
    public int attempt;
    public String tester;
    public String answer;
    public boolean inProgress;
    public boolean passed;
    public long lasttime;

    public MapleLieDetector(MapleCharacter c) {
        //chr = c;
        chr = new WeakReference<>(c);
        reset();
    }

    public final boolean startLieDetector(final String testers, final boolean isItem, final boolean anotherAttempt) {

        if (chr == null) {
            FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n Error LieDetectorScript, chr == null");
            return false;
        }
        if ((!anotherAttempt) && ((inProgress()) || (attempt == -1))) {
            FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n Error LieDetectorScript, 使用測謊儀失敗1");
            return false;
        }
        Pair captcha = LieDetectorScript.getImageBytes();
        if (captcha == null) {
            FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n Error LieDetectorScript, captcha = null");
            return false;
        }
        byte[] image = HexTool.getByteArrayFromHexString((String) captcha.getLeft());
        if (image == null) {
            FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n Error LieDetectorScript, image = null");
        }
        answer = ((String) captcha.getRight());
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n answer：" + answer);
        tester = testers;
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n tester：" + tester);
        inProgress = true;
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n inProgress：" + inProgress);
        type = (byte) (isItem ? 0 : 1);
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n type：" + type);
        attempt -= 1;
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n attempt：" + attempt);
        chr.get().getClient().sendPacket(MaplePacketCreator.sendLieDetector(image, attempt));
        FileoutputUtil.logToFile("logs/LieDetectorScript.txt", "\r\n image：" + Arrays.toString(image));
        schedule = EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (chr != null) {
                    if (attempt == -1) {
                        MapleCharacter search_chr = chr.get().getMap().getCharacterByName(tester);
                        if ((search_chr != null) && (search_chr.getId() != chr.get().getId())) {
                            search_chr.dropMessage(5, chr.get().getName() + " 沒有通過測謊儀。");
                            //search_chr.gainMeso(7000, true);
                        }
                        end();
                        chr.get().getClient().getSession().writeAndFlush(MaplePacketCreator.LieDetectorResponse((byte) 7, (byte) 0));
                        MapleMap map = chr.get().getMap().getReturnMap();
                        chr.get().changeMap(map, map.getPortal(0));
                        World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密語] 玩家: " + chr.get().getName() + " (等級 " + chr.get().getLevel() + ") 未通過測謊儀檢測，疑似使用腳本外掛！"));
                    } else {
                        startLieDetector(tester, isItem, true);
                    }
                }
            }
        }, 60000);
        return true;
    }

    public final int getAttempt() {
        return attempt;
    }

    public final byte getLastType() {
        return type;
    }

    public final String getTester() {
        return tester;
    }

    public final String getAnswer() {
        return answer;
    }

    public final boolean inProgress() {
        return inProgress;
    }

    public final boolean isPassed() {
        return passed;
    }

    public final boolean canDetector(long time) {
        return lasttime + 300000 > time;
    }

    public final void end() {
        inProgress = false;
        passed = true;
        attempt = 1;
        lasttime = System.currentTimeMillis();
        if (schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
    }

    public final void reset() {
        tester = "";
        answer = "";
        attempt = 1;
        inProgress = false;
        passed = false;
    }
}*/
