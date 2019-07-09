package server;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleDisease;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import provider.MapleData;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.MapleCarnivalFactory.MCSkill;
import server.Timer.BuffTimer;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleExtractor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SummonMovementType;
import tools.CaltechEval;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;

public class MapleStatEffect implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private byte mastery, mhpR, mmpR, mobCount, attackCount, bulletCount, reqGuildLevel, period, expR, familiarTarget, iceGageCon, //1 = party 2 = nearby
            recipeUseCount, recipeValidDay, reqSkillLevel, slotCount, effectedOnAlly, effectedOnEnemy, type, preventslip, immortal, bs;
    private short hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, mpCon, hpCon, forceCon, bdR, damage, prop,
            ehp, emp, ewatk, ewdef, emdef, ignoreMob, dot, dotTime, criticaldamageMin, criticaldamageMax, pddR, mddR,
            asrR, er, damR, padX, madX, mesoR, thaw, selfDestruction, PVPdamage, indiePad, indieMad, fatigueChange,
            str, dex, int_, luk, strX, dexX, intX, lukX, lifeId, imhp, immp, inflation, useLevel, mpConReduce,
            indieMhp, indieMmp, indieAllStat, indieSpeed, indieJump, indieAcc, indieEva, indiePdd, indieMdd, incPVPdamage,
            mobSkill, mobSkillLevel, indieMhpR, indieMmpR, terR; //ar = accuracy rate
    private double hpR, mpR;
    private int duration, subTime, sourceid, recipe, moveTo, t, u, v, w, x, y, z, cr, itemCon, itemConNo, bulletConsume, moneyCon,
            cooldown, morphId = 0, expinc, exp, consumeOnPickup, range, price, extendPrice, charColor, interval, rewardMeso, totalprob, cosmetic;
    private boolean overTime, skill, partyBuff = true;
    private EnumMap<MapleBuffStat, Integer> statups;
    private ArrayList<Pair<Integer, Integer>> availableMap;
    private EnumMap<MonsterStatus, Integer> monsterStatus;
    private Point lt, rb;
    private int expBuff, itemup, mesoup, cashup, berserk, illusion, booster, berserk2, cp, nuffSkill, fixdamage;
    private byte level;
//  private List<Pair<Integer, Integer>> randomMorph;
    private List<MapleDisease> cureDebuffs;
    private List<Integer> petsCanConsume, familiars, randomPickup;
    private List<Triple<Integer, Integer, Integer>> rewardItem;

    public static final MapleStatEffect loadSkillEffectFromData(final MapleData source, final int skillid, final boolean overtime, final int level, final String variables) {
        return loadFromData(source, skillid, true, overtime, level, variables);
    }

    public static final MapleStatEffect loadItemEffectFromData(final MapleData source, final int itemid) {
        return loadFromData(source, itemid, false, false, 1, null);
    }

    private static final void addBuffStatPairToListIfNotZero(final EnumMap<MapleBuffStat, Integer> list, final MapleBuffStat buffstat, final Integer val) {
        if (val.intValue() != 0) {
            list.put(buffstat, val);
        }
    }

    private final static int parseEval(String path, MapleData source, int def, String variables, int level) {
        if (variables == null) {
            return MapleDataTool.getIntConvert(path, source, def);
        } else {
            final MapleData dd = source.getChildByPath(path);
            if (dd == null) {
                return def;
            }
            if (dd.getType() != MapleDataType.STRING) {
                return MapleDataTool.getIntConvert(path, source, def);
            }
            String dddd = MapleDataTool.getString(dd).replace(variables, String.valueOf(level));
            if (dddd.substring(0, 1).equals("-")) { //-30+3*x
                if (dddd.substring(1, 2).equals("u") || dddd.substring(1, 2).equals("d")) { //-u(x/2)
                    dddd = "n(" + dddd.substring(1, dddd.length()) + ")"; //n(u(x/2))
                } else {
                    dddd = "n" + dddd.substring(1, dddd.length()); //n30+3*x
                }
            } else if (dddd.substring(0, 1).equals("=")) { //lol nexon and their mistakes
                dddd = dddd.substring(1, dddd.length());
            }
            return (int) (new CaltechEval(dddd).evaluate());
        }
    }

    private static MapleStatEffect loadFromData(final MapleData source, final int sourceid, final boolean skill, final boolean overTime, final int level, final String variables) {
        final MapleStatEffect ret = new MapleStatEffect();
        ret.sourceid = sourceid;
        ret.skill = skill;
        ret.level = (byte) level;
        if (source == null) {
            return ret;
        }
        ret.duration = parseEval("time", source, -1, variables, level);
        ret.hp = (short) parseEval("hp", source, 0, variables, level);
        ret.hpR = parseEval("hpR", source, 0, variables, level) / 100.0;
        ret.mp = (short) parseEval("mp", source, 0, variables, level);
        ret.mpR = parseEval("mpR", source, 0, variables, level) / 100.0;
        ret.mhpR = (byte) parseEval("mhpR", source, 0, variables, level);
        ret.mmpR = (byte) parseEval("mmpR", source, 0, variables, level);
        ret.pddR = (short) parseEval("pddR", source, 0, variables, level);
        ret.mddR = (short) parseEval("mddR", source, 0, variables, level);
        ret.ignoreMob = (short) parseEval("ignoreMobpdpR", source, 0, variables, level);
        ret.asrR = (short) parseEval("asrR", source, 0, variables, level);
        ret.bdR = (short) parseEval("bdR", source, 0, variables, level);
        ret.damR = (short) parseEval("damR", source, 0, variables, level);
        ret.terR = (short) parseEval("terR", source, 0, variables, level);
        ret.mesoR = (short) parseEval("mesoR", source, 0, variables, level);
        ret.thaw = (short) parseEval("thaw", source, 0, variables, level);
        ret.padX = (short) parseEval("padX", source, 0, variables, level);
        ret.madX = (short) parseEval("madX", source, 0, variables, level);
        ret.dot = (short) parseEval("dot", source, 0, variables, level);
        ret.dotTime = (short) parseEval("dotTime", source, 0, variables, level);
        ret.criticaldamageMin = (short) parseEval("criticaldamageMin", source, 0, variables, level);
        ret.criticaldamageMax = (short) parseEval("criticaldamageMax", source, 0, variables, level);
        ret.mpConReduce = (short) parseEval("mpConReduce", source, 0, variables, level);
        ret.forceCon = (short) parseEval("forceCon", source, 0, variables, level);
        ret.mpCon = (short) parseEval("mpCon", source, 0, variables, level);
        ret.hpCon = (short) parseEval("hpCon", source, 0, variables, level);
        ret.prop = (short) parseEval("prop", source, 100, variables, level);
        ret.cooldown = Math.max(0, parseEval("cooltime", source, 0, variables, level));
        ret.interval = parseEval("interval", source, 0, variables, level);
        ret.expinc = parseEval("expinc", source, 0, variables, level);
        ret.exp = parseEval("exp", source, 0, variables, level);
        ret.range = parseEval("range", source, 0, variables, level);
        ret.morphId = parseEval("morph", source, 0, variables, level);
        ret.cp = parseEval("cp", source, 0, variables, level);
        ret.cosmetic = parseEval("cosmetic", source, 0, variables, level);
        ret.er = (short) parseEval("er", source, 0, variables, level);
        ret.slotCount = (byte) parseEval("slotCount", source, 0, variables, level);
        ret.preventslip = (byte) parseEval("preventslip", source, 0, variables, level);
        ret.useLevel = (short) parseEval("useLevel", source, 0, variables, level);
        ret.nuffSkill = parseEval("nuffSkill", source, 0, variables, level);
        ret.familiarTarget = (byte) (parseEval("familiarPassiveSkillTarget", source, 0, variables, level) + 1);
        ret.mobCount = (byte) parseEval("mobCount", source, 1, variables, level);
        ret.immortal = (byte) parseEval("immortal", source, 0, variables, level);
        ret.iceGageCon = (byte) parseEval("iceGageCon", source, 0, variables, level);
        ret.expR = (byte) parseEval("expR", source, 0, variables, level);
        ret.reqGuildLevel = (byte) parseEval("reqGuildLevel", source, 0, variables, level);
        ret.period = (byte) parseEval("period", source, 0, variables, level);
        ret.type = (byte) parseEval("type", source, 0, variables, level);
        ret.bs = (byte) parseEval("bs", source, 0, variables, level);
        ret.attackCount = (byte) parseEval("attackCount", source, 1, variables, level);
        ret.bulletCount = (byte) parseEval("bulletCount", source, 1, variables, level);
        int priceUnit = parseEval("priceUnit", source, 0, variables, level);
        if (priceUnit > 0) {
            ret.price = parseEval("price", source, 0, variables, level) * priceUnit;
            ret.extendPrice = parseEval("extendPrice", source, 0, variables, level) * priceUnit;
        } else {
            ret.price = 0;
            ret.extendPrice = 0;
        }

        if (ret.skill) {
            switch (sourceid) {
                case 1100002://终极剑
                case 1100003://终极斧
                case 1200002:
                case 1300002:
                case 3100001:
                case 3200001:
                case 11101002:
                case 13101002:
                case 2111007:
                case 2211007:
                case 2311007:
                case 32111010:
                case 33100009:
                case 22150004:
                case 22181004: //All Final Attack
                case 1120013:
                case 3120008:
                case 23100006:
                case 23120012:
                    ret.mobCount = 6;
                    break;
                case 35121005:
                case 35111004:
                case 35121013:
                    ret.attackCount = 6;
                    ret.bulletCount = 6;
                    break;
            }
            if (GameConstants.isNoDelaySkill(sourceid)) {
                ret.mobCount = 6;
            }
        }

        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.subTime *= 1000;
            ret.overTime = overTime || ret.isMorph() || ret.isPirateMorph() || ret.isFinalAttack() || ret.isAngel();
        }
        ret.statups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
        ret.mastery = (byte) parseEval("mastery", source, 0, variables, level);
        ret.watk = (short) parseEval("pad", source, 0, variables, level);
        ret.wdef = (short) parseEval("pdd", source, 0, variables, level);
        ret.matk = (short) parseEval("mad", source, 0, variables, level);
        ret.mdef = (short) parseEval("mdd", source, 0, variables, level);
        ret.ehp = (short) parseEval("emhp", source, 0, variables, level);
        ret.emp = (short) parseEval("emmp", source, 0, variables, level);
        ret.ewatk = (short) parseEval("epad", source, 0, variables, level);
        ret.ewdef = (short) parseEval("epdd", source, 0, variables, level);
        ret.emdef = (short) parseEval("emdd", source, 0, variables, level);
        ret.acc = (short) parseEval("acc", source, 0, variables, level);
        ret.avoid = (short) parseEval("eva", source, 0, variables, level);
        ret.speed = (short) parseEval("speed", source, 0, variables, level);
        ret.jump = (short) parseEval("jump", source, 0, variables, level);
        ret.indiePad = (short) parseEval("indiePad", source, 0, variables, level);
        ret.indieMad = (short) parseEval("indieMad", source, 0, variables, level);
        ret.indieMhp = (short) parseEval("indieMhp", source, 0, variables, level);
        ret.indieMmp = (short) parseEval("indieMmp", source, 0, variables, level);
        ret.indieSpeed = (short) parseEval("indieSpeed", source, 0, variables, level);
        ret.indieJump = (short) parseEval("indieJump", source, 0, variables, level);
        ret.indieAcc = (short) parseEval("indieAcc", source, 0, variables, level);
        ret.indieEva = (short) parseEval("indieEva", source, 0, variables, level);
        ret.indiePdd = (short) parseEval("indiePdd", source, 0, variables, level);
        ret.indieMdd = (short) parseEval("indieMdd", source, 0, variables, level);
        ret.indieAllStat = (short) parseEval("indieAllStat", source, 0, variables, level);
        ret.indieMhpR = (short) parseEval("indieMhpR", source, 0, variables, level);
        ret.indieMmpR = (short) parseEval("indieMmpR", source, 0, variables, level);
        ret.str = (short) parseEval("str", source, 0, variables, level);
        ret.dex = (short) parseEval("dex", source, 0, variables, level);
        ret.int_ = (short) parseEval("int", source, 0, variables, level);
        ret.luk = (short) parseEval("luk", source, 0, variables, level);
        ret.strX = (short) parseEval("strX", source, 0, variables, level);
        ret.dexX = (short) parseEval("dexX", source, 0, variables, level);
        ret.intX = (short) parseEval("intX", source, 0, variables, level);
        ret.lukX = (short) parseEval("lukX", source, 0, variables, level);
        ret.expBuff = parseEval("expBuff", source, 0, variables, level);
        ret.cashup = parseEval("cashBuff", source, 0, variables, level);
        ret.itemup = parseEval("itemupbyitem", source, 0, variables, level);
        ret.mesoup = parseEval("mesoupbyitem", source, 0, variables, level);
        ret.berserk = parseEval("berserk", source, 0, variables, level);
        ret.berserk2 = parseEval("berserk2", source, 0, variables, level);
        ret.booster = parseEval("booster", source, 0, variables, level);
        ret.lifeId = (short) parseEval("lifeId", source, 0, variables, level);
        ret.inflation = (short) parseEval("inflation", source, 0, variables, level);
        ret.imhp = (short) parseEval("imhp", source, 0, variables, level);
        ret.immp = (short) parseEval("immp", source, 0, variables, level);
        ret.illusion = parseEval("illusion", source, 0, variables, level);
        ret.consumeOnPickup = parseEval("consumeOnPickup", source, 0, variables, level);
        if (ret.consumeOnPickup == 1) {
            if (parseEval("party", source, 0, variables, level) > 0) {
                ret.consumeOnPickup = 2;
            }
        }
        ret.charColor = 0;
        String cColor = MapleDataTool.getString("charColor", source, null);
        if (cColor != null) {
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
        }

        ret.recipe = parseEval("recipe", source, 0, variables, level);
        ret.recipeUseCount = (byte) parseEval("recipeUseCount", source, 0, variables, level);
        ret.recipeValidDay = (byte) parseEval("recipeValidDay", source, 0, variables, level);
        ret.reqSkillLevel = (byte) parseEval("reqSkillLevel", source, 0, variables, level);

        ret.effectedOnAlly = (byte) parseEval("effectedOnAlly", source, 0, variables, level);
        ret.effectedOnEnemy = (byte) parseEval("effectedOnEnemy", source, 0, variables, level);

        List<MapleDisease> cure = new ArrayList<MapleDisease>(5);
        if (parseEval("poison", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.POISON);
        }
        if (parseEval("seal", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.SEAL);
        }
        if (parseEval("darkness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.DARKNESS);
        }
        if (parseEval("weakness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.WEAKEN);
        }
        if (parseEval("curse", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.CURSE);
        }
        ret.cureDebuffs = cure;

        ret.petsCanConsume = new ArrayList<Integer>();
        for (int i = 0; true; i++) {
            final int dd = parseEval(String.valueOf(i), source, 0, variables, level);
            if (dd > 0) {
                ret.petsCanConsume.add(dd);
            } else {
                break;
            }
        }
        final MapleData mdd = source.getChildByPath("0");
        if (mdd != null && mdd.getChildren().size() > 0) {
            ret.mobSkill = (short) parseEval("mobSkill", mdd, 0, variables, level);
            ret.mobSkillLevel = (short) parseEval("level", mdd, 0, variables, level);
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
        }
        final MapleData pd = source.getChildByPath("randomPickup");
        if (pd != null) {
            ret.randomPickup = new ArrayList<Integer>();
            for (MapleData p : pd) {
                ret.randomPickup.add(MapleDataTool.getInt(p));
            }
        }
        final MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
        }

        final MapleData ltc = source.getChildByPath("con");
        if (ltc != null) {
            ret.availableMap = new ArrayList<Pair<Integer, Integer>>();
            for (MapleData ltb : ltc) {
                ret.availableMap.add(new Pair<Integer, Integer>(MapleDataTool.getInt("sMap", ltb, 0), MapleDataTool.getInt("eMap", ltb, 999999999)));
            }
        }
        final MapleData ltb = source.getChildByPath("familiar");
        if (ltb != null) {
            ret.fatigueChange = (short) (parseEval("incFatigue", ltb, 0, variables, level) - parseEval("decFatigue", ltb, 0, variables, level));
            ret.familiarTarget = (byte) parseEval("target", ltb, 0, variables, level);
            final MapleData lta = ltb.getChildByPath("targetList");
            if (lta != null) {
                ret.familiars = new ArrayList<Integer>();
                for (MapleData ltz : lta) {
                    ret.familiars.add(MapleDataTool.getInt(ltz, 0));
                }
            }
        } else {
            ret.fatigueChange = 0;
        }
        int totalprob = 0;
        final MapleData lta = source.getChildByPath("reward");
        if (lta != null) {
            ret.rewardMeso = parseEval("meso", lta, 0, variables, level);
            final MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
                ret.rewardItem = new ArrayList<Triple<Integer, Integer, Integer>>();
                for (MapleData lty : ltz) {
                    ret.rewardItem.add(new Triple<Integer, Integer, Integer>(MapleDataTool.getInt("id", lty, 0), MapleDataTool.getInt("count", lty, 0), MapleDataTool.getInt("prop", lty, 0)));
                    totalprob += MapleDataTool.getInt("prob", lty, 0);
                }
            }
        } else {
            ret.rewardMeso = 0;
        }
        ret.totalprob = totalprob;
        ret.cr = parseEval("cr", source, 0, variables, level);
        ret.t = parseEval("t", source, 0, variables, level);
        ret.u = parseEval("u", source, 0, variables, level);
        ret.v = parseEval("v", source, 0, variables, level);
        ret.w = parseEval("w", source, 0, variables, level);
        ret.x = parseEval("x", source, 0, variables, level);
        ret.y = parseEval("y", source, 0, variables, level);
        ret.z = parseEval("z", source, 0, variables, level);
        ret.damage = (short) parseEval("damage", source, 100, variables, level);
        ret.PVPdamage = (short) parseEval("PVPdamage", source, 0, variables, level);
        ret.incPVPdamage = (short) parseEval("incPVPDamage", source, 0, variables, level);
        ret.selfDestruction = (short) parseEval("selfDestruction", source, 0, variables, level);
        ret.bulletConsume = parseEval("bulletConsume", source, 0, variables, level);
        ret.moneyCon = parseEval("moneyCon", source, 0, variables, level);

        ret.itemCon = parseEval("itemCon", source, 0, variables, level);
        ret.itemConNo = parseEval("itemConNo", source, 0, variables, level);
        ret.moveTo = parseEval("moveTo", source, -1, variables, level);
        ret.fixdamage = parseEval("fixdamage", source, 0, variables, level);
        ret.monsterStatus = new EnumMap<MonsterStatus, Integer>(MonsterStatus.class);
        if (ret.overTime && ret.getSummonMovementType() == null && !ret.isEnergyCharge()) {
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.SPEED, sourceid == 32120001 || sourceid == 32101003 ? Integer.valueOf(ret.x) : Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXHP, (int) ret.mhpR);
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXMP, (int) ret.mmpR);
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_LOSS_GUARD, Integer.valueOf(ret.thaw));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.EXPRATE, Integer.valueOf(ret.expBuff)); // EXP
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACASH_RATE, Integer.valueOf(ret.cashup)); // custom
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DROP_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.itemup))); // defaults to 2x
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MESO_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.mesoup))); // defaults to 2x
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BERSERK_FURY, Integer.valueOf(ret.berserk2));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ILLUSION, Integer.valueOf(ret.illusion));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PYRAMID_PQ, Integer.valueOf(ret.berserk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXHP, Integer.valueOf(ret.ehp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXMP, Integer.valueOf(ret.emp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WATK, Integer.valueOf(ret.ewatk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WDEF, Integer.valueOf(ret.ewdef));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MDEF, Integer.valueOf(ret.emdef));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.GIANT_POTION, Integer.valueOf(ret.inflation));
            /*addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.STR, Integer.valueOf(ret.str));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DEX, Integer.valueOf(ret.dex));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INT, Integer.valueOf(ret.int_));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.LUK, Integer.valueOf(ret.luk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ATK, Integer.valueOf(ret.indiePad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_MATK, Integer.valueOf(ret.indieMad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, Integer.valueOf(ret.imhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, Integer.valueOf(ret.immp)); //same one? lol
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, Integer.valueOf(ret.indieMhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, Integer.valueOf(ret.indieMmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_DAMAGE, Integer.valueOf(ret.incPVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_JUMP, Integer.valueOf(ret.indieJump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_SPEED, Integer.valueOf(ret.indieSpeed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ACC, Integer.valueOf(ret.indieAcc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_AVOID, Integer.valueOf(ret.indieEva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_STAT, Integer.valueOf(ret.indieAllStat));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_ATTACK, Integer.valueOf(ret.PVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INVINCIBILITY, Integer.valueOf(ret.immortal));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.NO_SLIP, Integer.valueOf(ret.preventslip));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.FAMILIAR_SHADOW, Integer.valueOf(ret.charColor > 0 ? 1 : 0));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST_PERCENT, Integer.valueOf(ret.indieMhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST_PERCENT, Integer.valueOf(ret.indieMmpR));*/
            if (ret.isPirateMorph()) { //HACK: add stance :D and also this buffstat has to be the first one..
                ret.statups.put(MapleBuffStat.STANCE, 100); //100% :D:D:D
            }
        }
        if (ret.skill) { // hack because we can't get from the datafile...
            switch (sourceid) {
                case 2001002: // magic guard
                case 12001001:
                case 22111001:
                    ret.statups.put(MapleBuffStat.MAGIC_GUARD, ret.x);
                    break;
                case 2301003: // invincible
                    ret.statups.put(MapleBuffStat.INVINCIBLE, ret.x);
                    break;
                case 35120000:
                case 35001002:
                    ret.duration = 2100000000;
                    break;
                case 9101004:
                case 9001004: // hide
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.x);
                    break;
                case 13101006: // Wind Walk
                    ret.statups.put(MapleBuffStat.WIND_WALK, ret.x);
                    break;
                case 4330001:
                    ret.statups.put(MapleBuffStat.DARKSIGHT, (int) ret.level);
                    break;
                case 4001003: // darksight
                case 14001003: // cygnus ds
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.x);
                    break;
                case 4211003: // pickpocket
                    ret.statups.put(MapleBuffStat.PICKPOCKET, ret.x);
                    break;
                case 4211005: // mesoguard
                    ret.statups.put(MapleBuffStat.MESOGUARD, ret.x);
                    break;
                case 4111001: // mesoup
                    ret.statups.put(MapleBuffStat.MESOUP, ret.x);
                    break;
                case 4111002: // shadowpartner
                case 14111000: // cygnus
                case 4331002:
                    ret.statups.put(MapleBuffStat.SHADOWPARTNER, ret.x);
                    break;
                case 4211008:
                    ret.statups.put(MapleBuffStat.SHADOWPARTNER, (int) ret.level);
                    break;
                case 11101002: // All Final attack
                case 13101002:
                    ret.statups.put(MapleBuffStat.FINALATTACK, ret.x);
                    break;
                case 22161004:
                    ret.statups.put(MapleBuffStat.ONYX_SHROUD, ret.x);
                    break;
                case 3101004: // soul arrow
                case 3201004:
                case 2311002: // mystic door - hacked buff icon
                case 35101005:
                case 13101003:
                case 33101003:
                    ret.statups.put(MapleBuffStat.SOULARROW, ret.x);
                    break;
                case 2321010:
                case 2221009:
                case 2121009:
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.BUFF_MASTERY, ret.x);
                    break;
                case 2320011:
                case 2220010:
                case 2120010:
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.ARCANE_AIM, ret.x);
                    break;
                case 1211006: //寒冰之棍
                case 1211003: //烈焰之劍
                case 1211004: //烈焰之棍
                case 1211005: //寒冰之劍
                case 1211007: //雷鳴之劍
                case 1211008: //雷鳴之棍
                case 1221003: //聖靈之劍
                case 1221004: //聖靈之棍
                case 11111007:
                case 21101006:
                case 21111005:
                case 15101006:
                    ret.statups.put(MapleBuffStat.WK_CHARGE, ret.x);
                    break;
                case 2111008:
                case 2211008:
                case 12101005:
                case 22121001: // Elemental Reset
                    ret.statups.put(MapleBuffStat.ELEMENT_RESET, ret.x);
                    break;
                case 3111000:
                case 3121008:
                case 13111001:
                    ret.statups.put(MapleBuffStat.CONCENTRATE, ret.x);
                    break;
                case 5110001: // Energy Charge
                case 15100004:
                    ret.statups.put(MapleBuffStat.ENERGY_CHARGE, 0);
                    break;
                case 1101004://快速剑
                case 1101005://快速斧
                case 1201004://快速剑
                case 1201005://快速钝器
                case 1301004://快速枪
                case 1301005://快速矛    
                case 3101002://快速弓
                case 3201002:
                case 4101003:
                case 4201002:
                case 23101002:
                case 31001001:
                case 2111005: // spell booster, do these work the same?
                case 2211005:
                case 5101006:
                case 5201003:
                case 11101001:
                case 12101004:
                case 13101001:
                case 14101002:
                case 15101002:
                case 22141002: // Magic Booster
                case 4301002:
                case 32101005:
                case 33001003:
                case 35101006:
                case 5301002:

                    ret.statups.put(MapleBuffStat.BOOSTER, ret.x);
                    break;
                case 21001003: // Aran - Pole Arm Booster
                    ret.statups.put(MapleBuffStat.BOOSTER, -ret.y);
                    break;
                case 35111013:
                case 5111007:
                case 5211007:
                case 5311005:
                case 5320007:
                    ret.statups.put(MapleBuffStat.DICE_ROLL, 0);
                    break;
                case 5120011:
                case 5220012:
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.damR); //i think
                    break;
                case 5121009:
                case 15111005:
                    ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.x);
                    break;
                case 4321000: //tornado spin uses same buffstats
                    ret.duration = 1000;
                    ret.statups.put(MapleBuffStat.DASH_SPEED, 100 + ret.x);
                    ret.statups.put(MapleBuffStat.DASH_JUMP, ret.y); //always 0 but its there
                    break;
                case 5001005: // Dash
                case 15001003:
                    ret.statups.put(MapleBuffStat.DASH_SPEED, ret.x);
                    ret.statups.put(MapleBuffStat.DASH_JUMP, ret.y);
                    break;
                case 1101007: // pguard
                case 1201007:
                    ret.statups.put(MapleBuffStat.POWERGUARD, ret.x);
                    break;
                case 32111004: //conversion
                    ret.statups.put(MapleBuffStat.CONVERSION, ret.x);
                    break;
                case 1301007: // hyper body
                case 9001008:
                case 9101008:
                    ret.statups.put(MapleBuffStat.MAXHP, ret.x);
                    ret.statups.put(MapleBuffStat.MAXMP, ret.x);
                    break;
                case 1111002: // combo
                case 11111001: // combo
                    ret.statups.put(MapleBuffStat.COMBO, 0);
                    break;
                case 21120007: //combo barrier
                    ret.statups.put(MapleBuffStat.COMBO_BARRIER, ret.x);
                    break;
                case 5211006: // Homing Beacon
                case 5220011: // Bullseye
                case 22151002: //killer wings
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.HOMING_BEACON, ret.x);
                    break;
                case 4341007:
                    ret.statups.put(MapleBuffStat.STANCE, (int) ret.prop);
                    break;
                case 21111009: //combo recharge
                case 1311006: //dragon roar
                case 1311005: //NOT A BUFF - Sacrifice
                    ret.hpR = -ret.x / 100.0;
                    break;
                case 1211010: //NOT A BUFF - HP Recover
                    ret.hpR = ret.x / 100.0;
                    break;
                case 4341002:
                    ret.duration = 60 * 1000;
                    ret.hpR = -ret.x / 100.0;
                    ret.statups.put(MapleBuffStat.FINAL_CUT, ret.y);
                    break;
                case 2111007:
                case 2211007:
                case 2311007:
                case 32111010:
                    ret.mpCon = (short) ret.y;
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.TELEPORT_MASTERY, ret.x);
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case 31121005:
                    ret.statups.put(MapleBuffStat.DARK_METAMORPHOSIS, 6);
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.damR);
                case 4331003:
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.OWL_SPIRIT, ret.y);
                    break;
                case 1311008: // dragon blood
                    ret.statups.put(MapleBuffStat.DRAGONBLOOD, ret.x);
                    break;
                case 5321005:
                case 1121000: // maple warrior, all classes
                case 1221000:
                case 1321000:
                case 2121000:
                case 2221000:
                case 2321000:
                case 3121000:
                case 3221000:
                case 4121000:
                case 4221000:
                case 5121000:
                case 5221000:
                case 21121000: // Aran - Maple Warrior
                case 22171000:
                case 4341000:
                case 32121007:
                case 33121007:
                case 35121007:
                case 23121005:
                case 31121004:
                    ret.statups.put(MapleBuffStat.MAPLE_WARRIOR, ret.x);
                    break;
                case 15111006: //spark
                    ret.statups.put(MapleBuffStat.SPARK, ret.x);
                    break;
                case 3121002: // sharp eyes bow master
                case 3221002: // sharp eyes marksmen
                case 33121004:
                    ret.statups.put(MapleBuffStat.SHARP_EYES, (ret.x << 8) + ret.criticaldamageMax);
                    break;
                case 22151003: //magic resistance
                    ret.statups.put(MapleBuffStat.MAGIC_RESISTANCE, ret.x);
                    break;
                case 2000007:
                case 12000006:
                case 22000002:
                case 32000012:
                    ret.statups.put(MapleBuffStat.ELEMENT_WEAKEN, ret.x);
                    break;
                case 21101003: // Body Pressure
                    ret.statups.put(MapleBuffStat.BODY_PRESSURE, ret.x);
                    break;
                case 21000000: // Aran Combo
                    ret.statups.put(MapleBuffStat.ARAN_COMBO, 100);
                    break;
                case 23101003:
                    ret.statups.put(MapleBuffStat.SPIRIT_SURGE, ret.x);
                    break;
                case 21100005: // Combo Drain
                case 32101004:
                case 31121002:
                    ret.statups.put(MapleBuffStat.COMBO_DRAIN, ret.x);
                    break;
                case 21111001: // Smart Knockback
                    ret.statups.put(MapleBuffStat.SMART_KNOCKBACK, ret.x);
                    break;
                case 23121004://TODO LEGEND
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, (int) ret.damR);
                    break;
                case 1211009:
                case 1111007:
                case 1311007: //magic crash
                    ret.monsterStatus.put(MonsterStatus.MAGIC_CRASH, Integer.valueOf(1));
                    break;
                case 1220013:
                    ret.statups.put(MapleBuffStat.DIVINE_SHIELD, ret.x + 1);
                    break;
                case 1211011:
                    ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.x);
                    break;
                case 23111005:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, (int) ret.terR);
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, (int) ret.terR);
                    ret.statups.put(MapleBuffStat.WATER_SHIELD, ret.x);
                    break;
                case 22131001: //magic shield
                    ret.statups.put(MapleBuffStat.MAGIC_SHIELD, ret.x);
                    break;
                case 22181003: //soul stone
                    ret.statups.put(MapleBuffStat.SOUL_STONE, 1);
                    break;
                case 32121003: //twister
                    ret.statups.put(MapleBuffStat.TORNADO, ret.x);
                    break;
                case 2311009: //holy magic
                    ret.statups.put(MapleBuffStat.HOLY_MAGIC_SHELL, ret.x);
                    ret.cooldown = ret.y;
                    ret.hpR = ret.z / 100.0;
                    break;
                case 32111005: //body boost
                    ret.duration = 60000;
                    ret.statups.put(MapleBuffStat.BODY_BOOST, (int) ret.level); //lots of variables
                    break;
                case 22131002:
                case 22141003: // Slow
                    ret.statups.put(MapleBuffStat.SLOW, ret.x);
                    break;
                case 22181000: // idk if this fix, whatever
                    ret.statups.put(MapleBuffStat.MATK, (int) ret.matk);
                case 4001002: // disorder
                case 14001002: // cygnus disorder
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.y);
                    break;
                case 5221009: // Mind Control
                    ret.monsterStatus.put(MonsterStatus.HYPNOTIZE, 1);
                    break;
                case 4341003: // Monster Bomb
                    ret.monsterStatus.put(MonsterStatus.MONSTER_BOMB, (int) ret.damage);
                    break;
                case 1201006: // threaten
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.x);
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.z);
                    break;
                case 22141001:
                case 1211002: // charged blow
                case 1111008: // shout
                case 4211002: // assaulter
                case 3101005: // arrow bomb
                case 1111005: // coma: sword
                case 4221007: // boomerang step
                case 5101002: // Backspin Blow
                case 5101003: // Double Uppercut
                case 5121004: // Demolition
                case 5121005: // Snatch
                case 5121007: // Barrage
                case 5201004: // pirate blank shot
                case 4121008: // Ninja Storm
                case 22151001:
                case 4201004: //steal, new
                case 33101001:
                case 33101002:
                case 32101001:
                case 32111011:
                case 32121004:
                case 33111002:
                case 33121002:
                case 35101003:
                case 35111015:
                case 5111002: //energy blast
                case 15101005:
                case 4331005:
                case 1121001: //magnet
                case 1221001:
                case 1321001:
                case 9001020:
                case 31111001:
                case 31101002:
                case 9101020:
                //case 2211003:
                //case 2311004:
                case 3120010:
                case 22181001:
                case 21110006:
                case 22131000:
                case 5301001:
                case 5311001:
                case 5311002:
                //case 2221006:
                case 5310008:
                    ret.monsterStatus.put(MonsterStatus.STUN, 1);
                    break;
                case 90001004:
                case 4321002:
                case 1111003:
                case 11111002:
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.x);
                    break;
                case 4221003:
                case 4121003:
                case 33121005:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.x);
                    break;
                case 31121003:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.w);
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.x);
                    ret.monsterStatus.put(MonsterStatus.MATK, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.x);
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.x);
                    break;
                case 23121002: //not sure if negative
                    ret.monsterStatus.put(MonsterStatus.WDEF, -ret.x);
                    break;
                case 2201004: // cold beam
                //case 2221003:
                case 2211002: // ice strike
                case 3211003: // blizzard
                case 2211006: // il elemental compo
                case 2221007: // Blizzard
                case 5211005: // Ice Splitter
                case 2121006: // Paralyze
                case 21120006: // Tempest
                case 22121000:
                case 90001006:
                case 2221001:
                    ret.monsterStatus.put(MonsterStatus.FREEZE, 1);
                    //ret.duration *= 2; // freezing skills are a little strange
                    break;
                case 2101003: // fp slow
                case 2201003: // il slow
                case 12101001:
                case 90001002:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    break;
                case 5011002:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.z);
                    break;
                case 1121010: //enrage
                    ret.statups.put(MapleBuffStat.ENRAGE, Integer.valueOf(ret.watk));
                    break;
                case 23111002: //TODO LEGEND: damage increase?
                case 22161002: //phantom imprint
                    ret.monsterStatus.put(MonsterStatus.IMPRINT, ret.x);
                    break;
                case 90001003:
                    ret.monsterStatus.put(MonsterStatus.POISON, 1);
                    break;
                case 2101005: // 毒雾术
                case 2111006: // fp elemental compo
                case 2121003: // ice demon
                case 2221003: // fire demon
                case 5211004:
                    ret.monsterStatus.put(MonsterStatus.POISON, 1);
                    break;
                case 4121004: // Ninja ambush
                case 4221004:
                    ret.monsterStatus.put(MonsterStatus.NINJA_AMBUSH, (int) ret.damage);
                    break;
                case 2311005:
                    ret.monsterStatus.put(MonsterStatus.DOOM, 1);
                    break;
                case 32111006:
                    ret.statups.put(MapleBuffStat.REAPER, 1);
                    break;
                case 35121003:
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    break;
                case 35111001:
                case 35111010:
                case 35111009:
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.PUPPET, 1);
                    break;
                case 4341006:
                case 3120012:
                case 3220012:
                case 3111002: // puppet ranger
                case 3211002: // puppet sniper
                case 13111004: // puppet cygnus
                case 5211001: // Pirate octopus summon
                case 5220002: // wrath of the octopi
                case 33111003:
                case 5321003:
                    ret.statups.put(MapleBuffStat.PUPPET, 1);
                    break;
                case 3120006:
                case 3220005:
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, (int) ret.terR);
                    ret.statups.put(MapleBuffStat.SPIRIT_LINK, 1);
                    break;
                case 2121005: // elquines
                case 3201007:
                case 3101007:
                case 3211005: // golden eagle
                case 3111005: // golden hawk
                case 33111005:
                case 35111002:
                case 23111008:
                case 23111009:
                case 23111010:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case 3221005: // frostprey
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case 35111005:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.y);
                    break;
                case 1321007: // Beholder
                case 3121006: // phoenix
                case 2311006:
                case 2221005: // ifrit
                case 2321003: // bahamut
                case 5211002: // Pirate bird summon
                case 11001004:
                case 12001004:
                case 12111004: // Itrit
                case 13001004:
                case 14001005:
                case 15001004:
                case 35111011:
                case 35121009:
                case 35121011:
                case 33101008: //summon - its raining mines
                case 4111007: //dark flare
                case 4211007: //dark flare
                case 5321004:
                    ret.statups.put(MapleBuffStat.SUMMON, 1);
                    break;
                case 35121010:
                    ret.duration = 60000;
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.x);
                    break;
                case 2311003: // hs
                case 9001002: // GM hs
                case 9101002:
                    ret.statups.put(MapleBuffStat.HOLY_SYMBOL, ret.x);
                    break;
                case 80001034: //virtue
                case 80001035: //virtue
                case 80001036: //virtue
                    ret.statups.put(MapleBuffStat.VIRTUE_EFFECT, 1);
                    break;
                case 2211004: // il seal
                case 2111004: // fp seal
                case 12111002: // cygnus seal
                case 90001005:
                    ret.monsterStatus.put(MonsterStatus.SEAL, 1);
                    break;
                case 4111003: // shadow web
                case 14111001:
                    ret.monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case 4121006: // spirit claw
                    ret.statups.put(MapleBuffStat.SPIRIT_CLAW, 0);
                    break;
                case 2121004:
                case 2221004:
                case 2321004: // Infinity
                    //ret.hpR = ret.y / 100.0;
                    //ret.mpR = ret.y / 100.0;
                    ret.statups.put(MapleBuffStat.INFINITY, ret.x);
                    break;
                case 22181004:
                    ret.statups.put(MapleBuffStat.ONYX_WILL, (int) ret.damage); //is this the right order
                    ret.statups.put(MapleBuffStat.STANCE, (int) ret.prop);
                    break;
                case 1121002:
                case 1221002:
                case 1321002:
                case 5321010:
                case 21121003: // freezing posture
                case 32111014: // bam stance
                    ret.statups.put(MapleBuffStat.STANCE, (int) ret.prop);
                    break;
                case 2121002: // mana reflection
                case 2221002:
                case 2321002:
                    ret.statups.put(MapleBuffStat.MANA_REFLECTION, 1);
                    break;
                case 2321005: // holy shield, TODO JUMP
                    ret.statups.put(MapleBuffStat.HOLY_SHIELD, ret.x);
                    break;
                case 3121007: // Hamstring
                    ret.statups.put(MapleBuffStat.HAMSTRING, ret.x);
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    break;
                case 3221006: // Blind
                case 33111004:
                    ret.statups.put(MapleBuffStat.BLIND, ret.x);
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.x);
                    break;
                case 33111007: //feline berserk
                    ret.statups.put(MapleBuffStat.SPEED, ret.z);
                    ret.statups.put(MapleBuffStat.ATTACK_BUFF, ret.y);
                    ret.statups.put(MapleBuffStat.FELINE_BERSERK, ret.x);
                    break;
                case 2301004:
                case 9001003:
                case 9101003:
                    ret.statups.put(MapleBuffStat.BLESS, (int) ret.level);
                    break;
                case 32120000:
                    ret.dot = ret.damage;
                    ret.dotTime = 3;
                case 32001003: //dark aura
                case 32110007:
                    ret.duration = (sourceid == 32110007 ? 60000 : 2100000000);
                    ret.statups.put(MapleBuffStat.AURA, (int) ret.level);
                    ret.statups.put(MapleBuffStat.DARK_AURA, ret.x);
                    break;
                case 32111012: //blue aura
                case 32110000:
                case 32110008:
                    ret.duration = (sourceid == 32110008 ? 60000 : 2100000000);
                    ret.statups.put(MapleBuffStat.AURA, (int) ret.level);
                    ret.statups.put(MapleBuffStat.BLUE_AURA, (int) ret.level);
                    break;
                case 32120001:
                    ret.monsterStatus.put(MonsterStatus.SPEED, (int) ret.speed);
                case 32101003: //yellow aura
                case 32110009:
                    ret.duration = (sourceid == 32110009 ? 60000 : 2100000000);
                    ret.statups.put(MapleBuffStat.AURA, (int) ret.level);
                    ret.statups.put(MapleBuffStat.YELLOW_AURA, (int) ret.level);
                    break;
                case 33101004: //it's raining mines
                    ret.statups.put(MapleBuffStat.RAINING_MINES, ret.x); //x?
                    break;
                case 35101007: //perfect armor
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.x);
                    break;
                case 31101003:
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.y);
                    break;
                case 35121006: //satellite safety
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_PROC, ret.x);
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_ABSORB, ret.y);
                    break;
                case 80001040:
                case 20021110:
                    ret.moveTo = ret.x;
                    break;
                case 80001089: // Soaring
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.SOARING, 1);
                    break;
                case 35001001: //flame
                case 35101009:
                    ret.duration = 1000;
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case 35121013:
                case 35111004: //siege
                    ret.duration = 10000;
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case 35121005: //missile
                    ret.duration = 2100000000;
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, (int) level); //ya wtf
                    break;
                case 10001075: // Cygnus Echo
                    ret.statups.put(MapleBuffStat.ECHO_OF_HERO, ret.x);
                    break;
                case 31121007: // boundless rage
                    ret.statups.put(MapleBuffStat.BOUNDLESS_RAGE, ret.x); // for now, i'll use this just for the buff icon?
                    break;
                case 31111004: // black hearted strength					
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.y);
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.z);
                    ret.statups.put(MapleBuffStat.DEFENCE_BOOST_R, ret.x);
                    break;
                default:
                    break;
            }
            if (GameConstants.isBeginnerJob(sourceid / 10000)) {
                switch (sourceid % 10000) {  //angelic blessing: HACK, we're actually supposed to use the passives for atk/matk buff
                    case 1087:
                        ret.duration = 2100000000;
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 10);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 10);
                        break;
                    case 1085:
                    case 1090:
                        ret.duration = 2100000000;
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 5);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 5);
                        break;
                    case 180:
                    case 1179:
                        ret.duration = 2100000000;
                        ret.statups.put(MapleBuffStat.ANGEL_ATK, 12);
                        ret.statups.put(MapleBuffStat.ANGEL_MATK, 12);
                        break;
                    case 1105:
                        ret.statups.put(MapleBuffStat.ICE_SKILL, 1);
                        ret.duration = 2100000000;
                        break;
                    case 93:
                        ret.statups.put(MapleBuffStat.HIDDEN_POTENTIAL, 1);
                        break;
                    case 8001:
                        ret.statups.put(MapleBuffStat.SOULARROW, ret.x);
                        break;
                    case 1005: // Echo of Hero
                        ret.statups.put(MapleBuffStat.ECHO_OF_HERO, ret.x);
                        break;
                    case 1011: // Berserk fury
                        ret.statups.put(MapleBuffStat.BERSERK_FURY, ret.x);
                        break;
                    case 1010:
                        ret.statups.put(MapleBuffStat.DIVINE_BODY, 1);
                        break;
                    case 1001:
                        if (sourceid / 10000 == 3001 || sourceid / 10000 == 3000) { //resistance is diff
                            ret.statups.put(MapleBuffStat.INFILTRATE, ret.x);
                        } else {
                            ret.statups.put(MapleBuffStat.RECOVERY, ret.x);
                        }
                        break;
                    case 8003:
                        ret.statups.put(MapleBuffStat.MAXHP, ret.x);
                        ret.statups.put(MapleBuffStat.MAXMP, ret.x);
                        break;
                    case 8004:
                        ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.x);
                        break;
                    case 8005:
                        ret.statups.put(MapleBuffStat.HOLY_SHIELD, 1);
                        break;
                    case 8006:
                        ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.x);
                        break;
                    case 103:
                        ret.monsterStatus.put(MonsterStatus.STUN, 1);
                        break;
                    case 99:
                    case 104:
                        ret.monsterStatus.put(MonsterStatus.FREEZE, 1);
                        ret.duration *= 2; // freezing skills are a little strange
                        break;
                    case 8002:
                        ret.statups.put(MapleBuffStat.SHARP_EYES, (ret.x << 8) + ret.criticaldamageMax);
                        break;
                    case 1026: // Soaring
                    case 1142: // Soaring
                        ret.duration = 2100000000;
                        ret.statups.put(MapleBuffStat.SOARING, 1);
                        break;
                }
            }
        } else {
            switch (sourceid) {
                case 2022746: //angel bless
                case 2022747: //d.angel bless
                    ret.statups.clear(); //no atk/matk
                    ret.statups.put(MapleBuffStat.PYRAMID_PQ, 1); //ITEM_EFFECT buff
                    break;
            }
        }
        //if (ret.isPoison()) {
        //    ret.monsterStatus.put(MonsterStatus.POISON, 1);
        //}
        if (ret.isMorph() || ret.isPirateMorph()) {
            ret.statups.put(MapleBuffStat.MORPH, ret.getMorph());
        }
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public final void applyPassive(final MapleCharacter applyto, final MapleMapObject obj) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case 2100000:
                case 2200000:
                case 2300000:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    final MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.getStats().isBoss()) {
                        final int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp, applyto);
                            applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1, applyto.getLevel(), level));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, applyto.getLevel(), level), false);
                        }
                    }
                    break;
            }
        }
    }

    public final boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, duration);
    }

    public final boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, duration);
    }

    public final boolean applyTo(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final Point pos, int newDuration) {
        if (isHeal() && (applyfrom.getMapId() == 749040100 || applyto.getMapId() == 749040100)) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false; //z
        } else if ((isSoaring_Mount() && applyfrom.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null) || (isSoaring_Normal() && !applyfrom.getMap().canSoar())) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        } else if (sourceid == 4341006 && applyfrom.getBuffedValue(MapleBuffStat.SHADOWPARTNER) == null) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        } else if (sourceid == 33101008 && (applyfrom.getBuffedValue(MapleBuffStat.RAINING_MINES) == null || applyfrom.getBuffedValue(MapleBuffStat.SUMMON) != null || !applyfrom.canSummon())) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        } else if (isShadow() && applyfrom.getJob() / 100 % 10 != 4) { //pirate/shadow = dc
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        } else if (sourceid == 33101004 && applyfrom.getMap().isTown()) {
            applyfrom.dropMessage(5, "You may not use this skill in towns.");
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);

        final PlayerStats stat = applyto.getStat();
        if (primary) {
            if (itemConNo != 0 && !applyto.isClone() && !applyto.inPVP()) {
                if (!applyto.haveItem(itemCon, itemConNo, false, true)) {
                    applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                    return false;
                }
                MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        } else if (!primary && isResurrection()) {
            hpchange = stat.getMaxHp();
            applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
        }
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.dispelDebuffs();
        } else if (cureDebuffs.size() > 0) {
            for (final MapleDisease debuff : cureDebuffs) {
                applyfrom.dispelDebuff(debuff);
            }
        } else if (isMPRecovery()) {
            final int toDecreaseHP = ((stat.getMaxHp() / 100) * 10);
            if (stat.getHp() > toDecreaseHP) {
                hpchange += -toDecreaseHP; // -10% of max HP
                mpchange += ((toDecreaseHP / 100) * getY());
            } else {
                hpchange = stat.getHp() == 1 ? 0 : stat.getHp() - 1;
            }
        }
        final Map<MapleStat, Integer> hpmpupdate = new EnumMap<>(MapleStat.class);
        if (hpchange != 0) {
            if (hpchange < 0 && (-hpchange) > stat.getHp() && !applyto.hasDisease(MapleDisease.ZOMBIFY)) {
                applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                return false;
            }
            stat.setHp(stat.getHp() + hpchange, applyto);
        }
        if (mpchange != 0) {
            if (mpchange < 0 && (-mpchange) > stat.getMp()) {
                applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                return false;
            }
            //short converting needs math.min cuz of overflow
            stat.setMp(stat.getMp() + mpchange, applyto);

            hpmpupdate.put(MapleStat.MP, Integer.valueOf(stat.getMp()));
        }
        hpmpupdate.put(MapleStat.HP, Integer.valueOf(stat.getHp()));

        applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, applyto.getJob()));

        if (expinc != 0) {
            applyto.gainExp(expinc, true, true, false);
            //applyto.getClient().getSession().write(MaplePacketCreator.showSpecialEffect(0x09));
        } else if (sourceid / 10000 == 238) {
            applyto.getMonsterBook().addCard(applyto.getClient(), sourceid);
        } else if (isReturnScroll()) {
            applyReturnScroll(applyto);
        } else if (useLevel > 0 && !skill) {
            applyto.setExtractor(new MapleExtractor(applyto, sourceid, useLevel * 50, 1440)); //no clue about time left
            applyto.getMap().spawnExtractor(applyto.getExtractor());
        } else if (isMistEruption()) {
            int i = y;
            for (MapleMist m : applyto.getMap().getAllMistsThreadsafe()) {
                if (m.getOwnerId() == applyto.getId() && m.getSourceSkill().getId() == 2111003) {
                    if (m.getSchedule() != null) {
                        m.getSchedule().cancel(false);
                        m.setSchedule(null);
                    }
                    if (m.getPoisonSchedule() != null) {
                        m.getPoisonSchedule().cancel(false);
                        m.setPoisonSchedule(null);
                    }
                    applyto.getMap().broadcastMessage(MaplePacketCreator.removeMist(m.getObjectId(), true));
                    applyto.getMap().removeMapObject(m);

                    i--;
                    if (i <= 0) {
                        break;
                    }
                }
            }
        } else if (cosmetic > 0) {
            if (cosmetic >= 30000) {
                applyto.setHair(cosmetic);
                applyto.updateSingleStat(MapleStat.HAIR, cosmetic);
            } else if (cosmetic >= 20000) {
                applyto.setFace(cosmetic);
                applyto.updateSingleStat(MapleStat.FACE, cosmetic);
            } else if (cosmetic < 100) {
                applyto.setSkinColor((byte) cosmetic);
                applyto.updateSingleStat(MapleStat.SKIN, cosmetic);
            }
            applyto.equipChanged();
        } else if (bs > 0) {
            if (!applyto.inPVP()) {
                return false;
            }
            final int x = Integer.parseInt(applyto.getEventInstance().getProperty(String.valueOf(applyto.getId())));
            applyto.getEventInstance().setProperty(String.valueOf(applyto.getId()), String.valueOf(x + bs));
            applyto.getClient().getSession().write(MaplePacketCreator.getPVPScore(x + bs, false));
        } else if (iceGageCon > 0) {
            if (!applyto.inPVP()) {
                return false;
            }
            final int x = Integer.parseInt(applyto.getEventInstance().getProperty("icegage"));
            if (x < iceGageCon) {
                return false;
            }
            applyto.getEventInstance().setProperty("icegage", String.valueOf(x - iceGageCon));
            applyto.getClient().getSession().write(MaplePacketCreator.getPVPIceGage(x - iceGageCon));
            applyto.applyIceGage(x - iceGageCon);
        } else if (recipe > 0) {
            if (applyto.getSkillLevel(recipe) > 0 || applyto.getProfessionLevel((recipe / 10000) * 10000) < reqSkillLevel) {
                return false;
            }
            applyto.changeSkillLevel(SkillFactory.getCraft(recipe), Integer.MAX_VALUE, recipeUseCount, (long) (recipeValidDay > 0 ? (System.currentTimeMillis() + recipeValidDay * 24L * 60 * 60 * 1000) : -1L));
        } else if (isComboRecharge()) {
            applyto.setCombo((short) Math.min(30000, applyto.getCombo() + y));
            applyto.setLastCombo(System.currentTimeMillis());
            applyto.getClient().getSession().write(MaplePacketCreator.rechargeCombo(applyto.getCombo()));
            SkillFactory.getSkill(21000000).getEffect(10).applyComboBuff(applyto, applyto.getCombo());
        } else if (isDragonBlink()) {
            final MaplePortal portal = applyto.getMap().getPortal(Randomizer.nextInt(applyto.getMap().getPortals().size()));
            if (portal != null) {
                applyto.getClient().getSession().write(MaplePacketCreator.dragonBlink(portal.getId()));
                applyto.getMap().movePlayer(applyto, portal.getPosition());
                applyto.checkFollow();
            }
        } else if (isSpiritClaw() && !applyto.isClone()) {
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            boolean itemz = false;
            for (int i = 0; i <= use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((byte) i);
                if (item != null) {
                    if (GameConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) 200, false, true);
                        itemz = true;
                        break;
                    }
                }
            }
            if (!itemz) {
                return false;
            }
        } else if (cp != 0 && applyto.getCarnivalParty() != null) {
            applyto.getCarnivalParty().addCP(applyto, cp);
            applyto.CPUpdate(false, applyto.getAvailableCP(), applyto.getTotalCP(), 0);
            for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                chr.CPUpdate(true, applyto.getCarnivalParty().getAvailableCP(), applyto.getCarnivalParty().getTotalCP(), applyto.getCarnivalParty().getTeam());
            }
        } else if (nuffSkill != 0 && applyto.getParty() != null) {
            final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(nuffSkill);
            if (skil != null) {
                final MapleDisease dis = skil.getDisease();
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (applyto.getParty() == null || chr.getParty() == null || (chr.getParty().getId() != applyto.getParty().getId())) {
                        if (skil.targetsAll || Randomizer.nextBoolean()) {
                            if (dis == null) {
                                chr.dispel();
                            } else if (skil.getSkill() == null) {
                                chr.giveDebuff(dis, 1, 30000, dis.getDisease(), 1);
                            } else {
                                chr.giveDebuff(dis, skil.getSkill());
                            }
                            if (!skil.targetsAll) {
                                break;
                            }
                        }
                    }
                }
            }
        } else if ((effectedOnEnemy > 0 || effectedOnAlly > 0) && primary && applyto.inPVP()) {
            final int typeP = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
            if (typeP > 0 || effectedOnEnemy > 0) {
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (chr.getId() != applyto.getId() && (effectedOnAlly > 0 ? (chr.getTeam() == applyto.getTeam()) : (chr.getTeam() != applyto.getTeam() || typeP == 0))) {
                        applyTo(applyto, chr, false, pos, newDuration);
                    }
                }
            }
        } else if (mobSkill > 0 && mobSkillLevel > 0 && primary && applyto.inPVP()) {
            if (effectedOnEnemy > 0) {
                final int type = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    if (chr.getId() != applyto.getId() && (chr.getTeam() != applyto.getTeam() || type == 0)) {
                        chr.disease(mobSkill, mobSkillLevel);
                    }
                }
            } else if (sourceid == 2910000 || sourceid == 2910001) { //red flag
                applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 11, applyto.getLevel(), level));
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 11, applyto.getLevel(), level), false);

                //applyto.getClient().getSession().write(MaplePacketCreator.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Effect", 0, 0));
                //applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Effect", 0, 0), false);
                if (applyto.getTeam() == (sourceid - 2910000)) { //restore duh flag
                    if (sourceid == 2910000) {
                        applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been restored.");
                    } else {
                        applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been restored.");
                    }
                    applyto.getMap().spawnAutoDrop(sourceid, applyto.getMap().getGuardians().get(sourceid - 2910000).left);
                } else {
                    applyto.disease(mobSkill, mobSkillLevel);
                    if (sourceid == 2910000) {
                        applyto.getEventInstance().setProperty("redflag", String.valueOf(applyto.getId()));
                        applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been captured!");
                        //applyto.getClient().getSession().write(MaplePacketCreator.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Red", 600000, 0));
                        //applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Red", 600000, 0), false);
                    } else {
                        applyto.getEventInstance().setProperty("blueflag", String.valueOf(applyto.getId()));
                        applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been captured!");
                        //applyto.getClient().getSession().write(MaplePacketCreator.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0));
                        //applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0), false);
                    }
                }
            } else {
                applyto.disease(mobSkill, mobSkillLevel);
            }
        } else if (randomPickup != null && randomPickup.size() > 0) {
            MapleItemInformationProvider.getInstance().getItemEffect(randomPickup.get(Randomizer.nextInt(randomPickup.size()))).applyTo(applyto);
        }
        final SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null && (sourceid != 32111006 || (applyfrom.getBuffedValue(MapleBuffStat.REAPER) != null && !primary)) && !applyto.isClone()) {
            int summId = sourceid;
            if (sourceid == 3111002) {
                final Skill elite = SkillFactory.getSkill(3120012);
                if (applyfrom.getTotalSkillLevel(elite) > 0) {
                    return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, newDuration);
                }
            }
            final MapleSummon tosummon = new MapleSummon(applyfrom, summId, getLevel(), new Point(pos == null ? applyfrom.getTruePosition() : pos), summonMovementType);
            if (!tosummon.isPuppet()) {
                applyfrom.getCheatTracker().resetSummonAttack();
            }
            applyfrom.cancelEffect(this, true, -1, statups);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(tosummon);
            tosummon.addHP((short) x);
            if (isBeholder()) {
                tosummon.addHP((short) 1);
            } else if (sourceid == 4341006) {
                applyfrom.cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
            } else if (sourceid == 32111006) {
                return true; //no buff

            } else if (sourceid == 35111002) {
                List<Integer> count = new ArrayList<Integer>();
                final List<MapleSummon> ss = applyfrom.getSummonsReadLock();
                try {
                    for (MapleSummon s : ss) {
                        if (s.getSkill() == sourceid) {
                            count.add(s.getObjectId());
                        }
                    }
                } finally {
                    applyfrom.unlockSummonsReadLock();
                }
                if (count.size() != 3) {
                    return true; //no buff until 3
                }
                applyfrom.getClient().getSession().write(MaplePacketCreator.skillCooldown(sourceid, getCooldown()));
                applyfrom.addCooldown(sourceid, System.currentTimeMillis(), getCooldown() * 1000);
                applyfrom.getMap().broadcastMessage(MaplePacketCreator.teslaTriangle(applyfrom.getId(), count.get(0), count.get(1), count.get(2)));
            } else if (sourceid == 35121003) {
                applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions()); //doubt we need this at all
            }
        } else if (isMechDoor()) {
            int newId = 0;
            boolean applyBuff = false;
            if (applyto.getMechDoors().size() >= 2) {
                final MechDoor remove = applyto.getMechDoors().remove(0);
                newId = remove.getId();
                applyto.getMap().broadcastMessage(MaplePacketCreator.removeMechDoor(remove, true));
                applyto.getMap().removeMapObject(remove);
            } else {
                for (MechDoor d : applyto.getMechDoors()) {
                    if (d.getId() == newId) {
                        applyBuff = true;
                        newId = 1;
                        break;
                    }
                }
            }
            final MechDoor door = new MechDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), newId);
            applyto.getMap().spawnMechDoor(door);
            applyto.addMechDoor(door);
            applyto.getClient().getSession().write(MaplePacketCreator.mechPortal(door.getTruePosition()));
            if (!applyBuff) {
                return true; //do not apply buff until 2 doors spawned
            }
        }
        if (primary && availableMap != null) {
            for (Pair<Integer, Integer> e : availableMap) {
                if (applyto.getMapId() < e.left || applyto.getMapId() > e.right) {
                    applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                    return true;
                }
            }
        }
        if (overTime && !isEnergyCharge()) {
            applyBuffEffect(applyfrom, applyto, primary, newDuration);
        }
        if (skill) {
            removeMonsterBuff(applyfrom);
        }
        if (primary) {
            if ((overTime || isHeal()) && !isEnergyCharge()) {
                applyBuff(applyfrom, newDuration);
            }
            if (isMonsterBuff()) {
                applyMonsterBuff(applyfrom);
            }
        }
        if (isMagicDoor()) { // Magic Door
            MapleDoor door = new MapleDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), sourceid); // Current Map door
            if (door.getTownPortal() != null) {
                applyto.getMap().spawnDoor(door);
                applyto.addDoor(door);
                MapleDoor townDoor = new MapleDoor(door); // Town door
                applyto.addDoor(townDoor);
                door.getTown().spawnDoor(townDoor);
                door.first = false;
                if (applyto.getParty() != null) { // update town doors
                    applyto.silentPartyUpdate();
                }
            } else {
                applyto.dropMessage(5, "村庄里已经没有可创造时空门的位置。");
            }
        } else if (isMist()) {
            final Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            final MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), false);

        } else if (isTimeLeap()) { // Time Leap
            for (MapleCoolDownValueHolder i : applyto.getCooldowns()) {
                if (i.skillId != 5121010) {
                    applyto.removeCooldown(i.skillId);
                    applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
                }
            }
        } else {
            for (WeakReference<MapleCharacter> chrz : applyto.getClones()) {
                if (chrz.get() != null) {
                    applyTo(chrz.get(), chrz.get(), primary, pos, newDuration);
                }
            }
        }
        if (fatigueChange != 0 && applyto.getSummonedFamiliar() != null && (familiars == null || familiars.contains(applyto.getSummonedFamiliar().getFamiliar()))) {
            applyto.getSummonedFamiliar().addFatigue(applyto, fatigueChange);
        }
        if (rewardMeso != 0) {
            applyto.gainMeso(rewardMeso, false);
        }
        if (rewardItem != null && totalprob > 0) {
            for (Triple<Integer, Integer, Integer> reward : rewardItem) {
                if (MapleInventoryManipulator.checkSpace(applyto.getClient(), reward.left, reward.mid, "") && reward.right > 0 && Randomizer.nextInt(totalprob) < reward.right) { // Total prob
                    if (GameConstants.getInventoryType(reward.left) == MapleInventoryType.EQUIP) {
                        final Item item = MapleItemInformationProvider.getInstance().getEquipById(reward.left);
                        item.setGMLog("Reward item (effect): " + sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.addbyItem(applyto.getClient(), item);
                    } else {
                        MapleInventoryManipulator.addById(applyto.getClient(), reward.left, reward.mid.shortValue(), "Reward item (effect): " + sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                }
            }
        }
        if (familiarTarget == 2 && applyfrom.getParty() != null && primary) { //to party
            for (MaplePartyCharacter mpc : applyfrom.getParty().getMembers()) {
                if (mpc.getId() != applyfrom.getId() && mpc.getChannel() == applyfrom.getClient().getChannel() && mpc.getMapid() == applyfrom.getMapId() && mpc.isOnline()) {
                    MapleCharacter mc = applyfrom.getMap().getCharacterById(mpc.getId());
                    if (mc != null) {
                        applyTo(applyfrom, mc, false, null, newDuration);
                    }
                }
            }
        } else if (familiarTarget == 3 && primary) {
            for (MapleCharacter mc : applyfrom.getMap().getCharactersThreadsafe()) {
                if (mc.getId() != applyfrom.getId()) {
                    applyTo(applyfrom, mc, false, null, newDuration);
                }
            }
        }
        return true;
    }

    public final boolean applyReturnScroll(final MapleCharacter applyto) {
        if (moveTo != -1) {
            //if (applyto.getMap().getReturnMapId() != applyto.getMapId() || sourceid == 2031010 || sourceid == 2030021) {
            MapleMap target;
            if (moveTo == 999999999) {
                target = applyto.getMap().getReturnMap();
            } else {
                target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61) {
                    if (target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20) {
                        if (target.getId() / 10000000 != applyto.getMapId() / 10000000) {
                            if (target.getId() == 120000000 && applyto.getMapId() != 120000000) {
                                applyto.changeMap(target, target.getPortal(0));
                                return true;
                            } else if (target.getId() != 120000000 && applyto.getMapId() == 120000000) {
                                applyto.changeMap(target, target.getPortal(0));
                                return true;
                            }
                            return false;
                        }
                    }
                }
            }
            applyto.changeMap(target, target.getPortal(0));
            return true;
            //}
        }
        return false;
    }

    private final boolean isSoulStone() {
        return skill && sourceid == 22181003;
    }

    private final void applyBuff(final MapleCharacter applyfrom, int newDuration) {
        if (isSoulStone()) {
            if (applyfrom.getParty() != null) {
                int membrs = 0;
                for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                    if (!chr.isClone() && chr.getParty() != null && chr.getParty().getId() == applyfrom.getParty().getId() && chr.isAlive()) {
                        membrs++;
                    }
                }
                List<MapleCharacter> awarded = new ArrayList<MapleCharacter>();
                while (awarded.size() < Math.min(membrs, y)) {
                    for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                        if (chr != null && !chr.isClone() && chr.isAlive() && chr.getParty() != null && chr.getParty().getId() == applyfrom.getParty().getId() && !awarded.contains(chr) && Randomizer.nextInt(y) == 0) {
                            awarded.add(chr);
                        }
                    }
                }
                for (MapleCharacter chr : awarded) {
                    applyTo(applyfrom, chr, false, null, newDuration);
                    chr.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2, applyfrom.getLevel(), level));
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), sourceid, 2, applyfrom.getLevel(), level), false);
                }
            }
        } else if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff() || applyfrom.inPVP())) {
            final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            final List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));

            for (final MapleMapObject affectedmo : affecteds) {
                final MapleCharacter affected = (MapleCharacter) affectedmo;

                if (affected.getId() != applyfrom.getId() && (isGmBuff() || (applyfrom.inPVP() && affected.getTeam() == applyfrom.getTeam() && Integer.parseInt(applyfrom.getEventInstance().getProperty("type")) != 0) || (applyfrom.getParty() != null && affected.getParty() != null && applyfrom.getParty().getId() == affected.getParty().getId()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        applyTo(applyfrom, affected, false, null, newDuration);
                        affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2, applyfrom.getLevel(), level));
                        affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2, applyfrom.getLevel(), level), false);
                    }
                    if (isTimeLeap()) {
                        for (MapleCoolDownValueHolder i : affected.getCooldowns()) {
                            if (i.skillId != 5121010) {
                                affected.removeCooldown(i.skillId);
                                affected.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
                            }
                        }
                    }
                }
            }
        }
    }

    private final void removeMonsterBuff(final MapleCharacter applyfrom) {
        List<MonsterStatus> cancel = new ArrayList<MonsterStatus>();
        switch (sourceid) {
            case 1111007:
            case 1211009:
            case 1311007:
                cancel.add(MonsterStatus.WEAPON_DEFENSE_UP);
                cancel.add(MonsterStatus.MAGIC_DEFENSE_UP);
                cancel.add(MonsterStatus.WEAPON_ATTACK_UP);
                cancel.add(MonsterStatus.MAGIC_ATTACK_UP);
                break;
            default:
                return;
        }
        final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        final List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        int i = 0;

        for (final MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (MonsterStatus stat : cancel) {
                    ((MapleMonster) mo).cancelStatus(stat);
                }
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    public final void applyMonsterBuff(final MapleCharacter applyfrom) {
        final Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        final boolean pvp = applyfrom.inPVP();
        final MapleMapObjectType type = pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER;
        final List<MapleMapObject> affected = sourceid == 35111005 ? applyfrom.getMap().getMapObjectsInRange(applyfrom.getTruePosition(), Double.POSITIVE_INFINITY, Arrays.asList(type)) : applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(type));
        int i = 0;

        for (final MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (Map.Entry<MonsterStatus, Integer> stat : getMonsterStati().entrySet()) {
                    if (pvp) {
                        MapleCharacter chr = (MapleCharacter) mo;
                        MapleDisease d = MonsterStatus.getLinkedDisease(stat.getKey());
                        if (d != null) {
                            chr.giveDebuff(d, stat.getValue(), getDuration(), d.getDisease(), 1);
                        }
                    } else {
                        MapleMonster mons = (MapleMonster) mo;
                        if (sourceid == 35111005 && mons.getStats().isBoss()) {
                            break;
                        }
                        mons.applyStatus(applyfrom, new MonsterStatusEffect(stat.getKey(), stat.getValue(), sourceid, null, false), isPoison(), isSubTime(sourceid) ? getSubTime() : getDuration(), true, this);
                        //      mons.applyStatus(applyfrom, new MonsterStatusEffect(stat.getKey(), stat.getValue(), sourceid, null, false), isPoison(), getDuration(), true, this);
                    }
                }
                if (pvp && skill) {
                    MapleCharacter chr = (MapleCharacter) mo;
                    handleExtraPVP(applyfrom, chr);
                }
            }
            i++;
            if (i >= mobCount && sourceid != 35111005) {
                break;
            }
        }
    }

    public final void handleExtraPVP(MapleCharacter applyfrom, MapleCharacter chr) {
        if (sourceid == 2311005 || sourceid == 5121005 || sourceid == 1201006 || (GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 104)) { //doom, threaten, snatch
            final long starttime = System.currentTimeMillis();

            final int localsourceid = sourceid == 5121005 ? 90002000 : sourceid;
            final Map<MapleBuffStat, Integer> localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
            if (sourceid == 2311005) {
                localstatups.put(MapleBuffStat.MORPH, 7);
            } else if (sourceid == 1201006) {
                localstatups.put(MapleBuffStat.THREATEN_PVP, (int) level);
            } else if (sourceid == 5121005) {
                localstatups.put(MapleBuffStat.SNATCH, 1);
            } else {
                localstatups.put(MapleBuffStat.MORPH, x);
            }
            chr.getClient().getSession().write(MaplePacketCreator.giveBuff(localsourceid, getDuration(), localstatups, this));
            chr.registerEffect(this, starttime, BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, localstatups), getDuration()), localstatups, false, getDuration(), applyfrom.getId());
        }
    }

    public final Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft) {
        return calculateBoundingBox(posFrom, facingLeft, lt, rb, range);
    }

    public final Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft, int addedRange) {
        return calculateBoundingBox(posFrom, facingLeft, lt, rb, range + addedRange);
    }

    public final static Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft, final Point lt, final Point rb, final int range) {
        if (lt == null || rb == null) {
            return new Rectangle((facingLeft ? (-200 - range) : 0) + posFrom.x, (-100 - range) + posFrom.y, 200 + range, 100 + range);
        }
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public final double getMaxDistanceSq() { //lt = infront of you, rb = behind you; not gonna distanceSq the two points since this is in relative to player position which is (0,0) and not both directions, just one
        final int maxX = Math.max(Math.abs(lt == null ? 0 : lt.x), Math.abs(rb == null ? 0 : rb.x));
        final int maxY = Math.max(Math.abs(lt == null ? 0 : lt.y), Math.abs(rb == null ? 0 : rb.y));
        return (maxX * maxX) + (maxY * maxY);
    }

    public final void setDuration(int d) {
        this.duration = d;
    }

    public final void silentApplyBuff(final MapleCharacter chr, final long starttime, final int localDuration, final Map<MapleBuffStat, Integer> statup, final int cid) {
        chr.registerEffect(this, starttime, BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, statup),
                ((starttime + localDuration) - System.currentTimeMillis())), statup, true, localDuration, cid);

        final SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType);
            if (!tosummon.isPuppet()) {
                chr.getCheatTracker().resetSummonAttack();
                chr.getMap().spawnSummon(tosummon);
                chr.addSummon(tosummon);
                tosummon.addHP((short) x);
                if (isBeholder()) {
                    tosummon.addHP((short) 1);
                }
            }
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, short combo) {
        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
        stat.put(MapleBuffStat.ARAN_COMBO, (int) combo);
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, 99999, stat, this)); // Hackish timing, todo find out

        final long starttime = System.currentTimeMillis();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - System.currentTimeMillis()));
        applyto.registerEffect(this, starttime, null, applyto.getId());
    }

    public final void applyEnergyBuff(final MapleCharacter applyto, final boolean infinity) {
        final long starttime = System.currentTimeMillis();
        if (infinity) {
            applyto.getClient().getSession().write(MaplePacketCreator.giveEnergyChargeTest(0, duration / 1000));
            applyto.registerEffect(this, starttime, null, applyto.getId());
        } else {
            final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
            stat.put(MapleBuffStat.ENERGY_CHARGE, 10000);
            applyto.cancelEffect(this, true, -1, stat);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveEnergyChargeTest(applyto.getId(), 10000, duration / 1000), false);
            final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, stat);
            final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, ((starttime + duration) - System.currentTimeMillis()));
            applyto.registerEffect(this, starttime, schedule, stat, false, duration, applyto.getId());

        }
    }

    private final void applyBuffEffect(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final int newDuration) {
        int localDuration = newDuration;
        if (primary) {
            localDuration = Math.max(newDuration, alchemistModifyVal(applyfrom, localDuration, false));
        }
        Map<MapleBuffStat, Integer> localstatups = statups, maskedStatups = null;
        boolean normal = true, showEffect = primary;
        int maskedDuration = 0;
        switch (sourceid) {
            case 35111013:
            case 5111007:
            case 5311005:
            case 5211007: {//dice
                final int zz = Randomizer.nextInt(6) + 1;
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showDiceEffect(applyto.getId(), sourceid, zz, -1, level), false);
                applyto.getClient().getSession().write(MaplePacketCreator.showOwnDiceEffect(sourceid, zz, -1, level));
                if (zz <= 1) {
                    return;
                }
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, zz);
                applyto.getClient().getSession().write(MaplePacketCreator.giveDice(zz, sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            }
            case 5320007: {//dice
                final int zz = Randomizer.nextInt(6) + 1;
                final int zz2 = makeChanceResult() ? (Randomizer.nextInt(6) + 1) : 0;
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showDiceEffect(applyto.getId(), sourceid, zz, zz2 > 0 ? -1 : 0, level), false);
                applyto.getClient().getSession().write(MaplePacketCreator.showOwnDiceEffect(sourceid, zz, zz2 > 0 ? -1 : 0, level));
                if (zz <= 1 && zz2 <= 1) {
                    return;
                }
                final int buffid = zz == zz2 ? (zz * 100) : (zz <= 1 ? zz2 : (zz2 <= 1 ? zz : (zz * 10 + zz2)));
                if (buffid >= 100) { //just because of animation lol
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled a Double Down! (" + (buffid / 100) + ")");
                } else if (buffid >= 10) {
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled two dice. (" + (buffid / 10) + " and " + (buffid % 10) + ")");
                }
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, buffid);
                applyto.getClient().getSession().write(MaplePacketCreator.giveDice(zz, sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            }
            case 33101006: {//jaguar oshi
                applyto.clearLinkMid();
                MapleBuffStat theBuff = null;
                int theStat = y;
                switch (Randomizer.nextInt(6)) {
                    case 0:
                        theBuff = MapleBuffStat.CRITICAL_RATE_BUFF;
                        break;
                    case 1:
                        theBuff = MapleBuffStat.MP_BUFF;
                        break;
                    case 2:
                        theBuff = MapleBuffStat.DAMAGE_TAKEN_BUFF;
                        theStat = x;
                        break;
                    case 3:
                        theBuff = MapleBuffStat.DODGE_CHANGE_BUFF;
                        theStat = x;
                        break;
                    case 4:
                        theBuff = MapleBuffStat.DAMAGE_BUFF;
                        break;
                    case 5:
                        theBuff = MapleBuffStat.ATTACK_BUFF;
                        break;
                }
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(theBuff, theStat);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case 8006:
            case 10008006:
            case 20008006:
            case 20018006:
            case 20028006:
            case 30008006:
            case 30018006:
            case 5121009: // Speed Infusion
            case 15111005:
            case 5001005: // Dash
            case 4321000: //tornado spin
            case 15001003: {
                applyto.getClient().getSession().write(MaplePacketCreator.givePirate(statups, localDuration / 1000, sourceid));
                if (!applyto.isHidden()) {
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignPirate(statups, localDuration / 1000, applyto.getId(), sourceid), false);
                }
                normal = false;
                break;
            }
            /*case 5211006: // Homing Beacon
            case 22151002: //killer wings
            case 5220011: {// Bullseye
                if (applyto.getFirstLinkMid() > 0) {
                    applyto.getClient().getSession().write(MaplePacketCreator.cancelHoming());
                    applyto.getClient().getSession().write(MaplePacketCreator.giveHoming(sourceid, applyto.getFirstLinkMid(), 1));
                } else {
                    return;
                }
                normal = false;
                break;
            }*/
            case 2120010:
            case 2220010:
            case 2320011: //arcane aim
                if (applyto.getFirstLinkMid() > 0) {
                    applyto.getClient().getSession().write(MaplePacketCreator.giveArcane(applyto.getAllLinkMid(), localDuration));
                } else {
                    return;
                }
                normal = false;
                break;
            case 30011001:
            case 30001001: { // Wind Walk
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.INFILTRATE, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 13101006: { // Wind Walk
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.WIND_WALK, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 4001003: {
                if (applyfrom.getTotalSkillLevel(4330001) > 0) {
                    SkillFactory.getSkill(4330001).getEffect(applyfrom.getTotalSkillLevel(4330001)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                } //fallthrough intended
            }
            case 4330001:
            case 14001003: { // Dark Sight
                if (applyto.isHidden()) {
                    return; //don't even apply the buff
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARKSIGHT, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 23111005: {
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.WATER_SHIELD, x);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 23101003: {
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_SURGE, x);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 32121003: { //twister
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.TORNADO, x);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 32111005: { //body boost
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                Pair<MapleBuffStat, Integer> statt;
                int sourcez = 0;
                if (applyfrom.getStatForBuff(MapleBuffStat.DARK_AURA) != null) {
                    sourcez = 32001003;
                    statt = new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARK_AURA, (int) (level + 10 + applyto.getTotalSkillLevel(32001003))); //i think
                } else if (applyfrom.getStatForBuff(MapleBuffStat.YELLOW_AURA) != null) {
                    sourcez = 32101003;
                    statt = new Pair<MapleBuffStat, Integer>(MapleBuffStat.YELLOW_AURA, (int) applyto.getTotalSkillLevel(32101003));
                } else if (applyfrom.getStatForBuff(MapleBuffStat.BLUE_AURA) != null) {
                    sourcez = 32101002;
                    localDuration = 10000;
                    statt = new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLUE_AURA, (int) applyto.getTotalSkillLevel(32101002));
                } else {
                    return;
                }
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.BODY_BOOST, (int) level);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                localstatups.put(statt.left, statt.right);
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(statt.left, statt.right);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourcez, localDuration, stat, this));
                normal = false;
                break;
            }
            case 32001003: {//dark aura
                if (applyfrom.getTotalSkillLevel(32120000) > 0) {
                    SkillFactory.getSkill(32120000).getEffect(applyfrom.getTotalSkillLevel(32120000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 32110007: {//fallthrough intended
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                statt.put(sourceid == 32110007 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) level);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.DARK_AURA, x);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), statt, this), false);

                normal = false;
                break;
            }
            case 32120000: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                statt.put(MapleBuffStat.AURA, (int) applyfrom.getTotalSkillLevel(32001003));
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(32001003, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.DARK_AURA, x);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
            case 32101002: {
                if (applyfrom.getTotalSkillLevel(32110000) > 0) {
                    SkillFactory.getSkill(32110000).getEffect(applyfrom.getTotalSkillLevel(32110000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 31121005: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARK_METAMORPHOSIS, 6);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;

            }
            case 32110008: {
                localDuration = 10000;
            }
            case 32110000: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                statt.put(sourceid == 32110008 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) (sourceid == 32110000 ? applyfrom.getTotalSkillLevel(32101002) : level));
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid == 32110000 ? 32101002 : sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.BLUE_AURA, (int) level);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
            case 32101003: {
                if (applyfrom.getTotalSkillLevel(32120001) > 0) {
                    SkillFactory.getSkill(32120001).getEffect(applyfrom.getTotalSkillLevel(32120001)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            }
            case 32110009:
            case 32120001: {
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                final EnumMap<MapleBuffStat, Integer> statt = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                statt.put(sourceid == 32110009 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, (int) (sourceid == 32120001 ? applyfrom.getTotalSkillLevel(32101003) : level));
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid == 32120001 ? 32101003 : sourceid, localDuration, statt, this));
                statt.clear();
                statt.put(MapleBuffStat.YELLOW_AURA, (int) level);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, statt, this));
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), statt, this), false);
                normal = false;
                break;
            }
            /*case 1211008: { //lightning
                if (applyto.getBuffedValue(MapleBuffStat.WK_CHARGE) != null && applyto.getBuffSource(MapleBuffStat.WK_CHARGE) != sourceid) {
                    localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    localstatups.put(MapleBuffStat.LIGHTNING_CHARGE, 1);
                } else if (!applyto.isHidden()) {
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.WK_CHARGE, 1);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                }
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }*/
            case 35111004: {//siege
                if (applyto.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null && applyto.getBuffSource(MapleBuffStat.MECH_CHANGE) == 35121005) {
                    SkillFactory.getSkill(35121013).getEffect(level).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, 1);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 35001001: //flame
            case 35101009:
            case 35121013:
            case 35121005: { //missile
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, 1);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 1220013: {
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.DIVINE_SHIELD, 1);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 1111002:
            case 11111001: { // Combo
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.COMBO, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 3101004:
            case 3201004:
            case 13101003: { // Soul Arrow
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SOULARROW, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 2321005: //holy shield
                if (GameConstants.GMS) { //TODO JUMP
                    applyto.cancelEffectFromBuffStat(MapleBuffStat.BLESS);
                }
                break;
            case 4211008:
            case 4331002:
            case 4111002:
            case 14111000: { // Shadow Partne
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SHADOWPARTNER, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 15111006: { // Spark
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.SPARK, x);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case 4341002: { // Final Cut
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.FINAL_CUT, y);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            }
            case 1211006: //寒冰之棍
            case 1211003: //烈焰之劍
            case 1211004: //烈焰之棍
            case 1211005: //寒冰之劍
            case 1211007: //雷鳴之劍
            case 1211008: //雷鳴之棍
            case 1221003: //聖靈之劍
            case 1221004: //聖靈之棍
            case 11111007:
            case 21101006:
            case 21111005:
            case 15101006: { // Soul Arrow
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.WK_CHARGE, 1);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            case 3120006:
            case 3220005: { // Spirit Link
                if (applyto.isHidden()) {
                    break;
                }
                final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_LINK, 0);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            }
            //case 2121004:
            //case 2221004:
            //case 2321004: { //Infinity
            //maskedDuration = alchemistModifyVal(applyfrom, 4000, false);
            //break;
            //}
            case 4331003: { // Owl Spirit
                localstatups = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.OWL_SPIRIT, y);
                applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
                applyto.setBattleshipHP(x); //a variable that wouldnt' be used by a db
                normal = false;
                break;
            }
            case 1121010: // Enrage
                applyto.handleOrbconsume(10);
                break;
            case 35001002:
                if (applyfrom.getTotalSkillLevel(35120000) > 0) {
                    SkillFactory.getSkill(35120000).getEffect(applyfrom.getTotalSkillLevel(35120000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
            //fallthrough intended
            default:
                /*if (isPirateMorph()) {
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.MORPH, getMorph(applyto));
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                    applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, stat, this));
                    maskedStatups = new EnumMap<MapleBuffStat, Integer>(localstatups);
                    maskedStatups.remove(MapleBuffStat.MORPH);
                    normal = maskedStatups.size() > 0;
                } else*/ if (isMorph()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    if (isIceKnight()) {
                        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                        stat.put(MapleBuffStat.ICE_KNIGHT, 2);
                        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(0, localDuration, stat, this));
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.MORPH, getMorph(applyto));
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isInflation()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.GIANT_POTION, (int) inflation);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (charColor > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.FAMILIAR_SHADOW, 1);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isMonsterRiding()) {
                    localDuration = 2100000000;
                    localstatups = new EnumMap<>(statups);
                    localstatups.put(MapleBuffStat.MONSTER_RIDING, 1);
                    final int mountid = parseMountInfo(applyto, sourceid);
                    final int mountid2 = parseMountInfo_Pure(applyto, sourceid);
                    if (mountid != 0 && mountid2 != 0) {
                        final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                        stat.put(MapleBuffStat.MONSTER_RIDING, 0);
                        applyto.cancelEffectFromBuffStat(MapleBuffStat.POWERGUARD);
                        applyto.cancelEffectFromBuffStat(MapleBuffStat.MANA_REFLECTION);
                        applyto.getClient().getSession().write(MaplePacketCreator.giveMount(mountid2, sourceid, stat));
                        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, mountid, sourceid), false);
                    } else {
                        return;
                    }
                    maskedStatups = new EnumMap<MapleBuffStat, Integer>(localstatups);
                    maskedStatups.remove(MapleBuffStat.MONSTER_RIDING);
                    //normal = maskedStatups.size() > 0;
                    normal = false;
                } else if (isSoaring()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.SOARING, 1);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (berserk > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.PYRAMID_PQ, 0);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isBerserkFury() || berserk2 > 0) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.BERSERK_FURY, 1);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                } else if (isDivineBody()) {
                    if (applyto.isHidden()) {
                        break;
                    }
                    final EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.DIVINE_BODY, 1);
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
                }
                break;
        }
        if (showEffect && !applyto.isHidden()) {
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, applyto.getLevel(), level), false);
        }
        if (isMechPassive()) {
            applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid - 1000, 1, applyto.getLevel(), level, (byte) 1));
        }
        if (!isMonsterRiding() && !isMechDoor()) {
            applyto.cancelEffect(this, true, -1, localstatups);
        }

        if (isHoming()) {
            if (applyto.getFirstLinkMid() > 0) {
                applyto.getClient().getSession().write(MaplePacketCreator.cancelHoming());
                applyto.getClient().getSession().write(MaplePacketCreator.giveHoming(sourceid, applyto.getFirstLinkMid(), 1));
            } else {
                return;
            }
            normal = false;
        }

        // Broadcast effect to self
        if (normal && localstatups.size() > 0) {
            applyto.getClient().getSession().write(MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, maskedStatups == null ? localstatups : maskedStatups, this));
        }
        final long starttime = System.currentTimeMillis();
        final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, localstatups);
        final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, maskedDuration > 0 ? maskedDuration : localDuration);
        applyto.registerEffect(this, starttime, schedule, localstatups, false, localDuration, applyfrom.getId());
    }

    public static final int parseMountInfo(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
            case 1004: // Monster riding
            case 11004: // Monster riding
            case 10001004:
            case 20001004:
            case 20011004:
            case 20021004:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (GameConstants.GMS ? -118 : -123)) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (GameConstants.GMS ? -119 : -124)) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (GameConstants.GMS ? -118 : -123)).getItemId();
                }
                return parseMountInfo_Pure(player, skillid);
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    public static final int parseMountInfo_Pure(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
            case 1004: // Monster riding
            case 11004: // Monster riding
            case 10001004:
            case 20001004:
            case 20011004:
            case 20021004:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (-18)) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (-19)) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (-18)).getItemId();
                }
                return 0;
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    private final int calcHPChange(final MapleCharacter applyfrom, final boolean primary) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange /= 2;
                }
            } else { // assumption: this is heal
                if (isHeal()) {
                    hpchange += Math.ceil(((double) (hp / 100.0D)) * applyfrom.getStat().getCurrentMaxHp());
                } else {
                    hpchange += makeHealHP(hp / 100.0, applyfrom.getStat().getTotalMagic(), 3, 5);
                }
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange = -hpchange;
                }
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * hpR) / (applyfrom.hasDisease(MapleDisease.ZOMBIFY) ? 2 : 1);
        }
        // actually receivers probably never get any hp when it's not heal but whatever
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        switch (this.sourceid) {
            case 4211001: // Chakra
                final PlayerStats stat = applyfrom.getStat();
                int v42 = getY() + 100;
                int v38 = Randomizer.rand(1, 100) + 100;
                hpchange = (int) ((v38 * stat.getLuk() * 0.033 + stat.getDex()) * v42 * 0.002);
                hpchange += makeHealHP(getY() / 100.0, applyfrom.getStat().getTotalLuk(), 2.3, 3.5);
                break;
            case 9001000: // GM技能
                hpchange += applyfrom.getStat().getCurrentMaxHp();
                break;
        }
        return hpchange;
    }

    private static final int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private final int calcMPChange(final MapleCharacter applyfrom, final boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getStat().getCurrentMaxMp() * mpR);
        }
        if (GameConstants.isDemon(applyfrom.getJob())) {
            mpchange = 0;
        }
        if (primary) {
            if (mpCon != 0 && !GameConstants.isDemon(applyfrom.getJob())) {
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else {
                    mpchange -= (mpCon - (mpCon * applyfrom.getStat().mpconReduce / 100)) * (applyfrom.getStat().mpconPercent / 100.0);
                }
            } else if (forceCon != 0 && GameConstants.isDemon(applyfrom.getJob())) {
                mpchange -= forceCon;
            }
        }

        return mpchange;
    }

    private final MapleStatEffect getAlchemistEffect(final MapleCharacter chr) {
        Skill al;
        switch (chr.getJob()) {
            case 411:
            case 412:
                al = SkillFactory.getSkill(4110000);
                if (chr.getSkillLevel(al) <= 0) {
                    return null;
                }
                return al.getEffect(chr.getSkillLevel(al));
        }
        return null;
    }

    private final int alchemistModifyVal(final MapleCharacter chr, final int val, final boolean withX) {
        if (!skill) {
            int offset = chr.getStat().RecoveryUP;
            final MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                offset += (withX ? alchemistEffect.getX() : alchemistEffect.getY());
            } else {
                offset += 100;
            }
            return (val * offset / 100);
        }
        return val;
    }

    /*public final int alchemistModifyVal(final MapleCharacter chr, final int val, final boolean withX) {
        if (!skill) {
            return (val * (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP) / 100);
        }
        return (val * (withX ? chr.getStat().RecoveryUP : (chr.getStat().BuffUP_Skill + (getSummonMovementType() == null ? 0 : chr.getStat().BuffUP_Summon))) / 100);
    }*/
    public final void setSourceId(final int newid) {
        sourceid = newid;
    }

    public final boolean isGmBuff() {
        switch (sourceid) {
            case 10001075: //Empress Prayer
            case 9001000: // GM dispel
            case 9001001: // GM haste
            case 9001002: // GM Holy Symbol
            case 9001003: // GM Bless
            case 9001005: // GM resurrection
            case 9001008: // GM Hyper body

            case 9101000:
            case 9101001:
            case 9101002:
            case 9101003:
            case 9101005:
            case 9101008:
                return true;
            default:
                return GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1005;
        }
    }

    public final boolean isInflation() {
        return inflation > 0;
    }

    public final int getInflation() {
        return inflation;
    }

    public final boolean isEnergyCharge() {
        return skill && (sourceid == 5110001 || sourceid == 15100004);
    }

    private final boolean isMonsterBuff() {
        switch (sourceid) {
            case 1201006: // threaten
            case 2101003: // fp slow
            case 2201003: // il slow
            case 5011002:
            case 12101001: // cygnus slow
            case 2211004: // il seal
            case 2111004: // fp seal
            case 12111002: // cygnus seal
            case 2311005: // doom
            case 4111003: // shadow web
            case 14111001: // cygnus web
            case 4121004: // Ninja ambush
            case 4221004: // Ninja ambush
            case 22151001:
            case 22121000:
            case 22161002:
            case 4321002:
            case 4341003:
            case 90001002:
            case 90001003:
            case 90001004:
            case 90001005:
            case 90001006:
            case 1111007:
            case 1211009:
            case 1311007:
            case 35111005:
            case 32120000:
            case 32120001:
                return skill;
        }
        return false;
    }

    public final void setPartyBuff(boolean pb) {
        this.partyBuff = pb;
    }

    private final boolean isPartyBuff() {
        if (lt == null || rb == null || !partyBuff) {
            return isSoulStone();
        }
        switch (sourceid) {
            case 1211003:
            case 1211004:
            case 1211005:
            case 1211006:
            case 1211007:
            case 1211008:
            case 1221003:
            case 1221004:
            case 11111007:
            case 12101005:
            case 4311001:
            case 4331003:
            case 4341002:
            case 35121005:
                return false;
        }
        if (GameConstants.isNoDelaySkill(sourceid)) {
            return false;
        }
        return true;
    }

    public final boolean isArcane() {
        return skill && (sourceid == 2320011 || sourceid == 2220010 || sourceid == 2120010);
    }

    public final boolean isHeal() {
        return skill && (sourceid == 2301002 || sourceid == 9101000 || sourceid == 9001000);
    }

    public final boolean isResurrection() {
        return skill && (sourceid == 9001005 || sourceid == 9101005 || sourceid == 2321006);
    }

    public final boolean isTimeLeap() {
        return skill && sourceid == 5121010;
    }

    public final short getHp() {
        return hp;
    }

    public final short getMp() {
        return mp;
    }

    public final double getHpR() {
        return hpR;
    }

    public final double getMpR() {
        return mpR;
    }

    public final byte getMastery() {
        return mastery;
    }

    public final short getWatk() {
        return watk;
    }

    public final short getMatk() {
        return matk;
    }

    public final short getWdef() {
        return wdef;
    }

    public final short getMdef() {
        return mdef;
    }

    public final short getAcc() {
        return acc;
    }

    public final short getAvoid() {
        return avoid;
    }

    public final short getHands() {
        return hands;
    }

    public final short getSpeed() {
        return speed;
    }

    public final short getJump() {
        return jump;
    }

    public final int getDuration() {
        return duration;
    }

    public final boolean isOverTime() {
        return overTime;
    }

    public final Map<MapleBuffStat, Integer> getStatups() {
        return statups;
    }

    public final boolean sameSource(final MapleStatEffect effect) {
        return effect != null && this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public final int getCr() {
        return cr;
    }

    public final int getT() {
        return t;
    }

    public final int getU() {
        return u;
    }

    public final int getV() {
        return v;
    }

    public final int getW() {
        return w;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final int getZ() {
        return z;
    }

    public final short getDamage() {
        return damage;
    }

    public final short getPVPDamage() {
        return PVPdamage;
    }

    public final byte getAttackCount() {
        return attackCount;
    }

    public final byte getBulletCount() {
        return bulletCount;
    }

    public final int getBulletConsume() {
        return bulletConsume;
    }

    public final byte getMobCount() {
        return mobCount;
    }

    public final int getMoneyCon() {
        return moneyCon;
    }

    public final int getItemCon() {
        return itemCon;
    }

    public final int getItemConNo() {
        return itemConNo;
    }

    public final int getCooldown() {
        return cooldown;
    }

    public final Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    public final int getBerserk() {
        return berserk;
    }

    public final boolean isHide() {
        return skill && (sourceid == 9001004 || sourceid == 9101004);
    }

    public final boolean isDragonBlood() {
        return skill && sourceid == 1311008;
    }

    public final boolean isRecovery() {
        return skill && (sourceid == 1001 || sourceid == 10001001 || sourceid == 20001001 || sourceid == 20011001 || sourceid == 20021001 || sourceid == 11001 || sourceid == 35121005);
    }

    public final boolean isBerserk() {
        return skill && sourceid == 1320006;
    }

    public final boolean isBeholder() {
        return skill && sourceid == 1321007;
    }

    public final boolean isMPRecovery() {
        return skill && sourceid == 5101005;
    }

    public final boolean isInfinity() {
        return skill && (sourceid == 2121004 || sourceid == 2221004 || sourceid == 2321004);
    }

    public final boolean isMonsterRiding_() {
        return skill && (sourceid == 1004 || sourceid == 10001004 || sourceid == 20001004 || sourceid == 20011004 || sourceid == 11004 || sourceid == 20021004 || sourceid == 80001000);
    }

    public final boolean isMonsterRiding() {
        return skill && (isMonsterRiding_() || GameConstants.getMountItem(sourceid, null) != 0);
    }

    public final boolean isMagicDoor() {
        return skill && (sourceid == 2311002 || sourceid % 10000 == 8001);
    }

    public final boolean isMesoGuard() {
        return skill && sourceid == 4211005;
    }

    public final boolean isMechDoor() {
        return skill && sourceid == 35101005;
    }

    public final boolean isComboRecharge() {
        return skill && sourceid == 21111009;
    }

    public final boolean isDragonBlink() {
        return skill && sourceid == 22141004;
    }

    public final boolean isCharge() {
        switch (sourceid) {
            case 1211003:
            case 1211008:
            case 11111007:
            case 12101005:
            case 15101006:
            case 21111005:
                return skill;
        }
        return false;
    }

    public final boolean isSub() { // implement it soon
        switch (sourceid) {
            case 31121005:
                return skill;
        }
        return false;
    }

    public final boolean isPoison() {
        //return dot > 0 && dotTime > 0;
        switch (sourceid) {
            case 2111003:
            case 2101005:
            case 2111006:
            case 2121003:
            case 2221003:
            case 12111005: // Flame gear
            // case 3111003: //inferno, new
            case 22161002: //phantom imprint
                return skill;
        }
        return false;
    }

    private final boolean isMist() {
        return skill && (sourceid == 2111003 || sourceid == 4221006 || sourceid == 12111005 || sourceid == 14111006 || sourceid == 22161003 || sourceid == 32121006 || sourceid == 1076 || sourceid == 11076); // poison mist, smokescreen and flame gear, recovery aura
    }

    private final boolean isSpiritClaw() {
        return skill && sourceid == 4121006;
    }

    private final boolean isDispel() {
        return skill && (sourceid == 2311001 || sourceid == 9001000 || sourceid == 9101000);
    }

    private final boolean isHeroWill() {
        switch (sourceid) {
            case 1121011:
            case 1221012:
            case 1321010:
            case 2121008:
            case 2221008:
            case 2321009:
            case 3121009:
            case 3221008:
            case 4121009:
            case 4221008:
            case 5121008:
            case 5221010:
            case 21121008:
            case 22171004:
            case 4341008:
            case 32121008:
            case 33121008:
            case 35121008:
            case 5321008:
            case 23121008:
                return skill;
        }
        return false;
    }

    public final boolean isAranCombo() {
        return sourceid == 21000000;
    }

    public final boolean isCombo() {
        switch (sourceid) {
            case 1111002:
            case 11111001: // Combo
                return skill;
        }
        return false;
    }

    public final boolean isHoming() {
        switch (sourceid) {
            case 5211006: // Homing Beacon
            case 22151002: //killer wings
            case 5220011: // Bullseye
                return skill;
        }
        return false;
    }

    public final boolean isPirateMorph() {
        switch (sourceid) {
            case 13111005:
            case 15111002:
            case 5111005:
            case 5121003:
                return skill;
        }
        return false;
    }

    public final boolean isMorph() {
        return morphId > 0;
    }

    public final int getMorph() {
        switch (sourceid) {
            case 15111002:
            case 5111005:
                return 1000;
            case 5121003:
                return 1001;
            case 5101007:
                return 1002;
            case 13111005:
                return 1003;
        }
        return morphId;
    }

    public final boolean isDivineBody() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1010;
    }

    public final boolean isDivineShield() {
        switch (sourceid) {
            case 1220013:
                return skill;
        }
        return false;
    }

    public final boolean isBerserkFury() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1011;
    }

    public final int getMorph(final MapleCharacter chr) {
        final int morph = getMorph();
        switch (morph) {
            case 1000:
            case 1001:
            case 1003:
                return morph + (chr.getGender() == 1 ? 100 : 0);
        }
        return morph;
    }

    public final byte getLevel() {
        return level;
    }

    public final SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case 3211002: // puppet sniper
            case 3111002: // puppet ranger
            case 33111003:
            case 13111004: // puppet cygnus
            case 5211001: // octopus - pirate
            case 5220002: // advanced octopus - pirate
            case 4341006:
            case 35111002:
            case 35111005:
            case 35111011:
            case 35121009:
            case 35121010:
            case 35121011:
            case 4111007: //dark flare
            case 4211007: //dark flare
            case 33101008:
            case 35121003:
            case 3120012:
            case 3220012:
            case 5321003:
                return SummonMovementType.STATIONARY;
            case 3211005: // golden eagle
            case 3111005: // golden hawk
            case 3101007:
            case 3201007:
            case 33111005:
            case 2311006: // summon dragon
            case 3221005: // frostprey
            case 3121006: // phoenix
            case 23111008:
            case 23111009:
            case 23111010:
                return SummonMovementType.CIRCLE_FOLLOW;
            case 5211002: // bird - pirate
                return SummonMovementType.CIRCLE_STATIONARY;
            case 32111006: //reaper
                return SummonMovementType.WALK_STATIONARY;
            case 1321007: // beholder
            case 2121005: // elquines
            case 2221005: // ifrit
            case 2321003: // bahamut
            case 12111004: // Ifrit
            case 11001004: // soul
            case 12001004: // flame
            case 13001004: // storm
            case 14001005: // darkness
            case 15001004: // lightning
            case 35111001:
            case 35111010:
            case 35111009:
            case 5321004:
                return SummonMovementType.FOLLOW;
        }
        if (isAngel()) {
            return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public final boolean isAngel() {
        return GameConstants.isAngel(sourceid);
    }

    public final boolean isSkill() {
        return skill;
    }

    public final int getSourceId() {
        return sourceid;
    }

    public final boolean isIceKnight() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1105;
    }

    public final boolean isSoaring() {
        return isSoaring_Normal() || isSoaring_Mount();
    }

    public final boolean isSoaring_Normal() {
        return skill && GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1026;
    }

    public final boolean isSoaring_Mount() {
        return skill && ((GameConstants.isBeginnerJob(sourceid / 10000) && sourceid % 10000 == 1142) || sourceid == 80001089);
    }

    public final boolean isFinalAttack() {
        switch (sourceid) {
            case 13101002:
            case 11101002:
                return skill;
        }
        return false;
    }

    public final boolean isMistEruption() {
        switch (sourceid) {
            case 2121003:
                return skill;
        }
        return false;
    }

    public final boolean isShadow() {
        switch (sourceid) {
            case 4111002: // shadowpartner
            case 14111000: // cygnus
            case 4211008:
            case 4331002:
                return skill;
        }
        return false;
    }

    public final boolean isMechPassive() {
        switch (sourceid) {
            case 35121013:
                return true;
        }
        return false;
    }

    /**
     *
     * @return true if the effect should happen based on it's probablity, false
     * otherwise
     */
    public final boolean makeChanceResult() {
        return prop >= 100 || Randomizer.nextInt(100) < prop;
    }

    public final short getProb() {
        return prop;
    }

    public final short getIgnoreMob() {
        return ignoreMob;
    }

    public final int getEnhancedHP() {
        return ehp;
    }

    public final int getEnhancedMP() {
        return emp;
    }

    public final int getEnhancedWatk() {
        return ewatk;
    }

    public final int getEnhancedWdef() {
        return ewdef;
    }

    public final int getEnhancedMdef() {
        return emdef;
    }

    public final short getDOT() {
        return dot;
    }

    public final short getDOTTime() {
        return dotTime;
    }

    public final short getCriticalMax() {
        return criticaldamageMax;
    }

    public final short getCriticalMin() {
        return criticaldamageMin;
    }

    public final short getASRRate() {
        return asrR;
    }

    public final short getTeRRate() {
        return terR;
    }

    public final short getDAMRate() {
        return damR;
    }

    public final short getMesoRate() {
        return mesoR;
    }

    public final int getEXP() {
        return exp;
    }

    public final short getAttackX() {
        return padX;
    }

    public final short getMagicX() {
        return madX;
    }

    public final int getPercentHP() {
        return mhpR;
    }

    public final int getPercentMP() {
        return mmpR;
    }

    public final int getConsume() {
        return consumeOnPickup;
    }

    public final int getSelfDestruction() {
        return selfDestruction;
    }

    public final int getCharColor() {
        return charColor;
    }

    public final List<Integer> getPetsCanConsume() {
        return petsCanConsume;
    }

    public final boolean isReturnScroll() {
        return skill && (sourceid == 80001040 || sourceid == 20021110);
    }

    public final boolean isMechChange() {
        switch (sourceid) {
            case 35111004: //siege
            case 35001001: //flame
            case 35101009:
            case 35121013:
            case 35121005:
                return skill;
        }
        return false;
    }

    public final int getRange() {
        return range;
    }

    public final short getER() {
        return er;
    }

    public final int getPrice() {
        return price;
    }

    public final int getExtendPrice() {
        return extendPrice;
    }

    public final byte getPeriod() {
        return period;
    }

    public final byte getReqGuildLevel() {
        return reqGuildLevel;
    }

    public final byte getEXPRate() {
        return expR;
    }

    public final short getLifeID() {
        return lifeId;
    }

    public final short getUseLevel() {
        return useLevel;
    }

    public final byte getSlotCount() {
        return slotCount;
    }

    public final short getStr() {
        return str;
    }

    public final short getStrX() {
        return strX;
    }

    public final short getDex() {
        return dex;
    }

    public final short getDexX() {
        return dexX;
    }

    public final short getInt() {
        return int_;
    }

    public final short getIntX() {
        return intX;
    }

    public final short getLuk() {
        return luk;
    }

    public final short getLukX() {
        return lukX;
    }

    public final short getMPConReduce() {
        return mpConReduce;
    }

    public final short getIndieMHp() {
        return indieMhp;
    }

    public final short getIndieMMp() {
        return indieMmp;
    }

    public final short getIndieAllStat() {
        return indieAllStat;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public final byte getType() {
        return type;
    }

    public int getBossDamage() {
        return bdR;
    }

    public int getInterval() {
        return interval;
    }

    public ArrayList<Pair<Integer, Integer>> getAvailableMaps() {
        return availableMap;
    }

    public short getWDEFRate() {
        return pddR;
    }

    public short getMDEFRate() {
        return mddR;
    }

    public boolean isForce() {
        return sourceid == 31000004 || sourceid == 31001006 || sourceid == 31001007 || sourceid == 31001008;
    }

    public boolean isSubTime(int sourceid) {
        switch (sourceid) {
            case 31121005:
            case 31121003:
            case 31101003:
                return true;
        }
        return false;
    }

    public int getSubTime() {
        return subTime;
    }

    public static class CancelEffectAction implements Runnable {

        private final MapleStatEffect effect;
        private final WeakReference<MapleCharacter> target;
        private final long startTime;
        private final Map<MapleBuffStat, Integer> statup;

        public CancelEffectAction(final MapleCharacter target, final MapleStatEffect effect, final long startTime, final Map<MapleBuffStat, Integer> statup) {
            this.effect = effect;
            this.target = new WeakReference<MapleCharacter>(target);
            this.startTime = startTime;
            this.statup = statup;
        }

        @Override
        public void run() {
            final MapleCharacter realTarget = target.get();
            if (realTarget != null && !realTarget.isClone()) {
                realTarget.cancelEffect(effect, false, startTime, statup);
            }
        }
    }
}
