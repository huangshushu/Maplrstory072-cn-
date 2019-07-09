/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.anticheat;

import client.MapleBuffStat;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import constants.GameConstants;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.SkillFactory;
import handling.login.LoginServer;
import handling.world.World;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.AutobanManager;
import server.Timer.CheatTimer;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.StringUtil;

public class CheatTracker {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rL = lock.readLock(), wL = lock.writeLock();
    private final Map<CheatingOffense, CheatingOffenseEntry> offenses = new LinkedHashMap<CheatingOffense, CheatingOffenseEntry>();
    private WeakReference<MapleCharacter> chr;
    // For keeping track of speed attack hack.
    private long lastAttackTime = 0;
    private int lastAttackTickCount = 0;
    private byte Attack_tickResetCount = 0;
    private long Server_ClientAtkTickDiff = 0;
    private long lastDamage = 0;
    private long takingDamageSince;
    private int numSequentialDamage = 0;
    private long lastDamageTakenTime = 0;
    private byte numZeroDamageTaken = 0;
    private int numSequentialSummonAttack = 0;
    private long summonSummonTime = 0;
    private int numSameDamage = 0;
    private Point lastMonsterMove;
    private int monsterMoveCount;
    private int attacksWithoutHit = 0;
    private byte dropsPerSecond = 0;
    private long lastDropTime = 0;
    private byte msgsPerSecond = 0;
    private long lastMsgTime = 0;
    private ScheduledFuture<?> invalidationTask;
    private int gm_message = 0;
    private int lastTickCount = 0, tickSame = 0;
    private long lastSmegaTime = 0, lastBBSTime = 0, lastASmegaTime = 0;

    //private int lastFamiliarTickCount = 0;
    //private byte Familiar_tickResetCount = 0;
    //private long Server_ClientFamiliarTickDiff = 0;
    private int numSequentialFamiliarAttack = 0;
    private long familiarSummonTime = 0;
    private long lastSaveTime = 0;
    private long lastLieDetectorTime = 0;

    public CheatTracker(final MapleCharacter chr) {
        start(chr);
    }

    public final void checkAttack(final int skillId, final int tickcount) {
        int AtkDelay = GameConstants.getAttackDelay(skillId);
        if (chr.get().getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
            AtkDelay /= 6;// 使用這Buff之後 tickcount - lastAttackTickCount 可以為0...
        }
        // 攻擊加速
        if (chr.get().getBuffedValue(MapleBuffStat.BOOSTER) != null) {
            AtkDelay /= 1.5;
        }
        // 最終極速
        if (chr.get().getBuffedValue(MapleBuffStat.SPEED_INFUSION) != null) {
            AtkDelay /= 1.35;
        }
        // 狂郎
        if (GameConstants.isAran(chr.get().getJob())) {
            AtkDelay /= 1.4;// 407
        }
        // 海盜、拳霸
        if (chr.get().getJob() >= 500 && chr.get().getJob() <= 512) {
            AtkDelay = 0;// 407
        }
        // 強化連擊
        if (skillId == 21101003 || skillId == 5110001) {
            AtkDelay = 0;
        }
        if ((tickcount - lastAttackTickCount) < AtkDelay) {
            if (chr.get().getAttackMonster() >= 100) {
                if (!chr.get().hasGmLevel(1)) {
                    chr.get().ban(chr.get().getName() + "攻击速度异常，技能：" + skillId, true, true, false);
                    chr.get().getClient().getSession().close();
                    String reason = "使用非法程序";
                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[公告事项] " + chr.get().getName() + " 因为" + reason + "被系统永久停封。"));
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + chr.get().getName() + " 攻击无延迟自动封号! "));
                } else {
                    chr.get().dropMessage(6, "攻击速度异常");
                }
                FileoutputUtil.logToFile("Ban/攻击速度异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + chr.get().getName() + " 职业:" + chr.get().getJob() + " 技能: " + skillId + " check: " + (tickcount - lastAttackTickCount) + " " + "AtkDelay: " + AtkDelay);
                return;
            }
            FileoutputUtil.logToFile("Hack/攻击速度异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + chr.get().getName() + " 职业:" + chr.get().getJob() + "　技能: " + skillId + " check: " + (tickcount - lastAttackTickCount) + " " + "AtkDelay: " + AtkDelay);

            chr.get().addAttackMonster();
            registerOffense(CheatingOffense.FASTATTACK, "攻击速度异常，技能: " + skillId + " check: " + (tickcount - lastAttackTickCount) + " " + "AtkDelay: " + AtkDelay);
        }

        //if (LoginServer.isLogPackets()) {
        //chr.get().dropMessage(6, "Delay [" + skillId + "] = " + (tickcount - lastAttackTickCount) + ", " + (AtkDelay));
        //}
        updateTick(tickcount);
        lastAttackTickCount = tickcount;
    }

    //unfortunately PVP does not give a tick count
    public final void checkPVPAttack(final int skillId) {
        final int AtkDelay = GameConstants.getAttackDelay(skillId, skillId == 0 ? null : SkillFactory.getSkill(skillId));
        final long STime_TC = System.currentTimeMillis() - lastAttackTime; // hack = - more
        if (STime_TC < AtkDelay) { // 250 is the ping, TODO
            registerOffense(CheatingOffense.FASTATTACK);
        }
        lastAttackTime = System.currentTimeMillis();
    }

    public final long getLastAttack() {
        return lastAttackTime;
    }

    public final void checkTakeDamage(final int damage) {
        numSequentialDamage++;
        lastDamageTakenTime = System.currentTimeMillis();

        // System.out.println("tb" + timeBetweenDamage);
        // System.out.println("ns" + numSequentialDamage);
        // System.out.println(timeBetweenDamage / 1500 + "(" + timeBetweenDamage / numSequentialDamage + ")");
        if (lastDamageTakenTime - takingDamageSince / 500 < numSequentialDamage) {
            registerOffense(CheatingOffense.FAST_TAKE_DAMAGE);
        }
        if (lastDamageTakenTime - takingDamageSince > 4500) {
            takingDamageSince = lastDamageTakenTime;
            numSequentialDamage = 0;
        }
        /*	(non-thieves)
        Min Miss Rate: 2%
        Max Miss Rate: 80%
        (thieves)
        Min Miss Rate: 5%
        Max Miss Rate: 95%*/
        if (damage == 0) {
            numZeroDamageTaken++;
            if (numZeroDamageTaken >= 35) { // Num count MSEA a/b players
                numZeroDamageTaken = 0;
                registerOffense(CheatingOffense.HIGH_AVOID, "回避率过高 ");
            }
        } else if (damage != -1) {
            numZeroDamageTaken = 0;
        }
    }

    public final void checkSameDamage(final int dmg, final double expected) {
        if (dmg > 2000 && lastDamage == dmg && chr.get() != null && (chr.get().getLevel() < 175 || dmg > expected * 2)) {
            numSameDamage++;

            if (numSameDamage > 5) {
                registerOffense(CheatingOffense.SAME_DAMAGE, numSameDamage + " 次, 攻击伤害 " + dmg + ", 预计伤害 " + expected + " [等级: " + chr.get().getLevel() + ", 职业: " + chr.get().getJob() + "]");
                numSameDamage = 0;
            }
        } else {
            lastDamage = dmg;
            numSameDamage = 0;
        }
    }

    public final void checkMoveMonster(final Point pos) {
        if (pos == lastMonsterMove) {
            monsterMoveCount++;
            if (monsterMoveCount > 10) {
                registerOffense(CheatingOffense.MOVE_MONSTERS, "Position: " + pos.x + ", " + pos.y);
                monsterMoveCount = 0;
            }
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    public final void resetSummonAttack() {
        summonSummonTime = System.currentTimeMillis();
        numSequentialSummonAttack = 0;
    }

    public final boolean checkSummonAttack() {
        numSequentialSummonAttack++;
        //estimated
        // System.out.println(numMPRegens + "/" + allowedRegens);
        if ((System.currentTimeMillis() - summonSummonTime) / (1000 + 1) < numSequentialSummonAttack) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
            return false;
        }
        return true;
    }

    public final void resetFamiliarAttack() {
        familiarSummonTime = System.currentTimeMillis();
        numSequentialFamiliarAttack = 0;
        //lastFamiliarTickCount = 0;
        //Familiar_tickResetCount = 0;
        //Server_ClientFamiliarTickDiff = 0;
    }

    public final boolean checkFamiliarAttack(final MapleCharacter chr) {
        /*final int tickdifference = (tickcount - lastFamiliarTickCount);
        if (tickdifference < 500) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
        }
        final long STime_TC = System.currentTimeMillis() - tickcount;
        final long S_C_Difference = Server_ClientFamiliarTickDiff - STime_TC;
        if (S_C_Difference > 500) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
        }
        Familiar_tickResetCount++;
        if (Familiar_tickResetCount > 4) {
            Familiar_tickResetCount = 0;
            Server_ClientFamiliarTickDiff = STime_TC;
        }
        lastFamiliarTickCount = tickcount;*/
        numSequentialFamiliarAttack++;
        //estimated
        // System.out.println(numMPRegens + "/" + allowedRegens);
        if ((System.currentTimeMillis() - familiarSummonTime) / (600 + 1) < numSequentialFamiliarAttack) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
            return false;
        }
        return true;
    }

    public final void checkDrop() {
        checkDrop(false);
    }

    public final void checkDrop(final boolean dc) {
        if ((System.currentTimeMillis() - lastDropTime) < 1000) {
            dropsPerSecond++;
            if (dropsPerSecond >= (dc ? 32 : 16) && chr.get() != null && !chr.get().isGM()) {
                if (dc) {
                    chr.get().getClient().getSession().close();
                } else {
                    chr.get().getClient().setMonitored(true);
                }
            }
        } else {
            dropsPerSecond = 0;
        }
        lastDropTime = System.currentTimeMillis();
    }

    public final void checkMsg() { //ALL types of msg. caution with number of  msgsPerSecond
        if ((System.currentTimeMillis() - lastMsgTime) < 1000) { //luckily maplestory has auto-check for too much msging
            msgsPerSecond++;
            if (msgsPerSecond > 10 && chr.get() != null && !chr.get().isGM()) {
                chr.get().getClient().getSession().close();
            }
        } else {
            msgsPerSecond = 0;
        }
        lastMsgTime = System.currentTimeMillis();
    }

    public final int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }

    public final void setAttacksWithoutHit(final boolean increase) {
        if (increase) {
            this.attacksWithoutHit++;
        } else {
            this.attacksWithoutHit = 0;
        }
    }

    public final void registerOffense(final CheatingOffense offense) {
        registerOffense(offense, null);
    }

    public final void registerOffense(final CheatingOffense offense, final String param) {
        final MapleCharacter chrhardref = chr.get();
        if (chrhardref == null || !offense.isEnabled() || chrhardref.isClone() || chrhardref.isGM()) {
            return;
        }
        if (chr.get().hasGmLevel(5)) {
            chr.get().dropMessage(6, "注册：" + offense + " 原因：" + param);
        }
        CheatingOffenseEntry entry = null;
        rL.lock();
        try {
            entry = offenses.get(offense);
        } finally {
            rL.unlock();
        }
        if (entry != null && entry.isExpired()) {
            expireEntry(entry);
            entry = null;
            gm_message = 0;
        }
        if (entry == null) {
            entry = new CheatingOffenseEntry(offense, chrhardref.getId());
        }
        if (param != null) {
            entry.setParam(param);
        }
        entry.incrementCount();
        if (offense.shouldAutoban(entry.getCount())) {
            final byte type = offense.getBanType();
            String outputFileName;
            if (type == 1) {
                AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
            } else if (type == 2) {
                outputFileName = "断线";
                World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + chrhardref.getName() + " 自动断线 类别: " + offense.toString() + " 原因: " + (param == null ? "" : (" - " + param))));
                FileoutputUtil.logToFile("Hack/" + outputFileName + ".txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + chr.get().getName() + " 项目：" + offense.toString() + " 原因： " + (param == null ? "" : (" - " + param)));
                chrhardref.getClient().getSession().close();
            } else if (type == 3) {
                boolean ban = true;
                outputFileName = "封号";
                String show = "使用非法程序";
                String real = "";
                if (offense.toString() == "ITEMVAC_SERVER") {
                    outputFileName = "全图吸物";
                    real = "使用全图吸物";
                    ban = true;
                } else if (offense.toString() == "FAST_SUMMON_ATTACK") {
                    outputFileName = "召唤兽无延迟";
                    real = "使用召唤兽攻击无延迟";
                } else if (offense.toString() == "MOB_VAC") {
                    outputFileName = "吸怪";
                    real = "使用吸怪";
                    ban = true;

                } else if (offense.toString() == "ATTACK_FARAWAY_MONSTER_BAN") {
                    outputFileName = "全屏攻击";
                    real = "使用全屏攻击";
                    ban = true;
                } else {
                    ban = false;
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + " (编号: " + chrhardref.getId() + " )使用外挂! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param))));
                }

                if (chr.get().hasGmLevel(1)) {
                    chr.get().dropMessage(6, "触发违规: " + real + " param: " + (param == null ? "" : (" - " + param)));
                } else if (ban) {
                    FileoutputUtil.logToFile("Ban/" + outputFileName + ".txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + chr.get().getName() + " 项目：" + offense.toString() + " 原因： " + (param == null ? "" : (" - " + param)));
                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[公告事项] " + chrhardref.getName() + " 因為" + show + "而被系统永久停封。"));
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + chrhardref.getName() + " " + real + "自动封号! "));
                    chrhardref.ban(chrhardref.getName() + real, true, true, false);
                    chrhardref.getClient().getSession().close();
                } else {
                    FileoutputUtil.logToFile("Hack/" + outputFileName + ".txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + chr.get().getName() + " 项目：" + offense.toString() + " 原因： " + (param == null ? "" : (" - " + param)));
                }
            }
            gm_message = 100;
            return;
        }
        wL.lock();
        try {
            offenses.put(offense, entry);
        } finally {
            wL.unlock();
        }
        switch (offense) {
            case FAST_SUMMON_ATTACK:
            case ITEMVAC_SERVER:
            case MOB_VAC:
            case HIGH_DAMAGE_MAGIC:
            case HIGH_DAMAGE_MAGIC_2:
            case HIGH_DAMAGE:
            case HIGH_DAMAGE_2:
            case ATTACK_FARAWAY_MONSTER:
            case SAME_DAMAGE:
                gm_message--;
                boolean log = false;
                String out_log = "";
                String show = offense.name();
                switch (show) {
                    case "ATTACK_FARAWAY_MONSTER":
                        show = "全图攻击";
                        out_log = "攻击范围异常";
                        log = true;
                        break;
                    case "MOB_VAC":
                        show = "使用吸怪";
                        out_log = "吸怪";
                        log = true;
                        break;
                }
                if (gm_message % 5 == 0) {
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + chrhardref.getName() + " (编号:" + chrhardref.getId() + ")疑似外挂! " + show + (param == null ? "" : (" - " + param))));
                    if (log) {
                        FileoutputUtil.logToFile("Hack/" + out_log + ".txt", "\r\n" + FileoutputUtil.NowTime() + " " + chrhardref.getName() + " (编号:" + chrhardref.getId() + ")疑似外挂! " + show + (param == null ? "" : (" - " + param)));
                    }
                }
                if (gm_message == 0) {
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[公告事项] " + chrhardref.getName() + " (编号: " + chrhardref.getId() + " )疑似外挂！" + show + (param == null ? "" : (" - " + param))));
                    AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
                    gm_message = 100;
                }
                break;
        }
        CheatingOffensePersister.getInstance().persistEntry(entry);
    }

    public void updateTick(int newTick) {
        if (newTick <= lastTickCount) { //definitely packet spamming or the added feature in many PEs which is to generate random tick
            if (tickSame >= 5 && chr.get() != null && !chr.get().isGM()) {
                chr.get().getClient().getSession().close();
            } else {
                tickSame++;
            }
        } else {
            tickSame = 0;
        }
        lastTickCount = newTick;
    }

    public boolean canSmega() {
        if (lastSmegaTime + 15000 > System.currentTimeMillis() && chr.get() != null && !chr.get().isGM()) {
            return false;
        }
        lastSmegaTime = System.currentTimeMillis();
        return true;
    }

    public boolean canAvatarSmega() {
        if (lastASmegaTime + 300000 > System.currentTimeMillis() && chr.get() != null && !chr.get().isGM()) {
            return false;
        }
        lastASmegaTime = System.currentTimeMillis();
        return true;
    }

    public boolean canBBS() {
        if (lastBBSTime + 60000 > System.currentTimeMillis() && chr.get() != null && !chr.get().isGM()) {
            return false;
        }
        lastBBSTime = System.currentTimeMillis();
        return true;
    }

    public final void expireEntry(final CheatingOffenseEntry coe) {
        wL.lock();
        try {
            offenses.remove(coe.getOffense());
        } finally {
            wL.unlock();
        }
    }

    public final int getPoints() {
        int ret = 0;
        CheatingOffenseEntry[] offenses_copy;
        rL.lock();
        try {
            offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
        } finally {
            rL.unlock();
        }
        for (final CheatingOffenseEntry entry : offenses_copy) {
            if (entry.isExpired()) {
                expireEntry(entry);
            } else {
                ret += entry.getPoints();
            }
        }
        return ret;
    }

    public final Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    public final String getSummary() {
        final StringBuilder ret = new StringBuilder();
        final List<CheatingOffenseEntry> offenseList = new ArrayList<CheatingOffenseEntry>();
        rL.lock();
        try {
            for (final CheatingOffenseEntry entry : offenses.values()) {
                if (!entry.isExpired()) {
                    offenseList.add(entry);
                }
            }
        } finally {
            rL.unlock();
        }
        Collections.sort(offenseList, new Comparator<CheatingOffenseEntry>() {

            @Override
            public final int compare(final CheatingOffenseEntry o1, final CheatingOffenseEntry o2) {
                final int thisVal = o1.getPoints();
                final int anotherVal = o2.getPoints();
                return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
            }
        });
        final int to = Math.min(offenseList.size(), 4);
        for (int x = 0; x < to; x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()));
            ret.append(": ");
            ret.append(offenseList.get(x).getCount());
            if (x != to - 1) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    public final void dispose() {
        if (invalidationTask != null) {
            invalidationTask.cancel(false);
        }
        invalidationTask = null;
        chr = new WeakReference<MapleCharacter>(null);
    }

    public final void start(final MapleCharacter chr) {
        this.chr = new WeakReference<MapleCharacter>(chr);
        invalidationTask = CheatTimer.getInstance().register(new InvalidationTask(), 60000);
        takingDamageSince = System.currentTimeMillis();
    }

    private final class InvalidationTask implements Runnable {

        @Override
        public final void run() {
            CheatingOffenseEntry[] offenses_copy;
            rL.lock();
            try {
                offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
            } finally {
                rL.unlock();
            }
            for (CheatingOffenseEntry offense : offenses_copy) {
                if (offense.isExpired()) {
                    expireEntry(offense);
                }
            }
            if (chr.get() == null) {
                dispose();
            }
        }
    }

    public boolean canSaveDB() {
        if ((System.currentTimeMillis() - lastSaveTime < 5 * 60 * 1000)) {
            return false;
        }
        this.lastSaveTime = System.currentTimeMillis();
        return true;
    }

    public int getlastSaveTime() {
        return (int) ((System.currentTimeMillis() - this.lastSaveTime) / 1000);
    }
    
    public boolean canLieDetector() {
        if ((this.lastLieDetectorTime + 300000 > System.currentTimeMillis()) && (this.chr.get() != null)) {
            return false;
        }
        this.lastLieDetectorTime = System.currentTimeMillis();
        return true;
    }
}
