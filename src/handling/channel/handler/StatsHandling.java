package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import java.util.EnumMap;
import java.util.Map;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;

public class StatsHandling {

    public static final void DistributeAP(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        Map<MapleStat, Integer> statupdate = new EnumMap<>(MapleStat.class);
        c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
        chr.updateTick(slea.readInt());

        final PlayerStats stat = chr.getStat();
        final int job = chr.getJob();
        if (chr.getRemainingAp() > 0) {
            switch (slea.readInt()) {
                case 0x100: // Str
                    if (stat.getStr() >= 999) {
                        return;
                    }
                    stat.setStr((short) (stat.getStr() + 1), chr);
                    statupdate.put(MapleStat.STR, (int) stat.getStr());
                    break;
                case 0x200: // Dex
                    if (stat.getDex() >= 999) {
                        return;
                    }
                    stat.setDex((short) (stat.getDex() + 1), chr);
                    statupdate.put(MapleStat.DEX, (int) stat.getDex());
                    break;
                case 0x400: // Int
                    if (stat.getInt() >= 999) {
                        return;
                    }
                    stat.setInt((short) (stat.getInt() + 1), chr);
                    statupdate.put(MapleStat.INT, (int) stat.getInt());
                    break;
                case 0x800: // Luk
                    if (stat.getLuk() >= 999) {
                        return;
                    }
                    stat.setLuk((short) (stat.getLuk() + 1), chr);
                    statupdate.put(MapleStat.LUK, (int) stat.getLuk());
                    break;
                case 0x2000: // HP
                    int maxhp = stat.getMaxHp();
                    if (chr.getHpApUsed() >= 10000 || maxhp >= 30000) {
                        return;
                    }
                    Skill improvingMaxHP;
                    int improvingMaxHPLevel;
                    if (GameConstants.isBeginnerJob(job)) { // Beginner
                        maxhp += Randomizer.rand(8, 12);
                    } else if (job >= 100 && job <= 132) { // Warrior
                        improvingMaxHP = SkillFactory.getSkill(1000001);
                        improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                        if (improvingMaxHPLevel >= 1) {
                            maxhp += Randomizer.rand(20, 24) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                        } else {
                            maxhp += Randomizer.rand(20, 24);
                        }
                        //maxhp += Randomizer.rand(36, 42);
                    } else if (job >= 200 && job <= 232) { // Magician
                        maxhp += Randomizer.rand(10, 20);
                    } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434)) { // Bowman
                        maxhp += Randomizer.rand(16, 20);
                    } else if ((job >= 510 && job <= 512)) {
                        maxhp += Randomizer.rand(28, 32);
                    } else if ((job >= 500 && job <= 532)) { // Pirate
                        improvingMaxHP = SkillFactory.getSkill(5100000);
                        improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                        if (improvingMaxHPLevel >= 1) {
                            maxhp += Randomizer.rand(16, 20) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                        } else {
                            maxhp += Randomizer.rand(16, 20);
                        }
                        //maxhp += Randomizer.rand(18, 22);
                    } else if (job >= 2000 && job <= 2112) { // Aran
                        maxhp += Randomizer.rand(40, 50);
                    } else { // GameMaster
                        maxhp += Randomizer.rand(50, 100);
                    }
                    maxhp = Math.min(30000, Math.abs(maxhp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxHp(maxhp, chr);
                    statupdate.put(MapleStat.MAXHP, (int) maxhp);
                    break;
                case 0x8000: // MP
                    int maxmp = stat.getMaxMp();
                    if (chr.getHpApUsed() >= 10000 || stat.getMaxMp() >= 99999) {
                        return;
                    }
                    if (GameConstants.isBeginnerJob(job)) { // Beginner
                        maxmp += Randomizer.rand(6, 8);
                    } else if (job >= 100 && job <= 132) { // Warrior
                        maxmp += Randomizer.rand(2, 4);
                    } else if (job >= 200 && job <= 232) { // Magician
                        Skill improvingMaxMP = SkillFactory.getSkill(2000001);
                        int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                        if (improvingMaxMPLevel >= 1) {
                            maxmp += Randomizer.rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                        } else {
                            maxmp += Randomizer.rand(18, 20);
                        }
                        //maxmp += Randomizer.rand(38, 40);
                    } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 500 && job <= 532)) { // Bowman
                        maxmp += Randomizer.rand(10, 12);
                    } else if (job >= 100 && job <= 132) { // Soul Master
                        maxmp += Randomizer.rand(6, 9);
                    } else if (job >= 2000 && job <= 2112) { // Aran
                        maxmp += Randomizer.rand(6, 9);
                    } else { // GameMaster
                        maxmp += Randomizer.rand(50, 100);
                    }
                    maxmp = Math.min(30000, Math.abs(maxmp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxMp(maxmp, chr);
                    statupdate.put(MapleStat.MAXMP, (int) maxmp);
                    break;
                default:
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
                    return;
            }
            chr.setRemainingAp((short) (chr.getRemainingAp() - 1));
            statupdate.put(MapleStat.AVAILABLEAP, (int) chr.getRemainingAp());
            c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
        }
    }

    public static final void DistributeSP(final int skillid, final MapleClient c, final MapleCharacter chr) {
        boolean isBeginnerSkill = false;
        final int remainingSp;

        if (GameConstants.isBeginnerJob(skillid / 10000) && (skillid % 10000 == 1000 || skillid % 10000 == 1001 || skillid % 10000 == 1002 || skillid % 10000 == 2)) {
            final boolean resistance = skillid / 10000 == 3000 || skillid / 10000 == 3001;
            final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + 1000));
            final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + 1001));
            final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + (resistance ? 2 : 1002)));
            remainingSp = Math.min((chr.getLevel() - 1), resistance ? 9 : 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
            isBeginnerSkill = true;
        } else if (GameConstants.isBeginnerJob(skillid / 10000)) {
            return;
        } else {
            remainingSp = chr.getRemainingSp(GameConstants.getSkillBookForSkill(skillid));
        }
        final Skill skill = SkillFactory.getSkill(skillid);

        for (Pair<Integer, Byte> ski : skill.getRequiredSkills()) {
            if (chr.getSkillLevel(SkillFactory.getSkill(ski.left)) < ski.right) {
                //AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without the required skill (" + skillid + ")");
                return;
            }
        }
        final int maxlevel = skill.isFourthJob() ? chr.getMasterLevel(skill) : skill.getMaxLevel();
        final int curLevel = chr.getSkillLevel(skill);

        if (skill.isInvisible() && chr.getSkillLevel(skill) == 0) {
            if ((skill.isFourthJob() && chr.getMasterLevel(skill) == 0) || (!skill.isFourthJob() && maxlevel < 10 && !isBeginnerSkill)) {
                c.getSession().write(MaplePacketCreator.enableActions());
                //AutobanManager.getInstance().addPoints(c, 1000, 0, "Illegal distribution of SP to invisible skills (" + skillid + ")");
                return;
            }
        }

        if ((remainingSp > 0 && curLevel + 1 <= maxlevel) && skill.canBeLearnedBy(chr.getJob())) {
            if (!isBeginnerSkill) {
                final int skillbook = GameConstants.getSkillBookForSkill(skillid);
                chr.setRemainingSp(chr.getRemainingSp(skillbook) - 1, skillbook);
            }
            c.getSession().write(MaplePacketCreator.updateSp(chr, false));
            chr.changeSkillLevel(skill, (byte) (curLevel + 1), chr.getMasterLevel(skill));
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static final void AutoAssignAP(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        chr.updateTick(slea.readInt());
        slea.skip(4);
        if (slea.available() < 16) {
            return;
        }
        final int PrimaryStat = slea.readInt();
        final int amount = slea.readInt();
        final int SecondaryStat = slea.readInt();
        final int amount2 = slea.readInt();
        if (amount < 0 || amount2 < 0) {
            return;
        }

        final PlayerStats playerst = chr.getStat();

        Map<MapleStat, Integer> statupdate = new EnumMap<>(MapleStat.class);
        c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));

        if (chr.getRemainingAp() == amount + amount2) {
            switch (PrimaryStat) {
                case 0x100: // Str
                    if (playerst.getStr() + amount > 999) {
                        return;
                    }
                    playerst.setStr((short) (playerst.getStr() + amount), chr);
                    statupdate.put(MapleStat.STR, (int) playerst.getStr());
                    break;
                case 0x200: // Dex
                    if (playerst.getDex() + amount > 999) {
                        return;
                    }
                    playerst.setDex((short) (playerst.getDex() + amount), chr);
                    statupdate.put(MapleStat.DEX, (int) playerst.getDex());
                    break;
                case 0x400: // Int
                    if (playerst.getInt() + amount > 999) {
                        return;
                    }
                    playerst.setInt((short) (playerst.getInt() + amount), chr);
                    statupdate.put(MapleStat.INT, (int) playerst.getInt());
                    break;
                case 0x800: // Luk
                    if (playerst.getLuk() + amount > 999) {
                        return;
                    }
                    playerst.setLuk((short) (playerst.getLuk() + amount), chr);
                    statupdate.put(MapleStat.LUK, (int) playerst.getLuk());
                    break;
                default:
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
                    return;
            }
            switch (SecondaryStat) {
                case 0x100: // Str
                    if (playerst.getStr() + amount2 > 999) {
                        return;
                    }
                    playerst.setStr((short) (playerst.getStr() + amount2), chr);
                    statupdate.put(MapleStat.STR, (int) playerst.getStr());
                    break;
                case 0x200: // Dex
                    if (playerst.getDex() + amount2 > 999) {
                        return;
                    }
                    playerst.setDex((short) (playerst.getDex() + amount2), chr);
                    statupdate.put(MapleStat.DEX, (int) playerst.getDex());
                    break;
                case 0x400: // Int
                    if (playerst.getInt() + amount2 > 999) {
                        return;
                    }
                    playerst.setInt((short) (playerst.getInt() + amount2), chr);
                    statupdate.put(MapleStat.INT, (int) playerst.getInt());
                    break;
                case 0x800: // Luk
                    if (playerst.getLuk() + amount2 > 999) {
                        return;
                    }
                    playerst.setLuk((short) (playerst.getLuk() + amount2), chr);
                    statupdate.put(MapleStat.LUK, (int) playerst.getLuk());
                    break;
                default:
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
                    return;
            }
            chr.setRemainingAp((short) (chr.getRemainingAp() - (amount + amount2)));
            statupdate.put(MapleStat.AVAILABLEAP, (int) chr.getRemainingAp());
            c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
        }
    }
}
