package client;

import client.inventory.Equip;
import client.inventory.EquipAdditions;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildSkill;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.StructPotentialItem;
import server.StructSetItem;
import server.StructSetItem.SetItem;
import server.life.Element;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class PlayerStats implements Serializable {

    private static final long serialVersionUID = -679541993413738569L;
    private Map<Integer, Integer> setHandling = new HashMap<Integer, Integer>(), skillsIncrement = new HashMap<Integer, Integer>(), damageIncrease = new HashMap<Integer, Integer>();
    private EnumMap<Element, Integer> elemBoosts = new EnumMap<Element, Integer>(Element.class);
    private List<Equip> durabilityHandling = new ArrayList<Equip>(), equipLevelHandling = new ArrayList<Equip>();
    private transient float shouldHealHP, shouldHealMP;
    public short str, dex, luk, int_;
    public int hp, maxhp, mp, maxmp;
    private transient short passive_sharpeye_min_percent, passive_sharpeye_percent, passive_sharpeye_rate;
    private transient byte passive_mastery;
    private transient int localstr, localdex, localluk, localint_, localmaxhp, localmaxmp;
    private transient int magic, watk, hands, accuracy;
    public transient boolean equippedWelcomeBackRing, hasClone, hasPartyBonus, Berserk, canFish, canFishVIP, equippedFriendshipRing, equippedMarriageRing;
    public transient double expBuff, dropBuff, mesoBuff, cashBuff, mesoGuard, mesoGuardMeso, expMod, pickupRange;
    //same with incMesoProp/incRewardProp for now
    public transient double dam_r, bossdam_r;
    public transient int recoverHP, recoverMP, mpconReduce, mpconPercent, incMesoProp, incRewardProp, DAMreflect, DAMreflect_rate, mpRestore,
            hpRecover, hpRecoverProp, hpRecoverPercent, mpRecover, mpRecoverProp, RecoveryUP, BuffUP, RecoveryUP_Skill, BuffUP_Skill,
            incAllskill, combatOrders, ignoreTargetDEF, defRange, BuffUP_Summon, dodgeChance, speed, jump, harvestingTool,
            equipmentBonusExp, dropMod, cashMod, levelBonus, ASR, pickRate, decreaseDebuff, equippedFairy, equippedSummon,
            percent_hp, percent_mp, percent_str, percent_dex, percent_int, percent_luk, percent_acc, percent_atk, percent_matk, percent_wdef, percent_mdef,
            pvpDamage, hpRecoverTime = 0, mpRecoverTime = 0, dot, dotTime, questBonus, pvpRank, pvpExp, wdef, mdef, trueMastery, terR, asrR;
    private transient float localmaxbasedamage, localmaxbasepvpdamage, localmaxbasepvpdamageL;
    public transient int def, element_ice, element_fire, element_light, element_psn;  // Elemental properties

    public final void init(MapleCharacter chra) {
        recalcLocalStats(chra);
    }

    public final short getStr() {
        return str;
    }

    public final short getDex() {
        return dex;
    }

    public final short getLuk() {
        return luk;
    }

    public final short getInt() {
        return int_;
    }

    public final void setStr(final short str, MapleCharacter chra) {
        this.str = str;
        recalcLocalStats(chra);
    }

    public final void setDex(final short dex, MapleCharacter chra) {
        this.dex = dex;
        recalcLocalStats(chra);
    }

    public final void setLuk(final short luk, MapleCharacter chra) {
        this.luk = luk;
        recalcLocalStats(chra);
    }

    public final void setInt(final short int_, MapleCharacter chra) {
        this.int_ = int_;
        recalcLocalStats(chra);
    }

    public final boolean setHp(final int newhp, MapleCharacter chra) {
        return setHp(newhp, false, chra);
    }

    public final boolean setHp(int newhp, boolean silent, MapleCharacter chra) {
        final int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;

        if (chra != null) {
            if (!silent) {
                chra.checkBerserk();
                chra.updatePartyMemberHP();
            }
            if (oldHp > hp && !chra.isAlive()) {
                chra.playerDead();
            }
        }
        return hp != oldHp;
    }

    public final boolean setMp(final int newmp, final MapleCharacter chra) {
        final int oldMp = mp;
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
        return mp != oldMp;
    }

    public final void setInfo(final int maxhp, final int maxmp, final int hp, final int mp) {
        this.maxhp = maxhp;
        this.maxmp = maxmp;
        this.hp = hp;
        this.mp = mp;
    }

    public final void setMaxHp(final int hp, MapleCharacter chra) {
        this.maxhp = hp;
        recalcLocalStats(chra);
    }

    public final void setMaxMp(final int mp, MapleCharacter chra) {
        this.maxmp = mp;
        recalcLocalStats(chra);
    }

    public final int getHp() {
        return hp;
    }

    public final int getMaxHp() {
        return maxhp;
    }

    public final int getMp() {
        return mp;
    }

    public final int getMaxMp() {
        return maxmp;
    }

    public final int getTotalDex() {
        return localdex;
    }

    public final int getTotalInt() {
        return localint_;
    }

    public final int getTotalStr() {
        return localstr;
    }

    public final int getTotalLuk() {
        return localluk;
    }

    public final int getTotalMagic() {
        return magic;
    }

    public final int getSpeed() {
        return speed;
    }

    public final int getJump() {
        return jump;
    }

    public final int getTotalWatk() {
        return watk;
    }

    public final int getCurrentMaxHp() {
        return localmaxhp;
    }

    public final int getCurrentMaxMp() {
        return localmaxmp;
    }

    public final int getHands() {
        return hands;
    }

    public final float getCurrentMaxBaseDamage() {
        return localmaxbasedamage;
    }

    public final float getCurrentMaxBasePVPDamage() {
        return localmaxbasepvpdamage;
    }

    public final float getCurrentMaxBasePVPDamageL() {
        return localmaxbasepvpdamageL;
    }

    public void recalcLocalStats(MapleCharacter chra) {
        recalcLocalStats(false, chra);
    }

    public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        if (chra.isClone()) {
            return; //clones share PlayerStats objects and do not need to be recalculated
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int oldmaxhp = localmaxhp;
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        accuracy = 0;
        wdef = 0;
        mdef = 0;
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        speed = 100;
        jump = 100;
        pickupRange = 0.0;
        decreaseDebuff = 0;
        ASR = 0;
        dot = 0;
        questBonus = 1;
        dotTime = 0;
        trueMastery = 0;
        percent_wdef = 0;
        percent_mdef = 0;
        percent_hp = 0;
        percent_mp = 0;
        percent_str = 0;
        percent_dex = 0;
        percent_int = 0;
        percent_luk = 0;
        percent_acc = 0;
        percent_atk = 0;
        percent_matk = 0;
        passive_sharpeye_rate = 5;
        passive_sharpeye_min_percent = 20;
        passive_sharpeye_percent = 50;
        magic = 0;
        watk = 0;
        if (chra.getJob() == 500 || (chra.getJob() >= 520 && chra.getJob() <= 522)) {
            watk = 20; //bullet
        } else if (chra.getJob() == 400 || (chra.getJob() >= 410 && chra.getJob() <= 412) || (chra.getJob() >= 1400 && chra.getJob() <= 1412)) {
            watk = 30; //stars
        }
        StructPotentialItem pot;
        dodgeChance = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        dam_r = 100.0;
        bossdam_r = 100.0;
        expBuff = 100.0;
        cashBuff = 100.0;
        dropBuff = 100.0;
        mesoBuff = 100.0;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        mpconPercent = 100;
        incMesoProp = 0;
        incRewardProp = 0;
        DAMreflect = 0;
        DAMreflect_rate = 0;
        ignoreTargetDEF = 0;
        hpRecover = 0;
        hpRecoverProp = 0;
        hpRecoverPercent = 0;
        mpRecover = 0;
        mpRecoverProp = 0;
        mpRestore = 0;
        pickRate = 0;
        equippedWelcomeBackRing = false;
        equippedFriendshipRing = false;
        equippedMarriageRing = false;
        equippedFairy = 0;
        equippedSummon = 0;
        hasPartyBonus = false;
        hasClone = false;
        Berserk = false;
        canFish = true;
        canFishVIP = false;
        equipmentBonusExp = 0;
        RecoveryUP = 0;
        BuffUP = 0;
        RecoveryUP_Skill = 0;
        BuffUP_Skill = 0;
        BuffUP_Summon = 0;
        dropMod = 1;
        expMod = 1.0;
        cashMod = 1;
        levelBonus = 0;
        incAllskill = 0;
        combatOrders = 0;
        defRange = 0;
        durabilityHandling.clear();
        equipLevelHandling.clear();
        skillsIncrement.clear();
        damageIncrease.clear();
        setHandling.clear();
        harvestingTool = 0;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        def = 100;
        terR = 0;
        asrR = 0;
        final Iterator<Item> itera = chra.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();
        while (itera.hasNext()) {
            final Equip equip = (Equip) itera.next();

            if (equip.getPosition() == -11) {
                if (GameConstants.isMagicWeapon(equip.getItemId())) {
                    final Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());

                    if (eqstat != null) {
                        if (eqstat.containsKey("incRMAF")) {
                            element_fire = eqstat.get("incRMAF");
                        }
                        if (eqstat.containsKey("incRMAI")) {
                            element_ice = eqstat.get("incRMAI");
                        }
                        if (eqstat.containsKey("incRMAL")) {
                            element_light = eqstat.get("incRMAL");
                        }
                        if (eqstat.containsKey("incRMAS")) {
                            element_psn = eqstat.get("incRMAS");
                        }
                        if (eqstat.containsKey("elemDefault")) {
                            def = eqstat.get("elemDefault");
                        }
                    }
                }
            }
            if (equip.getItemId() / 10000 == 166 && equip.getAndroid() != null && chra.getAndroid() == null) {
                chra.setAndroid(equip.getAndroid());
            }
            accuracy += equip.getAcc();
            localmaxhp_ += equip.getHp();
            localmaxmp_ += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk();
            watk += equip.getWatk();
            wdef += equip.getWdef();
            mdef += equip.getMdef();
            speed += equip.getSpeed();
            jump += equip.getJump();
            pvpDamage += equip.getPVPDamage();
            switch (equip.getItemId()) {
                case 1112127:
                    equippedWelcomeBackRing = true;
                    break;
                case 1112800:
                case 1112801:
                case 1112802:
                case 1112810:
                case 1112811:
                case 1112812:
                case 1112816:
                case 1112817:
                case 1049000:
                    equippedFriendshipRing = true;
                    break;
                case 1112001:
                case 1112002:
                case 1112003:
                case 1112005: //new
                case 1112006: //new
                case 1112007:
                case 1112012:
                case 1112015: //new
                case 1048000:
                case 1048001:
                case 1048002:
                case 1112013:
                case 1112014:
                case 1112820:
                    equippedMarriageRing = true;
                    break;
                case 1112728: // mapler ring
                case 1122017: // fairy's pendant
                    equippedFairy = 10;
                    break;
                case 1122158:
                    equippedFairy = 5;
                    break;
                case 1112585:
                    equippedSummon = 1085;
                    break;
                case 1112586:
                    equippedSummon = 1087;
                    break;
                case 1112663:
                    equippedSummon = 1179;
                    break;
                default:
                    for (int eb_bonus : GameConstants.Equipments_Bonus) {
                        if (equip.getItemId() == eb_bonus) {
                            equipmentBonusExp += GameConstants.Equipment_Bonus_EXP(eb_bonus);
                            break;
                        }
                    }
                    break;
            } //slow, poison, darkness, seal, freeze
            percent_hp += equip.getHpR();
            percent_mp += equip.getMpR();
            Integer set = ii.getSetItemID(equip.getItemId());
            if (set != null && set > 0) {
                int value = 1;
                if (setHandling.containsKey(set)) {
                    value += setHandling.get(set).intValue();
                }
                setHandling.put(set, value); //id of Set, number of items to go with the set
            }
            if (equip.getIncSkill() > 0 && ii.getEquipSkills(equip.getItemId()) != null) {
                for (int zzz : ii.getEquipSkills(equip.getItemId())) {
                    final Skill skil = SkillFactory.getSkill(zzz);
                    if (skil != null && skil.canBeLearnedBy(chra.getJob())) { //dont go over masterlevel :D
                        int value = 1;
                        if (skillsIncrement.get(skil.getId()) != null) {
                            value += skillsIncrement.get(skil.getId());
                        }
                        skillsIncrement.put(skil.getId(), value);
                    }
                }

            }
            EnumMap<EquipAdditions, Pair<Integer, Integer>> additions = ii.getEquipAdditions(equip.getItemId());
            if (additions != null) {
                for (Entry<EquipAdditions, Pair<Integer, Integer>> add : additions.entrySet()) {
                    switch (add.getKey()) {
                        case elemboost:
                            int value = add.getValue().right;
                            Element key = Element.getFromId(add.getValue().left);
                            if (elemBoosts.get(key) != null) {
                                value += elemBoosts.get(key);
                            }
                            elemBoosts.put(key, value);
                            break;
                        case mobcategory: //skip the category, thinkings too expensive to have yet another Map<Integer, Integer> for damage calculations
                            dam_r *= (add.getValue().right + 100.0) / 100.0;
                            bossdam_r += (add.getValue().right + 100.0) / 100.0;
                            break;
                        case critical:
                            passive_sharpeye_rate += add.getValue().left;
                            passive_sharpeye_min_percent += add.getValue().right;
                            passive_sharpeye_percent += add.getValue().right; //???CONFIRM - not sure if this is max or minCritDmg
                            break;
                        case boss:
                            bossdam_r *= (add.getValue().right + 100.0) / 100.0;
                            break;
                        case mobdie:
                            if (add.getValue().left > 0) {
                                hpRecover += add.getValue().left; //no indication of prop, so i made myself
                                hpRecoverProp += 5;
                            }
                            if (add.getValue().right > 0) {
                                mpRecover += add.getValue().right; //no indication of prop, so i made myself
                                mpRecoverProp += 5;
                            }
                            break;
                        case skill: //now, i'm a bit iffy on this one
                            if (first_login) {
                                chra.changeSkillLevel_Skip(SkillFactory.getSkill(add.getValue().left), (byte) (int) add.getValue().right, (byte) 0);
                            }
                            break;
                        case hpmpchange:
                            recoverHP += add.getValue().left;
                            recoverMP += add.getValue().right;
                            break;
                    }
                }
            }
            if (equip.getState() > 1) {
                int[] potentials = {equip.getPotential1(), equip.getPotential2(), equip.getPotential3(), equip.getPotential4(), equip.getPotential5()};
                for (int i : potentials) {
                    if (i > 0) {
                        pot = ii.getPotentialInfo(i).get(ii.getReqLevel(equip.getItemId()) / 10);
                        if (pot != null) {
                            localmaxhp_ += pot.incMHP;
                            localmaxmp_ += pot.incMMP;
                            handlePotential(pot, chra, first_login);
                        }
                    }
                }
            }
            if (equip.getDurability() > 0) {
                durabilityHandling.add((Equip) equip);
            }
            if (GameConstants.getMaxLevel(equip.getItemId()) > 0 && (GameConstants.getStatFromWeapon(equip.getItemId()) == null ? (equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId())) : (equip.getEquipLevel() < GameConstants.getMaxLevel(equip.getItemId())))) {
                equipLevelHandling.add((Equip) equip);
            }
        }
        final Iterator<Entry<Integer, Integer>> iter = setHandling.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<Integer, Integer> entry = iter.next();
            final StructSetItem set = ii.getSetItem(entry.getKey());
            if (set != null) {
                final Map<Integer, SetItem> itemz = set.getItems();
                for (Entry<Integer, SetItem> ent : itemz.entrySet()) {
                    if (ent.getKey() <= entry.getValue()) {
                        SetItem se = ent.getValue();
                        localstr += se.incSTR + se.incAllStat;
                        localdex += se.incDEX + se.incAllStat;
                        localint_ += se.incINT + se.incAllStat;
                        localluk += se.incLUK + se.incAllStat;
                        watk += se.incPAD;
                        magic += se.incMAD;
                        speed += se.incSpeed;
                        accuracy += se.incACC;
                        localmaxhp_ += se.incMHP;
                        localmaxmp_ += se.incMMP;
                        percent_hp += se.incMHPr;
                        percent_mp += se.incMMPr;
                        wdef += se.incPDD;
                        mdef += se.incMDD;
                        if (se.option1 > 0 && se.option1Level > 0) {
                            pot = ii.getPotentialInfo(se.option1).get(se.option1Level);
                            if (pot != null) {
                                localmaxhp_ += pot.incMHP;
                                localmaxmp_ += pot.incMMP;
                                handlePotential(pot, chra, first_login);
                            }
                        }
                        if (se.option2 > 0 && se.option2Level > 0) {
                            pot = ii.getPotentialInfo(se.option2).get(se.option2Level);
                            if (pot != null) {
                                localmaxhp_ += pot.incMHP;
                                localmaxmp_ += pot.incMMP;
                                handlePotential(pot, chra, first_login);
                            }
                        }
                    }
                }
            }
        }
        handleProfessionTool(chra);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int weekDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
        for (Item item : chra.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 100000 == 52) {
                if (expMod < 3 && (item.getItemId() == 5211060 || item.getItemId() == 5211050 || item.getItemId() == 5211051 || item.getItemId() == 5211052 || item.getItemId() == 5211053 || item.getItemId() == 5211054)) {
                    expMod = 3.0;//overwrite
                } else if (expMod < 2 && (item.getItemId() == 5211061 || item.getItemId() == 5211000 || item.getItemId() == 5211001 || item.getItemId() == 5211002 || item.getItemId() == 5211003 || item.getItemId() == 5211046 || item.getItemId() == 5211047 || item.getItemId() == 5211048 || item.getItemId() == 5211049)) {
                    expMod = 2.0;
                } else if (expMod < 2 && (item.getItemId() == 5210002 || item.getItemId() == 5210003) && (((hour >= 6 && hour <= 18) && (weekDay >= 2 && weekDay <= 6)) || (weekDay == 1 || weekDay == 7))) {
                    expMod = 2.0;
                } else if (expMod < 2 && (item.getItemId() == 5210004 || item.getItemId() == 5210005 || item.getItemId() == 521000) && (((hour >= 18 || hour <= 6) && (weekDay >= 2 && weekDay <= 6)) || (weekDay == 1 || weekDay == 7))) {
                    expMod = 2.0;
                } else if (expMod < 2 && (item.getItemId() == 5210000 || item.getItemId() == 5210001) && (((hour >= 10 && hour <= 22) && (weekDay >= 2 && weekDay <= 6)) || (weekDay == 1 || weekDay == 7))) {
                    expMod = 2.0;
                } else if (expMod < 1.5 && (item.getItemId() == 5211077 || item.getItemId() == 5211078 || item.getItemId() == 5211079 || item.getItemId() == 5211068)) {
                    expMod = 1.5;
                } else if (expMod < 1.2 && (item.getItemId() == 5211071 || item.getItemId() == 5211072 || item.getItemId() == 5211073 || item.getItemId() == 5211074 || item.getItemId() == 5211075 || item.getItemId() == 5211076 || item.getItemId() == 5211067)) {
                    expMod = 1.2;
                }
            } else if (dropMod == 1 && item.getItemId() / 10000 == 536) {
                if (item.getItemId() == 5360000 || item.getItemId() == 5360009 || item.getItemId() == 5360010 || item.getItemId() == 5360011 || item.getItemId() == 5360012 || item.getItemId() == 5360013 || item.getItemId() == 5360014 || item.getItemId() == 5360017 || item.getItemId() == 5360050 || item.getItemId() == 5360053 || item.getItemId() == 5360042 || item.getItemId() == 5360052 || item.getItemId() == 5360015 || item.getItemId() == 5360016) {
                    dropMod = 2;
                }
            } else if (item.getItemId() == 5650000) {
                hasPartyBonus = true;
            } else if (item.getItemId() == 5590001) {
                levelBonus = 10;
            } else if (levelBonus == 0 && item.getItemId() == 5590000) {
                levelBonus = 5;
            } else if (item.getItemId() == 5710000) {
                questBonus = 2;
            } else if (item.getItemId() == 5340000) {
                canFish = true;
            } else if (item.getItemId() == 5340001) {
                canFish = true;
                canFishVIP = true;
            }
        }
        for (Item item : chra.getInventory(MapleInventoryType.ETC).list()) { //omfg;
            switch (item.getItemId()) {
                case 4030003:
                    pickupRange = Double.POSITIVE_INFINITY;
                    break;
                case 4030004:
                    hasClone = true;
                    break;
                case 4030005:
                    cashMod = 2;
                    break;
            }
        }
        if (first_login && chra.getLevel() >= 30) { //yeah
            if (chra.isGM()) { //!job lol
                for (int i = 0; i < allJobs.length; i++) {
                    chra.changeSkillLevel_Skip(SkillFactory.getSkill(1085 + allJobs[i]), (byte) 1, (byte) 0);
                    chra.changeSkillLevel_Skip(SkillFactory.getSkill(1087 + allJobs[i]), (byte) 1, (byte) 0);
                    chra.changeSkillLevel_Skip(SkillFactory.getSkill(1179 + allJobs[i]), (byte) 1, (byte) 0);
                }
            } else {
                chra.changeSkillLevel_Skip(SkillFactory.getSkill(getSkillByJob(1085, chra.getJob())), (byte) 1, (byte) 0);
                chra.changeSkillLevel_Skip(SkillFactory.getSkill(getSkillByJob(1087, chra.getJob())), (byte) 1, (byte) 0);
                chra.changeSkillLevel_Skip(SkillFactory.getSkill(getSkillByJob(1179, chra.getJob())), (byte) 1, (byte) 0);
            }

        }
        if (equippedSummon > 0) {
            equippedSummon = getSkillByJob(equippedSummon, chra.getJob());
        }
        //dam_r += (chra.getJob() >= 430 && chra.getJob() <= 434 ? 70 : 0); //leniency on upper stab
        this.localstr += Math.floor((localstr * percent_str) / 100.0f);
        this.localdex += Math.floor((localdex * percent_dex) / 100.0f);
        this.localint_ += Math.floor((localint_ * percent_int) / 100.0f);
        this.localluk += Math.floor((localluk * percent_luk) / 100.0f);

        if (localint_ > localdex) {
            accuracy += localint_ + Math.floor(localluk * 1.2);
        } else {
            accuracy += localluk + Math.floor(localdex * 1.2);
        }
        this.wdef += Math.floor((localstr * 1.2) + ((localdex + localluk) * 0.5) + (localint_ * 0.4));
        this.mdef += Math.floor((localstr * 0.4) + ((localdex + localluk) * 0.5) + (localint_ * 1.2));
        this.accuracy += Math.floor((accuracy * percent_acc) / 100.0f);
        Skill bx;
        int bof;
        MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.MONSTER_RIDING);
        if (eff != null && eff.getSourceId() == 33001001) { //jaguar
            passive_sharpeye_rate += eff.getW();
            percent_hp += eff.getZ();
        }

        Integer buff = chra.getBuffedValue(MapleBuffStat.DICE_ROLL);
        if (buff != null) {
            percent_wdef += GameConstants.getDiceStat(buff.intValue(), 2);
            percent_mdef += GameConstants.getDiceStat(buff.intValue(), 2);
            percent_hp += GameConstants.getDiceStat(buff.intValue(), 3);
            percent_mp += GameConstants.getDiceStat(buff.intValue(), 3);
            passive_sharpeye_rate += GameConstants.getDiceStat(buff.intValue(), 4);
            dam_r *= (GameConstants.getDiceStat(buff.intValue(), 5) + 100.0) / 100.0;
            bossdam_r *= (GameConstants.getDiceStat(buff.intValue(), 5) + 100.0) / 100.0;
            expBuff *= (GameConstants.getDiceStat(buff.intValue(), 6) + 100.0) / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.INFINITY);
        if (buff != null) {
            percent_matk += buff.intValue() - 1;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ONYX_SHROUD);
        if (buff != null) {
            dodgeChance += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_DAMAGE);
        if (buff != null) {
            pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_ATTACK);
        if (buff != null) {
            pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.FELINE_BERSERK);
        if (buff != null) {
            percent_hp += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLUE_AURA);
        if (eff != null) {
            percent_wdef += eff.getZ() + eff.getY();
            percent_mdef += eff.getZ() + eff.getY();

        }
        buff = chra.getBuffedValue(MapleBuffStat.CONVERSION);
        if (buff != null) {
            percent_hp += buff.intValue();
        } else {
            buff = chra.getBuffedValue(MapleBuffStat.MAXHP);
            if (buff != null) {
                percent_hp += buff.intValue();
            }
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
        if (buff != null) {
            percent_mp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BUFF);
        if (buff != null) {
            percent_mp += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BUFF_MASTERY);
        if (buff != null) {
            BuffUP_Skill += buff.intValue();
        }
        if (GameConstants.isKOC(chra.getJob())) {
            bx = SkillFactory.getSkill(2000006);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                percent_hp += eff.getX();
                percent_mp += eff.getX();
            }
        }
        switch (chra.getJob()) {
            case 1200:
            case 1210:
            case 1211:
            case 1212: {
                bx = SkillFactory.getSkill(12000005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_mp += bx.getEffect(bof).getPercentMP();
                }
                break;
            }
            case 1100:
            case 1110:
            case 1111:
            case 1112: {
                bx = SkillFactory.getSkill(11000005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 501:
            case 530:
            case 531:
            case 532:
                defRange = 200;
                bx = SkillFactory.getSkill(5010003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(5300008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(5311001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(5301001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5310007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getHpR();
                    ASR += eff.getASRRate();
                    percent_wdef += eff.getWDEFRate();
                }
                bx = SkillFactory.getSkill(5310006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(5321009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            case 3001:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                mpRecoverProp = 100;
                bx = SkillFactory.getSkill(31000003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getHpR();
                }
                bx = SkillFactory.getSkill(31100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getDAMRate());
                    damageIncrease.put(31001006, (int) eff.getDAMRate());
                    damageIncrease.put(31001007, (int) eff.getDAMRate());
                    damageIncrease.put(31001008, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(31100005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(31100010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getX());
                    damageIncrease.put(31001006, (int) eff.getX());
                    damageIncrease.put(31001007, (int) eff.getX());
                    damageIncrease.put(31001008, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(31111007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(31110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dodgeChance += eff.getX();
                    hpRecoverPercent += eff.getY();
                    hpRecoverProp += eff.getX();
                    mpRecover += eff.getY();
                    mpRecoverProp += eff.getX();
                }
                bx = SkillFactory.getSkill(31110009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpRecover += 1;
                    mpRecoverProp += eff.getProb();
                }
                bx = SkillFactory.getSkill(31111006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() + 100.0) / 100.0;
                    passive_sharpeye_rate += eff.getY();
                }
                bx = SkillFactory.getSkill(31121006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(31120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getX());
                    damageIncrease.put(31001006, (int) eff.getX());
                    damageIncrease.put(31001007, (int) eff.getX());
                    damageIncrease.put(31001008, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(31120008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getAttackX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(31120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_wdef += bx.getEffect(bof).getT();
                }
                bx = SkillFactory.getSkill(30010112);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    bossdam_r += eff.getBossDamage();
                    mpRecover += eff.getX();
                    mpRecoverProp += eff.getBossDamage(); //yes
                }
                bx = SkillFactory.getSkill(30010111);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverPercent += eff.getX();
                    hpRecoverProp += eff.getProb(); //yes
                }
                //TODO LEGEND: 31121007 (consumes fury,free of fury), 31111004 (increase wdef ASR ER), 
                break;
            case 510:
            case 511:
            case 512: {
                bx = SkillFactory.getSkill(5100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 1510:
            case 1511:
            case 1512: {
                bx = SkillFactory.getSkill(15100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 400:
            case 410:
            case 411:
            case 412: {
                bx = SkillFactory.getSkill(4100006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ASR += eff.getASRRate();
                }
                break;
            }
            case 420:
            case 421:
            case 422: {
                bx = SkillFactory.getSkill(4200006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ASR += eff.getASRRate();
                }

                bx = SkillFactory.getSkill(4210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
                break;
            }
            case 431:
            case 432:
            case 433:
            case 434: {
                bx = SkillFactory.getSkill(4310004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ASR += eff.getASRRate();
                }
                bx = SkillFactory.getSkill(4341006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getWDEFRate();
                    percent_mdef += eff.getMDEFRate();
                }
                break;
            }
            case 100:
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132: {
                bx = SkillFactory.getSkill(1000006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }

                bx = SkillFactory.getSkill(1210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }

                bx = SkillFactory.getSkill(1220005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_wdef += bx.getEffect(bof).getT();
                }
                bx = SkillFactory.getSkill(1220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    trueMastery += bx.getEffect(bof).getMastery();
                }
                break;
            }
            case 322: { // Crossbowman
                bx = SkillFactory.getSkill(3220004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(3220009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                bx = SkillFactory.getSkill(3220005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && chra.getBuffedValue(MapleBuffStat.SPIRIT_LINK) != null) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getX();
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            }
            case 312: { // Bowmaster
                bx = SkillFactory.getSkill(3120005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(3120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                bx = SkillFactory.getSkill(3120006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && chra.getBuffedValue(MapleBuffStat.SPIRIT_LINK) != null) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getX();
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            }
            case 3510:
            case 3511:
            case 3512:
                defRange = 200;
                bx = SkillFactory.getSkill(35100000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(35120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    trueMastery += bx.getEffect(bof).getMastery();
                }
                break;
            case 3211:
            case 3212:
                bx = SkillFactory.getSkill(32110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(32110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(32120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(32120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(32120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            case 3300:
            case 3310:
            case 3311:
            case 3312:
                defRange = 200;
                bx = SkillFactory.getSkill(33120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(33110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(33120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    ignoreTargetDEF += eff.getIgnoreMob();
                    dodgeChance += eff.getER();
                }
                bx = SkillFactory.getSkill(32110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                break;
            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218: {
                magic += chra.getTotalSkillLevel(SkillFactory.getSkill(22000000));
                bx = SkillFactory.getSkill(22150000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(22160000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(22170001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
            case 2112: {
                bx = SkillFactory.getSkill(21120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
        }
        this.watk += Math.floor((watk * percent_atk) / 100.0f);
        this.magic += Math.floor((magic * percent_matk) / 100.0f); //or should this go before
        this.localint_ += Math.floor((localint_ * percent_matk) / 100.0f); //overpowered..
        bx = SkillFactory.getSkill(80000000);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            localstr += eff.getStrX();
            localdex += eff.getDexX();
            localint_ += eff.getIntX();
            localluk += eff.getLukX();
            percent_hp += eff.getHpR();
            percent_mp += eff.getMpR();
        }
        bx = SkillFactory.getSkill(80000001);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            bossdam_r += eff.getBossDamage();
        }
        bx = SkillFactory.getSkill(80001040);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            expBuff *= (bx.getEffect(bof).getEXPRate() + 100.0) / 100.0;
        }
        if (GameConstants.isAdventurer(chra.getJob())) {
            bx = SkillFactory.getSkill(74);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(80);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10074);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10080);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                localstr += eff.getStrX();
                localdex += eff.getDexX();
                localint_ += eff.getIntX();
                localluk += eff.getLukX();
                percent_hp += eff.getHpR();
                percent_mp += eff.getMpR();
            }

            bx = SkillFactory.getSkill(10110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                localstr += eff.getStrX();
                localdex += eff.getDexX();
                localint_ += eff.getIntX();
                localluk += eff.getLukX();
                percent_hp += eff.getHpR();
                percent_mp += eff.getMpR();
            }
        }

        if (chra.getGuildId() > 0) {
            final MapleGuild g = World.Guild.getGuild(chra.getGuildId());
            if (g != null && g.getSkills().size() > 0) {
                final long now = System.currentTimeMillis();
                for (MapleGuildSkill gs : g.getSkills()) {
                    if (gs.timestamp > now && gs.activator.length() > 0) {
                        final MapleStatEffect e = SkillFactory.getSkill(gs.skillID).getEffect(gs.level);
                        passive_sharpeye_rate += e.getCr();
                        watk += e.getAttackX();
                        magic += e.getMagicX();
                        expBuff *= (e.getEXPRate() + 100.0) / 100.0;
                        dodgeChance += e.getER();
                        percent_wdef += e.getWDEFRate();
                        percent_mdef += e.getMDEFRate();
                    }
                }
            }
        }

        localmaxhp_ += Math.floor((percent_hp * localmaxhp_) / 100.0f);
        localmaxmp_ += Math.floor((percent_mp * localmaxmp_) / 100.0f);
        wdef += Math.min(30000, Math.floor((wdef * percent_wdef) / 100.0f));
        mdef += Math.min(30000, Math.floor((wdef * percent_mdef) / 100.0f));
        //magic = Math.min(magic, 1999); //buffs can make it higher
        buff = chra.getBuffedValue(MapleBuffStat.STR);
        if (buff != null) {
            localstr += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DEX);
        if (buff != null) {
            localdex += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.INT);
        if (buff != null) {
            localint_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.LUK);
        if (buff != null) {
            localluk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_STAT);
        if (buff != null) {
            localstr += buff.intValue();
            localdex += buff.intValue();
            localint_ += buff.intValue();
            localluk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXHP);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXMP);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WDEF);
        if (buff != null) {
            wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MDEF);
        if (buff != null) {
            mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            mdef += buff.intValue();
        }

        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }

        buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (buff != null) {
            final double d = buff.doubleValue() / 100.0;
            localstr += d * str; //base only
            localdex += d * dex;
            localluk += d * luk;
            localint_ += d * int_;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
        if (buff != null) {
            final double d = buff.doubleValue() / 100.0;
            watk += (int) (watk * d);
            magic += (int) (magic * d);
        }
        buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
        if (buff != null) {
            watk += buff.intValue() / 10;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOGUARD);
        if (buff != null) {
            mesoGuardMeso += buff.doubleValue();
        }
        bx = SkillFactory.getSkill(GameConstants.getBOF_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getX();
        }

        bx = SkillFactory.getSkill(GameConstants.getEmpress_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getZ();
        }

        buff = chra.getBuffedValue(MapleBuffStat.EXPRATE);
        if (buff != null) {
            expBuff *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.DROP_RATE);
        if (buff != null) {
            dropBuff *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACASH_RATE);
        if (buff != null) {
            cashBuff *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESO_RATE);
        if (buff != null) {
            mesoBuff *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOUP);
        if (buff != null) {
            mesoBuff *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACC);
        if (buff != null) {
            accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ACC);
        if (buff != null) {
            accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_MATK);
        if (buff != null) {
            magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPIRIT_SURGE);
        if (buff != null) {
            passive_sharpeye_rate += buff.intValue();
            dam_r *= (buff.intValue() + 100.0) / 100.0;
            bossdam_r *= (buff.intValue() + 100.0) / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.ENERGY_CHARGE);
        if (eff != null) {
            watk += eff.getWatk();
            accuracy += eff.getAcc();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MATK);
        if (buff != null) {
            magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPEED);
        if (buff != null) {
            speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.JUMP);
        if (buff != null) {
            jump += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
        if (buff != null) {
            speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
        if (buff != null) {
            jump += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HIDDEN_POTENTIAL);
        if (eff != null) {
            passive_sharpeye_rate = 100; //INTENSE
            ASR = 100; //INTENSE

            wdef += eff.getX();
            mdef += eff.getX();
            watk += eff.getX();
            magic += eff.getX();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DAMAGE_BUFF);
        if (buff != null) {
            dam_r *= (buff.doubleValue() + 100.0) / 100.0;
            bossdam_r *= (buff.doubleValue() + 100.0) / 100.0;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.FINAL_CUT);
        if (buff != null) {
            dam_r *= buff.doubleValue() / 100.0;
            bossdam_r *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.OWL_SPIRIT);
        if (buff != null) {
            dam_r *= buff.doubleValue() / 100.0;
            bossdam_r *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BERSERK_FURY);
        if (buff != null) {
            dam_r *= buff.doubleValue() / 100.0;
            bossdam_r *= buff.doubleValue() / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLESS);
        if (eff != null) {
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getV();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.CONCENTRATE);
        if (buff != null) {
            mpconReduce += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HOLY_SHIELD);
        if (eff != null) {
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getV();
            mpconReduce += eff.getMPConReduce();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MAGIC_RESISTANCE);
        if (eff != null) {
            ASR += eff.getX();
        }

        eff = chra.getStatForBuff(MapleBuffStat.COMBO);
        buff = chra.getBuffedValue(MapleBuffStat.COMBO);
        if (eff != null && buff != null) {
            dam_r *= ((100.0 + ((eff.getV() + eff.getDAMRate()) * (buff.intValue() - 1))) / 100.0);
            bossdam_r *= ((100.0 + ((eff.getV() + eff.getDAMRate()) * (buff.intValue() - 1))) / 100.0);
        }
        eff = chra.getStatForBuff(MapleBuffStat.SUMMON);
        if (eff != null) {
            if (eff.getSourceId() == 35121010) { //amp
                dam_r *= (eff.getX() + 100.0) / 100.0;
                bossdam_r *= (eff.getX() + 100.0) / 100.0;
            }
        }
        eff = chra.getStatForBuff(MapleBuffStat.DARK_AURA);
        if (eff != null) {
            dam_r *= (eff.getX() + 100.0) / 100.0;
            bossdam_r *= (eff.getX() + 100.0) / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BODY_BOOST);
        if (eff != null) {
            dam_r *= (eff.getV() + 100.0) / 100.0;
            bossdam_r *= (eff.getV() + 100.0) / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BEHOLDER);
        if (eff != null) {
            trueMastery += eff.getMastery();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MECH_CHANGE);
        if (eff != null) {
            passive_sharpeye_rate += eff.getCr();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PYRAMID_PQ);
        if (eff != null && eff.getBerserk() > 0) {
            dam_r *= eff.getBerserk() / 100.0;
            bossdam_r *= eff.getBerserk() / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.WK_CHARGE);
        if (eff != null) {
            dam_r *= eff.getDamage() / 100.0;
            bossdam_r *= eff.getDamage() / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.PICKPOCKET);
        if (eff != null) {
            pickRate = eff.getProb();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PIRATES_REVENGE);
        if (eff != null) {
            dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
            bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.LIGHTNING_CHARGE);
        if (eff != null) {
            dam_r *= eff.getDamage() / 100.0;
            bossdam_r *= eff.getDamage() / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.WIND_WALK);
        if (eff != null) {
            dam_r *= eff.getDamage() / 100.0;
            bossdam_r *= eff.getDamage() / 100.0;
        }
        eff = chra.getStatForBuff(MapleBuffStat.DIVINE_SHIELD);
        if (eff != null) {
            watk += eff.getEnhancedWatk();
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.DARKSIGHT);
        if (buff != null) {
            dam_r *= (buff.intValue() + 100.0) / 100.0;
            bossdam_r *= (buff.intValue() + 100.0) / 100.0;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.ENRAGE);
        if (buff != null) {
            dam_r *= (buff.intValue() + 100.0) / 100.0;
            bossdam_r *= (buff.intValue() + 100.0) / 100.0;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.COMBAT_ORDERS);
        if (buff != null) {
            combatOrders += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.SHARP_EYES);
        if (eff != null) {
            passive_sharpeye_rate += eff.getX();
            passive_sharpeye_percent += eff.getCriticalMax();
        }
        buff = chra.getBuffedValue(MapleBuffStat.CRITICAL_RATE_BUFF);
        if (buff != null) {
            passive_sharpeye_rate += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST_PERCENT);
        if (buff != null) {
            localmaxhp_ *= buff.doubleValue() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST_PERCENT);
        if (buff != null) {
            localmaxmp_ *= buff.doubleValue() / 100.0;
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (buff != null) {
            jump = 120;
            switch (buff.intValue()) {
                case 1:
                    speed = 150;
                    break;
                case 2:
                    speed = 170;
                    break;
                case 3:
                    speed = 180;
                    break;
                default:
                    speed = 200; //lol
                    break;
            }

        }
        hands = this.localdex + this.localint_ + this.localluk;
        localmaxhp = Math.min(30000, Math.abs(Math.max(-30000, localmaxhp_)));
        localmaxmp = Math.min(30000, Math.abs(Math.max(-30000, localmaxmp_)));

        if (chra.getEventInstance() != null && chra.getEventInstance().getName().startsWith("PVP")) { //hack
            localmaxhp = Math.min(30000, localmaxhp * 3); //approximate.
            localmaxmp = Math.min(30000, localmaxmp * 2);
            //not sure on 20000 cap
            for (int i : pvpSkills) {
                Skill skil = SkillFactory.getSkill(i);
                if (skil != null && skil.canBeLearnedBy(chra.getJob())) {
                    chra.changeSkillLevel_Skip(skil, (byte) 1, (byte) 0);
                    eff = skil.getEffect(1);
                    switch ((i / 1000000) % 10) {
                        case 1:
                            if (eff.getX() > 0) {
                                pvpDamage += (wdef / eff.getX());
                            }
                            break;
                        case 3:
                            hpRecoverProp += eff.getProb();
                            hpRecover += eff.getX();
                            mpRecoverProp += eff.getProb();
                            mpRecover += eff.getX();
                            break;
                        case 5:
                            passive_sharpeye_rate += eff.getProb();
                            passive_sharpeye_percent = 100;
                            break;
                    }
                    break;
                }
            }
            eff = chra.getStatForBuff(MapleBuffStat.MORPH);
            if (eff != null && eff.getSourceId() % 10000 == 1105) { //ice knight
                localmaxhp = 30000;
                localmaxmp = 30000;
            }
        }
        if (GameConstants.isDemon(chra.getJob())) {
            localmaxmp = 120; //for now; its decided by job
        }

        //damage increase
        switch (chra.getJob()) {
            case 210:
            case 211:
            case 212: { // IL
                bx = SkillFactory.getSkill(2100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dotTime += eff.getX();
                    dot += eff.getZ();
                }
                bx = SkillFactory.getSkill(2110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(2121003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(2111003, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(2120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2121009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 220:
            case 221:
            case 222: { // IL
                bx = SkillFactory.getSkill(2200007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dot += bx.getEffect(bof).getZ();
                }
                bx = SkillFactory.getSkill(2210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(2220009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2221009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 1211:
            case 1212: { // flame
                bx = SkillFactory.getSkill(12110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                break;
            }
            case 230:
            case 231:
            case 232: { // Bishop
                bx = SkillFactory.getSkill(2300007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2310008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    passive_sharpeye_rate += bx.getEffect(bof).getCr();
                }
                bx = SkillFactory.getSkill(2320010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2321010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2320005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(2320011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 2001:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
                bx = SkillFactory.getSkill(23111005);
                bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    terR += bx.getEffect(bof).getTeRRate();
                    asrR += bx.getEffect(bof).getASRRate();
                }
                defRange = 200;
                bx = SkillFactory.getSkill(20021110);
                bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    expBuff *= (bx.getEffect(bof).getEXPRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(23000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(23100008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(23110004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getProb();
                }
                bx = SkillFactory.getSkill(23110004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(23101001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(23121004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getProb();
                }
                bx = SkillFactory.getSkill(23120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(23120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getX(); //or should we do 100?
                }
                bx = SkillFactory.getSkill(23120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(23101001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(23120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                break;
            case 1300:
            case 1310:
            case 1311:
            case 1312:
                defRange = 200;
                bx = SkillFactory.getSkill(13000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(13110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(13110003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            case 300:
            case 310:
            case 311:
            case 312:
                defRange = 200;
                bx = SkillFactory.getSkill(3000002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(3110007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(3120005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            case 320:
            case 321:
            case 322:
                defRange = 200;
                bx = SkillFactory.getSkill(3000002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(3210007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                break;

            case 422:
                bx = SkillFactory.getSkill(4221007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Savage Blow, Steal, and Assaulter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(4201005, (int) eff.getDAMRate());
                    damageIncrease.put(4201004, (int) eff.getDAMRate());
                    damageIncrease.put(4211002, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(4220009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mesoBuff *= (eff.getMesoRate() + 100.0) / 100.0;
                    pickRate += eff.getU();
                    mesoGuard -= eff.getV();
                    mesoGuardMeso -= eff.getW();
                    damageIncrease.put(4211006, eff.getX());
                }
                break;
            case 433:
            case 434:
                bx = SkillFactory.getSkill(4330007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverProp += eff.getProb();
                    hpRecoverPercent += eff.getX();
                }
                bx = SkillFactory.getSkill(4341002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Fatal Blow, Slash Storm, Tornado Spin, Bloody Storm, Upper Stab, and Flying Assaulter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(4311002, (int) eff.getDAMRate());
                    damageIncrease.put(4311003, (int) eff.getDAMRate());
                    damageIncrease.put(4321000, (int) eff.getDAMRate());
                    damageIncrease.put(4321001, (int) eff.getDAMRate());
                    damageIncrease.put(4331000, (int) eff.getDAMRate());
                    damageIncrease.put(4331004, (int) eff.getDAMRate());
                    damageIncrease.put(4331005, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(4341006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                break;
            case 2110:
            case 2111:
            case 2112: { // Aran
                bx = SkillFactory.getSkill(21101006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(21110002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(21000004, bx.getEffect(bof).getW());
                }
                bx = SkillFactory.getSkill(21111010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(21120002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(21100007, bx.getEffect(bof).getZ());
                }
                bx = SkillFactory.getSkill(21120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(21100002, (int) eff.getDAMRate());
                    damageIncrease.put(21110003, (int) eff.getDAMRate());
                }
                break;
            }
            case 3511:
            case 3512:
                bx = SkillFactory.getSkill(35110014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //ME-07 Drillhands, Atomic Hammer
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35001003, (int) eff.getDAMRate());
                    damageIncrease.put(35101003, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(35121006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Satellite
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35111001, (int) eff.getDAMRate());
                    damageIncrease.put(35111009, (int) eff.getDAMRate());
                    damageIncrease.put(35111010, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(35120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Satellite
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35111005, eff.getX());
                    damageIncrease.put(35111011, eff.getX());
                    damageIncrease.put(35121009, eff.getX());
                    damageIncrease.put(35121010, eff.getX());
                    damageIncrease.put(35121011, eff.getX());
                    BuffUP_Summon += eff.getY();
                }
                break;
            case 110:
            case 111:
            case 112:
                bx = SkillFactory.getSkill(1100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1110009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= eff.getDamage() / 100.0;
                    bossdam_r *= eff.getDamage() / 100.0;
                }
                bx = SkillFactory.getSkill(1120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                break;
            case 120:
            case 121:
            case 122:
                bx = SkillFactory.getSkill(1200009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1220006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                }
                break;
            case 511:
            case 512:
                bx = SkillFactory.getSkill(5110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Backspin Blow, Double Uppercut, and Corkscrew Blow
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5101002, eff.getX());
                    damageIncrease.put(5101003, eff.getY());
                    damageIncrease.put(5101004, eff.getZ());
                }
                break;
            case 520:
            case 521:
            case 522:
                defRange = 200;
                bx = SkillFactory.getSkill(5220001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Flamethrower and Ice Splitter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5211004, (int) eff.getDamage());
                    damageIncrease.put(5211005, (int) eff.getDamage());
                }
                break;
            case 130:
            case 131:
            case 132:
                bx = SkillFactory.getSkill(1300009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1310009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    passive_sharpeye_rate += eff.getCr();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                    hpRecoverProp += eff.getProb();
                    hpRecoverPercent += eff.getX();
                }
                bx = SkillFactory.getSkill(1320006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            case 411:
            case 412:
                bx = SkillFactory.getSkill(4110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    RecoveryUP += eff.getX() - 100;
                    BuffUP += eff.getY() - 100;
                }
                bx = SkillFactory.getSkill(4120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Lucky Seven, Drain, Avenger, Shadow Meso
                    eff = bx.getEffect(bof);
                    damageIncrease.put(4001344, (int) eff.getDAMRate());
                    damageIncrease.put(4101005, (int) eff.getDAMRate());
                    damageIncrease.put(4111004, (int) eff.getDAMRate());
                    damageIncrease.put(4111005, (int) eff.getDAMRate());
                }
                break;
            case 1400:
            case 1410:
            case 1411:
            case 1412:
                defRange = 200;
                bx = SkillFactory.getSkill(14110003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    RecoveryUP += eff.getX() - 100;
                    BuffUP += eff.getY() - 100;
                }
                bx = SkillFactory.getSkill(14000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                break;
        }
        if (GameConstants.isResist(chra.getJob())) {
            bx = SkillFactory.getSkill(30000002);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                RecoveryUP += bx.getEffect(bof).getX() - 100;
            }
        }
        CalcPassive_SharpEye(chra);
        CalcPassive_Mastery(chra);
        recalcPVPRank(chra);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }

        calculateMaxBaseDamage(Math.max(magic, watk), pvpDamage, chra);
        trueMastery = Math.min(100, trueMastery);
        passive_sharpeye_min_percent = (short) Math.min(passive_sharpeye_min_percent, passive_sharpeye_percent);
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            chra.updatePartyMemberHP();
        }
    }

    public boolean checkEquipLevels(final MapleCharacter chr, int gain) {
        if (chr.isClone()) {
            return false;
        }
        boolean changed = false;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Equip> all = new ArrayList<Equip>(equipLevelHandling);
        for (Equip eq : all) {
            int lvlz = eq.getEquipLevel();
            eq.setItemEXP(eq.getItemEXP() + gain);

            if (eq.getEquipLevel() > lvlz) { //lvlup
                for (int i = eq.getEquipLevel() - lvlz; i > 0; i--) {
                    //now for the equipment increments...
                    final Map<Integer, Map<String, Integer>> inc = ii.getEquipIncrements(eq.getItemId());
                    if (inc != null && inc.containsKey(lvlz + i)) { //flair = 1
                        eq = ii.levelUpEquip(eq, inc.get(lvlz + i));
                    }
                    //UGH, skillz
                    if (GameConstants.getStatFromWeapon(eq.getItemId()) == null && GameConstants.getMaxLevel(eq.getItemId()) < (lvlz + i) && Math.random() < 0.1 && eq.getIncSkill() <= 0 && ii.getEquipSkills(eq.getItemId()) != null) {
                        for (int zzz : ii.getEquipSkills(eq.getItemId())) {
                            final Skill skil = SkillFactory.getSkill(zzz);
                            if (skil != null && skil.canBeLearnedBy(chr.getJob())) { //dont go over masterlevel :D
                                eq.setIncSkill(skil.getId());
                                chr.dropMessage(5, "Your skill has gained a levelup: " + skil.getName() + " +1");
                            }
                        }
                    }
                }
                changed = true;
            }
            chr.forceReAddItem(eq.copy(), MapleInventoryType.EQUIPPED);
        }
        if (changed) {
            chr.equipChanged();
            chr.getClient().getSession().write(MaplePacketCreator.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }

    public boolean checkEquipDurabilitys(final MapleCharacter chr, int gain) {
        return checkEquipDurabilitys(chr, gain, false);
    }

    public boolean checkEquipDurabilitys(final MapleCharacter chr, int gain, boolean aboveZero) {
        if (chr.isClone() || chr.inPVP()) {
            return true;
        }
        List<Equip> all = new ArrayList<Equip>(durabilityHandling);
        for (Equip item : all) {
            if (item != null && ((item.getPosition() >= 0) == aboveZero)) {
                item.setDurability(item.getDurability() + gain);
                if (item.getDurability() < 0) { //shouldnt be less than 0
                    item.setDurability(0);
                }
            }
        }
        for (Equip eqq : all) {
            if (eqq != null && eqq.getDurability() == 0 && eqq.getPosition() < 0) { //> 0 went to negative
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    chr.getClient().getSession().write(MaplePacketCreator.getInventoryFull());
                    chr.getClient().getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                durabilityHandling.remove(eqq);
                final short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos);
            } else if (eqq != null) {
                chr.forceReAddItem(eqq.copy(), MapleInventoryType.EQUIPPED);
            }
        }
        return true;
    }

    public final void handleProfessionTool(final MapleCharacter chra) {
        if (chra.getProfessionLevel(92000000) > 0 || chra.getProfessionLevel(92010000) > 0) {
            final Iterator<Item> itera = chra.getInventory(MapleInventoryType.EQUIP).newList().iterator();
            while (itera.hasNext()) { //goes to first harvesting tool and stops
                final Equip equip = (Equip) itera.next();
                if (equip.getDurability() != 0 && (equip.getItemId() / 10000 == 150 && chra.getProfessionLevel(92000000) > 0) || (equip.getItemId() / 10000 == 151 && chra.getProfessionLevel(92010000) > 0)) {
                    if (equip.getDurability() > 0) {
                        durabilityHandling.add(equip);
                    }
                    harvestingTool = equip.getPosition();
                    break;
                }
            }
        }
    }

    private final void CalcPassive_Mastery(final MapleCharacter player) {
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11) == null) {
            passive_mastery = 0;
            return;
        }
        final int skil;
        final MapleWeaponType weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11).getItemId());
        boolean acc = true;
        switch (weaponType) {
            case BOW://弓
                skil = 3100000;
                break;
            case CLAW://拳套
                skil = 4100000;
                break;
            case CANNON:
                skil = 5300005;
                break;
            case KATARA:
            case DAGGER:
                skil = player.getJob() >= 430 && player.getJob() <= 434 ? 4300000 : 4200000;
                break;
            case CROSSBOW://弩
                skil = 3200000;
                break;
            case AXE1H://斧头
            case AXE2H:
                skil = 1100001;
                break;
            case BLUNT1H://钝器
            case BLUNT2H:
                skil = 1200001;
                break;
            case SWORD1H://剑
            case SWORD2H:
                skil = (player.getJob() == 110 || player.getJob() == 111 || player.getJob() == 112) ? 1100000 : 1200000;
                break;
            case POLE_ARM://抢
                skil = 1300000;
                break;
            case SPEAR://矛
                skil = 1300001;
                break;
            case KNUCKLE:
                skil = GameConstants.isKOC(player.getJob()) ? 15100001 : 5100001;
                break;
            case GUN:
                skil = GameConstants.isResist(player.getJob()) ? 35100000 : 5200000;
                break;
            case DUAL_BOW:
                skil = 23100005;
                break;
            case WAND:
            case STAFF:
                acc = false;
                skil = GameConstants.isResist(player.getJob()) ? 32100006 : (player.getJob() <= 212 ? 2100006 : (player.getJob() <= 222 ? 2200006 : (player.getJob() <= 232 ? 2300006 : (player.getJob() <= 2000 ? 12100007 : 22120002))));
                break;
            default:
                passive_mastery = 0;
                return;

        }
        if (player.getSkillLevel(skil) <= 0) {
            passive_mastery = 0;
            return;
        }
        final MapleStatEffect eff = SkillFactory.getSkill(skil).getEffect(player.getTotalSkillLevel(skil));
        if (acc) {
            accuracy += eff.getX();
            if (skil == 35100000) {
                watk += eff.getX();
            }
        } else {
            magic += eff.getX();
        }
        passive_sharpeye_rate += eff.getCr();
        passive_mastery = (byte) eff.getMastery(); //after bb, simpler?
        trueMastery += eff.getMastery() + weaponType.getBaseMastery();
    }

    private final void CalcPassive_SharpEye(final MapleCharacter player) {
        if (GameConstants.isResist(player.getJob())) {
            Skill critSkill = SkillFactory.getSkill(30000022);
            int critlevel = player.getTotalSkillLevel(critSkill);
            if (critlevel > 0) {
                passive_sharpeye_rate += critSkill.getEffect(critlevel).getProb();
                this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
            }
            critSkill = SkillFactory.getSkill(30010022);
            critlevel = player.getTotalSkillLevel(critSkill);
            if (critlevel > 0) {
                passive_sharpeye_rate += critSkill.getEffect(critlevel).getProb();
                this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
            }
        }
        switch (player.getJob()) { // Apply passive Critical bonus
            case 410:
            case 411:
            case 412: { // Assasin/ Hermit / NL
                final Skill critSkill = SkillFactory.getSkill(4100001);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 1410:
            case 1411:
            case 1412: { // Night Walker
                final Skill critSkill = SkillFactory.getSkill(14100001);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 3100:
            case 3110:
            case 3111:
            case 3112: {
                final Skill critSkill = SkillFactory.getSkill(31100006); //TODO LEGEND, not final
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.watk += critSkill.getEffect(critlevel).getAttackX();
                    return;
                }
                break;
            }
            case 2300:
            case 2310:
            case 2311:
            case 2312: {
                final Skill critSkill = SkillFactory.getSkill(23000003);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 3210:
            case 3211:
            case 3212: {
                final Skill critSkill = SkillFactory.getSkill(32100006);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 434: {
                final Skill critSkill = SkillFactory.getSkill(4340010);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 520:
            case 521:
            case 522: {
                final Skill critSkill = SkillFactory.getSkill(5200007);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 1211:
            case 1212: {
                final Skill critSkill = SkillFactory.getSkill(12110000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 530:
            case 531:
            case 532: {
                final Skill critSkill = SkillFactory.getSkill(5300004);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getCr());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                }
                break;
            }
            case 510:
            case 511:
            case 512: { // Buccaner, Viper
                final Skill critSkill = SkillFactory.getSkill(5110000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) critSkill.getEffect(critlevel).getProb();
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                }
                break;
            }
            case 1511:
            case 1512: {
                final Skill critSkill = SkillFactory.getSkill(15110000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                }
                break;
            }
            case 2111:
            case 2112: {
                final Skill critSkill = SkillFactory.getSkill(21110000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) ((critSkill.getEffect(critlevel).getX() * critSkill.getEffect(critlevel).getY()) + critSkill.getEffect(critlevel).getCr());
                    return;
                }
                break;
            }
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322: { // Bowman
                final Skill critSkill = SkillFactory.getSkill(3000001);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 1300:
            case 1310:
            case 1311:
            case 1312: { // Bowman
                final Skill critSkill = SkillFactory.getSkill(13000000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218: { //Evan
                final Skill critSkill = SkillFactory.getSkill(22140000);
                final int critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate += (short) (critSkill.getEffect(critlevel).getProb());
                    this.passive_sharpeye_min_percent += critSkill.getEffect(critlevel).getCriticalMin();
                    return;
                }
                break;
            }
        }
    }

    public final short passive_sharpeye_min_percent() {
        return passive_sharpeye_min_percent;
    }

    public final short passive_sharpeye_percent() {
        return passive_sharpeye_percent;
    }

    public final short passive_sharpeye_rate() {
        return passive_sharpeye_rate;
    }

    public final byte passive_mastery() {
        return passive_mastery; //* 5 + 10 for mastery %
    }

    public final void calculateMaxBaseDamage(final int watk, final int pvpDamage, MapleCharacter chra) {
        if (watk <= 0) {
            localmaxbasedamage = 1;
            localmaxbasepvpdamage = 1;
        } else {
            final Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            final Item weapon_item2 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
            final int job = chra.getJob();
            final MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item.getItemId());
            final MapleWeaponType weapon2 = weapon_item2 == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item2.getItemId());
            int mainstat, secondarystat, mainstatpvp, secondarystatpvp;
            final boolean mage = (job >= 200 && job <= 232) || (job >= 1200 && job <= 1212) || (job >= 2200 && job <= 2218) || (job >= 3200 && job <= 3212);
            switch (weapon) {
                case BOW:
                case CROSSBOW:
                case GUN:
                    mainstat = localdex;
                    secondarystat = localstr;
                    mainstatpvp = dex;
                    secondarystatpvp = str;
                    break;
                case DAGGER:
                case KATARA:
                case CLAW:
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                    mainstatpvp = luk;
                    secondarystatpvp = dex + str;
                    break;
                default:
                    if (mage) {
                        mainstat = localint_;
                        secondarystat = localluk;
                        mainstatpvp = int_;
                        secondarystatpvp = luk;
                    } else {
                        mainstat = localstr;
                        secondarystat = localdex;
                        mainstatpvp = str;
                        secondarystatpvp = dex;
                    }
                    break;
            }
            localmaxbasepvpdamage = weapon.getMaxDamageMultiplier() * (4 * mainstatpvp + secondarystatpvp) * (100.0f + (pvpDamage / 100.0f));
            localmaxbasepvpdamageL = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (100.0f + (pvpDamage / 100.0f));
            if (weapon2 != MapleWeaponType.NOT_A_WEAPON && weapon_item != null && weapon_item2 != null) {
                Equip we1 = (Equip) weapon_item;
                Equip we2 = (Equip) weapon_item2;
                localmaxbasedamage = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we2.getMatk() : we2.getWatk())) / 100.0f);
                localmaxbasedamage += weapon2.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we1.getMatk() : we1.getWatk())) / 100.0f);
            } else {
                localmaxbasedamage = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (watk / 100.0f);
            }
        }
    }

    public final float getHealHP() {
        return shouldHealHP;
    }

    public final float getHealMP() {
        return shouldHealMP;
    }

    public final void relocHeal(MapleCharacter chra) {
        if (chra.isClone()) {
            return;
        }
        final int playerjob = chra.getJob();

        shouldHealHP = 10 + recoverHP; // Reset
        shouldHealMP = GameConstants.isDemon(chra.getJob()) ? 0 : (3 + mpRestore + recoverMP + (localint_ / 10)); // i think
        mpRecoverTime = 0;
        hpRecoverTime = 0;
        if (playerjob == 111 || playerjob == 112) {
            final Skill effect = SkillFactory.getSkill(1110000); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                MapleStatEffect eff = effect.getEffect(lvl);
                if (eff.getHp() > 0) {
                    shouldHealHP += eff.getHp();
                    hpRecoverTime = 4000;
                }
                shouldHealMP += eff.getMp();
                mpRecoverTime = 4000;
            }

        } else if (playerjob == 1111 || playerjob == 1112) {
            final Skill effect = SkillFactory.getSkill(11110000); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealMP += effect.getEffect(lvl).getMp();
                mpRecoverTime = 4000;
            }
        } else if (GameConstants.isMercedes(playerjob)) {
            final Skill effect = SkillFactory.getSkill(20020109); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealHP += (effect.getEffect(lvl).getX() * localmaxhp) / 100;
                hpRecoverTime = 4000;
                shouldHealMP += (effect.getEffect(lvl).getX() * localmaxmp) / 100;
                mpRecoverTime = 4000;
            }
        } else if (playerjob == 3111 || playerjob == 3112) {
            final Skill effect = SkillFactory.getSkill(31110009); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealMP += effect.getEffect(lvl).getY();
                mpRecoverTime = 4000;
            }
        }
        if (chra.getChair() != 0) { // Is sitting on a chair.
            shouldHealHP += 99; // Until the values of Chair heal has been fixed,
            shouldHealMP += 99; // MP is different here, if chair data MP = 0, heal + 1.5
        } else if (chra.getMap() != null) { // Because Heal isn't multipled when there's a chair :)
            final float recvRate = chra.getMap().getRecoveryRate();
            if (recvRate > 0) {
                shouldHealHP *= recvRate;
                shouldHealMP *= recvRate;
            }
        }
    }

    public final void connectData(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(str); // str
        mplew.writeShort(dex); // dex
        mplew.writeShort(int_); // int
        mplew.writeShort(luk); // luk
        mplew.writeShort(hp); // hp -- INT after bigbang
        mplew.writeShort(maxhp); // maxhp
        mplew.writeShort(mp); // mp
        mplew.writeShort(maxmp); // maxmp
    }
    private final static int[] allJobs = {0, 10000, 10000000, 20000000, 20010000, 20020000, 30000000, 30010000};
    public final static int[] pvpSkills = {1000007, 2000007, 3000006, 4000010, 5000006, 5010004, 11000006, 12000006, 13000005, 14000006, 15000005, 21000005, 22000002, 23000004, 31000005, 32000012, 33000004, 35000005};

    public final static int getSkillByJob(final int skillID, final int job) {
        if (GameConstants.isKOC(job)) {
            return skillID + 10000000;
        } else if (GameConstants.isAran(job)) {
            return skillID + 20000000;
        } else if (GameConstants.isEvan(job)) {
            return skillID + 20010000;
        } else if (GameConstants.isMercedes(job)) {
            return skillID + 20020000;
        } else if (GameConstants.isDemon(job)) {
            return skillID + 30010000;
        } else if (GameConstants.isResist(job)) {
            return skillID + 30000000;
            //     } else if (GameConstants.isCannon(job)) {
            //         return skillID + 10000;
        }
        return skillID;
    }

    public final int getSkillIncrement(final int skillID) {
        if (skillsIncrement.containsKey(skillID)) {
            return skillsIncrement.get(skillID);
        }
        return 0;
    }

    public final int getElementBoost(final Element key) {
        if (elemBoosts.containsKey(key)) {
            return elemBoosts.get(key);
        }
        return 0;
    }

    public final int getDamageIncrease(final int key) {
        if (damageIncrease.containsKey(key)) {
            return damageIncrease.get(key);
        }
        return 0;
    }

    public final int getAccuracy() {
        return accuracy;
    }

    public void heal_noUpdate(MapleCharacter chra) {
        setHp(getCurrentMaxHp(), chra);
        setMp(getCurrentMaxMp(), chra);
    }

    public void heal(MapleCharacter chra) {
        heal_noUpdate(chra);
        chra.updateSingleStat(MapleStat.HP, getCurrentMaxHp());
        chra.updateSingleStat(MapleStat.MP, getCurrentMaxMp());
    }

    public void handlePotential(StructPotentialItem pot, MapleCharacter chra, boolean first_login) {
        localstr += pot.incSTR;
        localdex += pot.incDEX;
        localint_ += pot.incINT;
        localluk += pot.incLUK;
        wdef += pot.incPDD;
        mdef += pot.incMDD;
        watk += pot.incPAD;
        magic += pot.incMAD;
        accuracy += pot.incACC;
        incAllskill += pot.incAllskill;
        ignoreTargetDEF += pot.ignoreTargetDEF;
        bossdam_r *= (pot.incDAMr + 100.0) / 100.0;
        if (!pot.boss) {
            dam_r *= (pot.incDAMr + 100.0) / 100.0;
        }
        recoverHP += pot.RecoveryHP;
        recoverMP += pot.RecoveryMP;
        RecoveryUP += pot.RecoveryUP;
        if (pot.HP > 0) {
            hpRecover += pot.HP;
            hpRecoverProp += pot.prop;
        }
        if (pot.MP > 0 && !GameConstants.isDemon(chra.getJob())) {
            mpRecover += pot.MP;
            mpRecoverProp += pot.prop;
        }
        /*if (pot.time > 0 && pot.prop == 0) {
            decreaseDebuff += pot.time * 1000;
        }*/
        mpconReduce += pot.mpconReduce;
        incMesoProp += pot.incMesoProp;
        incRewardProp += pot.incRewardProp;
        if (pot.DAMreflect > 0) {
            DAMreflect += pot.DAMreflect;
            DAMreflect_rate += pot.prop;
        }
        percent_hp += pot.incMHPr;
        percent_mp += pot.incMMPr;
        percent_str += pot.incSTRr;
        percent_dex += pot.incDEXr;
        percent_int += pot.incINTr;
        percent_luk += pot.incLUKr;
        percent_acc += pot.incACCr;
        percent_atk += pot.incPADr;
        percent_matk += pot.incMADr;
        percent_wdef += pot.incPDDr;
        percent_mdef += pot.incMDDr;
        passive_sharpeye_rate += pot.incCr;
        mpRestore += pot.mpRestore;
        if (first_login && pot.skillID > 0) {
            chra.changeSkillLevel_Skip(SkillFactory.getSkill(getSkillByJob(pot.skillID, chra.getJob())), (byte) 1, (byte) 0);
        }
    }

    public void recalcPVPRank(MapleCharacter chra) {
        this.pvpRank = 10;
        this.pvpExp = chra.getTotalBattleExp();
        for (int i = 0; i < 10; i++) {
            if (pvpExp > GameConstants.getPVPExpNeededForLevel(i + 1)) {
                pvpRank--;
                pvpExp -= GameConstants.getPVPExpNeededForLevel(i + 1);
            }
        }
    }

    public int getHPPercent() {
        return (int) Math.ceil((hp * 100.0) / localmaxhp);
    }
}
