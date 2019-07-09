package client;

import client.status.MonsterStatus;
import constants.GameConstants;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Randomizer;
import tools.StringUtil;
import tools.Triple;

public class SkillFactory {

    private static final Map<Integer, Skill> skills = new HashMap<Integer, Skill>();
    private static final Map<String, Integer> delays = new HashMap<String, Integer>();
    private static final Map<Integer, CraftingEntry> crafts = new HashMap<Integer, CraftingEntry>();
    private static final Map<Integer, FamiliarEntry> familiars = new HashMap<Integer, FamiliarEntry>();
    private static final Map<Integer, List<Integer>> skillsByJob = new HashMap<Integer, List<Integer>>();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<Integer, SummonSkillEntry>();

    public static void load() {
        final MapleData delayData = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Character.wz")).getData("00002000.img");
        final MapleData stringData = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/String.wz")).getData("Skill.img");
        final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Skill.wz"));
        final MapleDataDirectoryEntry root = datasource.getRoot();
        int del = 0; //buster is 67 but its the 57th one!
        for (MapleData delay : delayData) {
            if (!delay.getName().equals("info")) {
                delays.put(delay.getName(), del);
                del++;
            }
        }

        int skillid;
        MapleData summon_data;
        SummonSkillEntry sse;

        for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
            if (topDir.getName().length() <= 8) {
                for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) { // Loop thru each jobs
                            if (data2 != null) {
                                skillid = Integer.parseInt(data2.getName());
                                Skill skil = Skill.loadFromData(skillid, data2, delayData);
                                List<Integer> job = skillsByJob.get(skillid / 10000);
                                if (job == null) {
                                    job = new ArrayList<Integer>();
                                    skillsByJob.put(skillid / 10000, job);
                                }
                                job.add(skillid);
                                skil.setName(getName(skillid, stringData));
                                skills.put(skillid, skil);
                                summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    sse = new SummonSkillEntry();
                                    sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                    sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                    sse.attackCount = (byte) MapleDataTool.getInt("attackCount", summon_data, 1);
                                    if (summon_data.getChildByPath("range/lt") != null) {
                                        final MapleData ltd = summon_data.getChildByPath("range/lt");
                                        sse.lt = (Point) ltd.getData();
                                        sse.rb = (Point) summon_data.getChildByPath("range/rb").getData();
                                    } else {
                                        sse.lt = new Point(-100, -100);
                                        sse.rb = new Point(100, 100);
                                    }
                                    //sse.range = (short) MapleDataTool.getInt("range/r", summon_data, 0);
                                    sse.delay = MapleDataTool.getInt("effectAfter", summon_data, 0) + MapleDataTool.getInt("attackAfter", summon_data, 0);
                                    for (MapleData effect : summon_data) {
                                        if (effect.getChildren().size() > 0) {
                                            for (final MapleData effectEntry : effect) {
                                                sse.delay += MapleDataTool.getIntConvert("delay", effectEntry, 0);
                                            }
                                        }
                                    }
                                    for (MapleData effect : data2.getChildByPath("summon/attack1")) {
                                        sse.delay += MapleDataTool.getIntConvert("delay", effect, 0);
                                    }
                                    SummonSkillInformation.put(skillid, sse);
                                }
                            }
                        }
                    }
                }
            } else if (topDir.getName().startsWith("Familiar")) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    skillid = Integer.parseInt(data.getName());
                    FamiliarEntry skil = new FamiliarEntry();
                    skil.prop = (byte) MapleDataTool.getInt("prop", data, 0);
                    skil.time = (byte) MapleDataTool.getInt("time", data, 0);
                    skil.attackCount = (byte) MapleDataTool.getInt("attackCount", data, 1);
                    skil.targetCount = (byte) MapleDataTool.getInt("targetCount", data, 1);
                    skil.speed = (byte) MapleDataTool.getInt("speed", data, 1);
                    skil.knockback = MapleDataTool.getInt("knockback", data, 0) > 0 || MapleDataTool.getInt("attract", data, 0) > 0;
                    if (data.getChildByPath("lt") != null) {
                        skil.lt = (Point) data.getChildByPath("lt").getData();
                        skil.rb = (Point) data.getChildByPath("rb").getData();
                    }
                    if (MapleDataTool.getInt("stun", data, 0) > 0) {
                        skil.status.add(MonsterStatus.STUN);
                    }
//                  if (MapleDataTool.getInt("poison", data, 0) > 0) {
//                      status.add(MonsterStatus.POISON);
//                  }
                    if (MapleDataTool.getInt("slow", data, 0) > 0) {
                        skil.status.add(MonsterStatus.SPEED);
                    }
                    familiars.put(skillid, skil);
                }
            } else if (topDir.getName().startsWith("Recipe")) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    skillid = Integer.parseInt(data.getName());
                    CraftingEntry skil = new CraftingEntry(skillid, (byte) MapleDataTool.getInt("incFatigability", data, 0), (byte) MapleDataTool.getInt("reqSkillLevel", data, 0), (byte) MapleDataTool.getInt("incSkillProficiency", data, 0), MapleDataTool.getInt("needOpenItem", data, 0) > 0, MapleDataTool.getInt("period", data, 0));
                    for (MapleData d : data.getChildByPath("target")) {
                        skil.targetItems.add(new Triple<Integer, Integer, Integer>(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0), MapleDataTool.getInt("probWeight", d, 0)));
                    }
                    for (MapleData d : data.getChildByPath("recipe")) {
                        skil.reqItems.put(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0));
                    }
                    crafts.put(skillid, skil);
                }
            }
        }
    }

    public static List<Integer> getSkillsByJob(final int jobId) {
        return skillsByJob.get(jobId);
    }

    public static String getSkillName(final int id) {
        Skill skil = getSkill(id);
        if (skil != null) {
            return skil.getName();
        }
        return null;
    }

    public static Integer getDelay(final String id) {
        if (Delay.fromString(id) != null) {
            return Delay.fromString(id).i;
        }
        return delays.get(id);
    }

    private static String getName(final int id, final MapleData stringData) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return "";
    }

    public static SummonSkillEntry getSummonData(final int skillid) {
        return SummonSkillInformation.get(skillid);
    }

    public static Collection<Skill> getAllSkills() {
        return skills.values();
    }

    public static Skill getSkill(final int id) {
        if (!skills.isEmpty()) {
            if (id >= 92000000 && crafts.containsKey(Integer.valueOf(id))) { //92000000
                return crafts.get(Integer.valueOf(id));
            }
            return skills.get(Integer.valueOf(id));
        }

        return null;
    }

    public static CraftingEntry getCraft(final int id) {
        if (!crafts.isEmpty()) {
            return crafts.get(Integer.valueOf(id));
        }
        return null;
    }

    public static FamiliarEntry getFamiliar(final int id) {
        if (!familiars.isEmpty()) {
            return familiars.get(Integer.valueOf(id));
        }
        return null;
    }

    public static long getDefaultSExpiry(final Skill skill) {
        if (skill == null) { //idk why it need to be null
            return 1;
        }
        return (skill.isTimeLimited() ? (System.currentTimeMillis() + (long) (30L * 24L * 60L * 60L * 1000L)) : -1);
    } 

    public static class CraftingEntry extends Skill {
   
        public boolean needOpenItem;
        public int period;
        public byte incFatigability, reqSkillLevel, incSkillProficiency;
        public List<Triple<Integer, Integer, Integer>> targetItems = new ArrayList<Triple<Integer, Integer, Integer>>(); // itemId / amount / probability
        public Map<Integer, Integer> reqItems = new HashMap<Integer, Integer>(); // itemId / amount

        public CraftingEntry(int id, byte incFatigability, byte reqSkillLevel, byte incSkillProficiency, boolean needOpenItem, int period) {
            super(id);
            this.incFatigability = incFatigability;
            this.reqSkillLevel = reqSkillLevel;
            this.incSkillProficiency = incSkillProficiency;
            this.needOpenItem = needOpenItem;
            this.period = period;
        }
    }

    public static class FamiliarEntry {
        public byte prop, time, attackCount, targetCount, speed;
        public Point lt, rb;
        public boolean knockback;
        public EnumSet<MonsterStatus> status = EnumSet.noneOf(MonsterStatus.class);

        public final boolean makeChanceResult() {
            return prop >= 100 || Randomizer.nextInt(100) < prop;
        }
    }

    public static enum Delay {
        walk1(0x00),
        walk2(0x01),
        stand1(0x02),
        stand2(0x03),
        alert(0x04),
        swingO1(0x05),
        swingO2(0x06),
        swingO3(0x07),
        swingOF(0x08),
        swingT1(0x09),
        swingT2(0x0A),
        swingT3(0x0B),
        swingTF(0x0C),
        swingP1(0x0D),
        swingP2(0x0E),
        swingPF(0x0F),
        stabO1(0x10),
        stabO2(0x11),
        stabOF(0x12),
        stabT1(0x13),
        stabT2(0x14),
        stabTF(0x15),
        swingD1(0x16),
        swingD2(0x17),
        stabD1(0x18),
        swingDb1(0x19),
        swingDb2(0x1A),
        swingC1(0x1B),
        swingC2(0x1C),
        rushBoom(0x1C),
        tripleBlow(GameConstants.GMS ? 0x1D : 0x1E),
        quadBlow(GameConstants.GMS ? 0x1E : 0x1F), // sea
        deathBlow(GameConstants.GMS ? 0x1F : 0x20),
        finishBlow(GameConstants.GMS ? 0x20 : 0x21), // sea
        finishAttack(GameConstants.GMS ? 0x21 : 0x22), // sea
        finishAttack_link(GameConstants.GMS ? 0x22 : 0x23),
        finishAttack_link2(GameConstants.GMS ? 0x22 : 0x24),
        shoot1(GameConstants.GMS ? 0x23 : 0x1F),
        shoot2(GameConstants.GMS ? 0x24 : 0x20),
        shootF(GameConstants.GMS ? 0x25 : 0x21),
        shootDb2(0x28),
        shotC1(0x29),
        dash(GameConstants.GMS ? 0x2B : 0x25),
        dash2(GameConstants.GMS ? 0x2C : 0x26), //hack. doesn't really exist
        proneStab(GameConstants.GMS ? 0x2F : 0x29),
        prone(GameConstants.GMS ? 0x30 : 0x2A),
        heal(GameConstants.GMS ? 0x31 : 0x2B), // sea
        fly(GameConstants.GMS ? 0x32 : 0x2C),
        jump(GameConstants.GMS ? 0x33 : 0x2D),
        sit(GameConstants.GMS ? 0x34 : 0x2E),
        rope(GameConstants.GMS ? 0x35 : 0x2F),
        dead(GameConstants.GMS ? 0x36 : 0x30),
        ladder(GameConstants.GMS ? 0x37 : 0x31),
        rain(GameConstants.GMS ? 0x38 : 0x32),
        alert2(GameConstants.GMS ? 0x40 : 0x34),
        alert3(GameConstants.GMS ? 0x41 : 0x35),
        alert4(GameConstants.GMS ? 0x42 : 0x36),
        alert5(GameConstants.GMS ? 0x43 : 0x37),
        alert6(GameConstants.GMS ? 0x44 : 0x38),
        alert7(GameConstants.GMS ? 0x45 : 0x39),
        ladder2(GameConstants.GMS ? 0x46 : 0x3A),
        rope2(GameConstants.GMS ? 0x47 : 0x3B),
        shoot6(GameConstants.GMS ? 0x48 : 0x3C),
        magic1(GameConstants.GMS ? 0x49 : 0x3D),
        magic2(GameConstants.GMS ? 0x4A : 0x3E),
        magic3(GameConstants.GMS ? 0x4B : 0x3F),
        magic5(GameConstants.GMS ? 0x4C : 0x40),
        magic6(GameConstants.GMS ? 0x4D : 0x41),
        explosion(GameConstants.GMS ? 0x4D : 0x4E), // sea
        burster1(GameConstants.GMS ? 0x4E : 0x42),
        burster2(GameConstants.GMS ? 0x4F : 0x43),
        savage(GameConstants.GMS ? 0x50 : 0x4A),
        avenger(GameConstants.GMS ? 0x51 : 0x4C), // sea
        assaulter(GameConstants.GMS ? 0x52 : 0x4D), // sea
        prone2(GameConstants.GMS ? 0x53 : 0x47),
        assassination(GameConstants.GMS ? 0x54 : 0x4E),
        assassinationS(GameConstants.GMS ? 0x55 : 0x49),
        tornadoDash(GameConstants.GMS ? 0x58 : 0x4C),
        tornadoDashStop(GameConstants.GMS ? 0x58 : 0x4C),
        tornadoRush(GameConstants.GMS ? 0x58 : 0x4C),
        rush(GameConstants.GMS ? 0x59 : 0x55), // sea
        rush2(GameConstants.GMS ? 0x5A : 0x56), // sea
        brandish1(GameConstants.GMS ? 0x5B : 0x56),
        brandish2(GameConstants.GMS ? 0x5C : 0x50),
        braveSlash(GameConstants.GMS ? 0x5D : 0x51),
        braveslash1(GameConstants.GMS ? 0x5D : 0x51),
        braveslash2(GameConstants.GMS ? 0x5E : 0x51),
        braveslash3(GameConstants.GMS ? 0x5F : 0x51),
        braveslash4(GameConstants.GMS ? 0x60 : 0x51),
        darkImpale(GameConstants.GMS ? 0x61 : 0x5C), // sea
        sanctuary(GameConstants.GMS ? 0x62 : 0x5D), // sea
        meteor(GameConstants.GMS ? 0x63 : 0x5E), // sea
        paralyze(GameConstants.GMS ? 0x64 : 0x54),
        blizzard(GameConstants.GMS ? 0x65 : 0x60), // sea
        genesis(GameConstants.GMS ? 0x66 : 0x61), // sea
        blast(GameConstants.GMS ? 0x69 : 0x64), // sea
        smokeshell(GameConstants.GMS ? 0x6A : 0x59),
        showdown(GameConstants.GMS ? 0x6B : 0x66), // sea
        ninjastorm(GameConstants.GMS ? 0x6C : 0x63), // sea
        chainlightning(GameConstants.GMS ? 0x6D : 0x68), // sea
        holyshield(GameConstants.GMS ? 0x6E : 0x5D),
        resurrection(GameConstants.GMS ? 0x6F : 0x5E),
        somersault(GameConstants.GMS ? 0x70 : 0x6B), // sea
        straight(GameConstants.GMS ? 0x71 : 0x6C), // sea
        eburster(GameConstants.GMS ? 0x72 : 0x6D),
        backspin(GameConstants.GMS ? 0x73 : 0x6E), // sea
        eorb(GameConstants.GMS ? 0x74 : 0x6F),
        screw(GameConstants.GMS ? 0x75 : 0x70), // sea
        doubleupper(GameConstants.GMS ? 0x76 : 0x71),
        dragonstrike(GameConstants.GMS ? 0x77 : 0x72),
        doublefire(GameConstants.GMS ? 0x78 : 0x73), // sea
        triplefire(GameConstants.GMS ? 0x79 : 0x74), // sea
	fake(GameConstants.GMS ? 0x7A : 0x75),
        airstrike(GameConstants.GMS ? 0x7B : 0x76), //sea
        edrain(GameConstants.GMS ? 0x7C : 0x77),
        octopus(GameConstants.GMS ? 0x7D : 0x78),
        backstep(GameConstants.GMS ? 0x7E : 0x79), // sea
        shot(GameConstants.GMS ? 0x7F : 0x7A), // sea
        rapidfire(GameConstants.GMS ? 0x7F : 0x7A), // sea
        fireburner(GameConstants.GMS ? 0x81 : 0x7B), // sea
        coolingeffect(GameConstants.GMS ? 0x82 : 0x7C), // sea
        fist(GameConstants.GMS ? 0x84 : 0x7E),
        timeleap(GameConstants.GMS ? 0x85 : 0x7F),
        homing(GameConstants.GMS ? 0x86 : 0x81), //sea
        ghostwalk(GameConstants.GMS ? 0x87 : 0x82),
        ghoststand(GameConstants.GMS ? 0x88 : 0x83),
        ghostjump(GameConstants.GMS ? 0x89 : 0x84),
        ghostproneStab(GameConstants.GMS ? 0x8A : 0x85),
        ghostladder(GameConstants.GMS ? 0x8B : 0x86),
        ghostrope(GameConstants.GMS ? 0x8C : 0x87),
        ghostfly(GameConstants.GMS ? 0x8D : 0x88),
        ghostsit(GameConstants.GMS ? 0x8E : 0x89),
        cannon(GameConstants.GMS ? 0x8F : 0x8A),
        torpedo(GameConstants.GMS ? 0x90 : 0x8B),
        darksight(GameConstants.GMS ? 0x91 : 0x8C),
        bamboo(GameConstants.GMS ? 0x92 : 0x8D),
        pyramid(GameConstants.GMS ? 0x93 : 0x8E),
        wave(GameConstants.GMS ? 0x94 : 0x8F), // sea
        blade(GameConstants.GMS ? 0x95 : 0x90),
        souldriver(GameConstants.GMS ? 0x96 : 0x91), // sea
        firestrike(GameConstants.GMS ? 0x97 : 0x92), // sea
        flamegear(GameConstants.GMS ? 0x98 : 0x93), // sea
        stormbreak(GameConstants.GMS ? 0x99 : 0x94), // sea
        vampire(GameConstants.GMS ? 0x9A : 0x95), // sea
        swingT2PoleArm(GameConstants.GMS ? 0x9C : 0x97),
        swingP1PoleArm(GameConstants.GMS ? 0x9D : 0x98),
        swingP2PoleArm(GameConstants.GMS ? 0x9E : 0x99),
        doubleSwing(GameConstants.GMS ? 0x9F : 0x9A),
        tripleSwing(GameConstants.GMS ? 0xA0 : 0x9B),
        fullSwingDouble(GameConstants.GMS ? 0xA1 : 0x9C),
        fullSwingTriple(GameConstants.GMS ? 0xA2 : 0x9D),
        overSwingDouble(GameConstants.GMS ? 0xA3 : 0x9E),
        overSwingTriple(GameConstants.GMS ? 0xA4 : 0x9F),
        rollingSpin(GameConstants.GMS ? 0xA5 : 0xA0), // sea
        comboSmash(GameConstants.GMS ? 0xA6 : 0xA1), // sea
        comboFenrir(GameConstants.GMS ? 0xA7 : 0xA2), // sea
        comboTempest(GameConstants.GMS ? 0xA8 : 0xA3), // sea
        finalCharge(GameConstants.GMS ? 0xA9 : 0xA4), // sea
        finalBlow(GameConstants.GMS ? 0xAB : 0xA6), // sea
        finalToss(GameConstants.GMS ? 0xAC : 0xA7), // sea
        magicmissile(GameConstants.GMS ? 0xAD : 0xA8), // sea
        lightningBolt(GameConstants.GMS ? 0xAE : 0xA9), // sea
        dragonBreathe(GameConstants.GMS ? 0xAF : 0xAA),
        breathe_prepare(GameConstants.GMS ? 0xB0 : 0xAB),
        dragonIceBreathe(GameConstants.GMS ? 0xB1 : 0xAC),
        icebreathe_prepare(GameConstants.GMS ? 0xB2 : 0xAD),
        blaze(GameConstants.GMS ? 0xB3 : 0xAE),
        fireCircle(GameConstants.GMS ? 0xB4 : 0xAF), // sea
        illusion(GameConstants.GMS ? 0xB5 : 0xB0), // sea
        magicFlare(GameConstants.GMS ? 0xB6 : 0xB1), // sea
        elementalReset(GameConstants.GMS ? 0xB7 : 0xB2),
        magicRegistance(GameConstants.GMS ? 0xB8 : 0xB3),
        magicBooster(GameConstants.GMS ? 0xB9 : 0xB4),
        magicShield(GameConstants.GMS ? 0xBA : 0xB5),
        recoveryAura(GameConstants.GMS ? 0xBB : 0xB6),
        flameWheel(GameConstants.GMS ? 0xBC : 0xB7), // sea
        killingWing(GameConstants.GMS ? 0xBD : 0xB8), // sea
        OnixBlessing(GameConstants.GMS ? 0xBE : 0xB9),
        Earthquake(GameConstants.GMS ? 0xBF : 0xBA), // sea
        soulStone(GameConstants.GMS ? 0xC0 : 0xBB),
        dragonThrust(GameConstants.GMS ? 0xC1 : 0xBC), // sea
        ghostLettering(GameConstants.GMS ? 0xC2 : 0xBD),
        darkFog(GameConstants.GMS ? 0xC3 : 0xBE), // sea
        slow(GameConstants.GMS ? 0xC4 : 0xBF),
        mapleHero(GameConstants.GMS ? 0xC5 : 0xC0),
        Awakening(GameConstants.GMS ? 0xC6 : 0xC1),
        flyingAssaulter(GameConstants.GMS ? 0xC7 : 0xC2), // sea
        tripleStab(GameConstants.GMS ? 0xC8 : 0xC3), // sea
        fatalBlow(GameConstants.GMS ? 0xC9 : 0xC4), // sea
        slashStorm1(GameConstants.GMS ? 0xCA : 0xc5), // sea
        slashStorm2(GameConstants.GMS ? 0xCB : 0xC6), // sea
        bloodyStorm(GameConstants.GMS ? 0xCC : 0xC7), // sea
        flashBang(GameConstants.GMS ? 0xCD : 0xC8), // sea
        upperStab(GameConstants.GMS ? 0xCE : 0xC9), // sea 
        bladeFury(GameConstants.GMS ? 0xCF : 0xCA), // sea
        chainPull(GameConstants.GMS ? 0xD1 : 0xCC), //sea
        chainAttack(GameConstants.GMS ? 0xD1 : 0xCC), // sea
        owlDead(GameConstants.GMS ? 0xD2 : 0xCD), // sea
        monsterBombPrepare(GameConstants.GMS ? 0xD4 : 0xCF),
        monsterBombThrow(GameConstants.GMS ? 0xD4 : 0xCF),
        finalCut(GameConstants.GMS ? 0xD5 : 0xD0), //sea
        finalCutPrepare(GameConstants.GMS ? 0xD5 : 0xD0), // sea
        suddenRaid(GameConstants.GMS ? 0xD7 : 0xD2), //idk, not in data anymore
        fly2(GameConstants.GMS ? 0xD8 : 0xD3),
        fly2Move(GameConstants.GMS ? 0xD9 : 0xD4),
        fly2Skill(GameConstants.GMS ? 0xDA : 0xD5),
        knockback(GameConstants.GMS ? 0xDB : 0xD6),
        rbooster_pre(GameConstants.GMS ? 0xDF : 0xDA),
        rbooster(GameConstants.GMS ? 0xDF : 0xDA),
        rbooster_after(GameConstants.GMS ? 0xDF : 0xDA),
        crossRoad(GameConstants.GMS ? 0xE2 : 0xDD),
        nemesis(GameConstants.GMS ? 0xE3 : 0xDE),
        tank(GameConstants.GMS ? 0xEA : 0xE5),
        tank_laser(GameConstants.GMS ? 0xEE : 0xE9), // sea
        siege_pre(GameConstants.GMS ? 0xF0 : 0xEA),
        tank_siegepre(GameConstants.GMS ? 0xF0 : 0xEA), //just to make it work with the skill, these two
        sonicBoom(GameConstants.GMS ? 0xF3 : 0xED),
        darkLightning(GameConstants.GMS ? 0xF5 : 0xEF),
        darkChain(GameConstants.GMS ? 0xF6 : 0xF0), //sea
        cyclone_pre(0),
        cyclone(0), //energy attack
        glacialchain(GameConstants.GMS ? 0xF7 : 0xF1), // sea
        flamethrower(GameConstants.GMS ? 0xFB : 0xF5),
        flamethrower_pre(GameConstants.GMS ? 0xFB : 0xF5),
        flamethrower2(GameConstants.GMS ? 0xFC : 0xF6),
        flamethrower_pre2(GameConstants.GMS ? 0xFC : 0xF6),
        gatlingshot(GameConstants.GMS ? 0x101 : 0xFE),
        gatlingshot2(GameConstants.GMS ? 0x102 : 0xFF),
        drillrush(GameConstants.GMS ? 0x103 : 0x100),
        earthslug(GameConstants.GMS ? 0x104 : 0x101),
        rpunch(GameConstants.GMS ? 0x105 : 0x102),
        clawCut(GameConstants.GMS ? 0x106 : 0x103),
        swallow(GameConstants.GMS ? 0x109 : 0x106),
        swallow_attack(GameConstants.GMS ? 0x109 : 0x106),
        swallow_loop(GameConstants.GMS ? 0x109 : 0x106),
        flashRain(GameConstants.GMS ? 0x111 : 0x108),
        OnixProtection(GameConstants.GMS ? 0x11C : 0x117),
        OnixWill(GameConstants.GMS ? 0x11D : 0x118),
        phantomBlow(GameConstants.GMS ? 0x11E : 0x119),
        comboJudgement(GameConstants.GMS ? 0x11F : 0x11A), // sea
        arrowRain(GameConstants.GMS ? 0x120 : 0x11B), // sea
        arrowEruption(GameConstants.GMS ? 0x121 : 0x10D),
        iceStrike(GameConstants.GMS ? 0x122 : 0x10E),
        swingT2Giant(GameConstants.GMS ? 0x125 : 0x111),
        cannonJump(GameConstants.GMS ? 0x127 : 0x122), // sea
        swiftShot(0x128),
        giganticBackstep(0x12A),
        mistEruption(0x12B),
        cannonSmash(0x12C),
        cannonSlam(0x12D),
        flamesplash(0x12E),
        noiseWave(GameConstants.GMS ? 0x132 : 0x12C),
        superCannon(0x136),
        jShot(0x138),
        demonSlasher(GameConstants.GMS ? 0x139 : 0x149),
        bombExplosion(0x13A),
        cannonSpike(GameConstants.GMS ? 0x13B : 0x135),
        speedDualShot(0x13C),
        strikeDual(0x13D),
        bluntSmash(0x13F),
        crossPiercing(0x140),
        piercing(0x141),
        elfTornado(0x143),
        immolation(0x144),
        multiSniping(0x147),
        windEffect(0x148),
        elfrush(0x149),
        elfrush2(0x149),
        dealingRush(0x14E),
        maxForce0(0x150),
        maxForce1(0x151),
        maxForce2(0x152),
        maxForce3(0x153),
        //special: pirate morph attacks
        iceAttack1(GameConstants.GMS ? 0x158 : 0x112),
        iceAttack2(GameConstants.GMS ? 0x159 : 0x113),
        iceSmash(GameConstants.GMS ? 0x15A : 0x114),
        iceTempest(GameConstants.GMS ? 0x15B : 0x115),
        iceChop(GameConstants.GMS ? 0x15C : 0x116),
        icePanic(GameConstants.GMS ? 0x15D : 0x117),
        iceDoubleJump(GameConstants.GMS ? 0x15E : 0x118),
        shockwave(GameConstants.GMS ? 0x169 : 0x182), //sea 
        demolition(GameConstants.GMS ? 0x16A : 0x183), // sea
        snatch(GameConstants.GMS ? 0x16B : 0x184), // sea
        windspear(GameConstants.GMS ? 0x16C : 0x127),
        windshot(GameConstants.GMS ? 0x16D : 0x128);
        public int i;

        private Delay(int i) {
            this.i = i;
        }

        public static Delay fromString(String s) {
            for (Delay b : Delay.values()) {
                if (b.name().equalsIgnoreCase(s)) {
                    return b;
                }
            }
            return null;
        }
    }
}
