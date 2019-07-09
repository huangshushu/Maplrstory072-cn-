package client;

import constants.GameConstants;
import handling.Buffstat;
import java.io.Serializable;

public enum MapleBuffStat implements Serializable, Buffstat {
    WATK(0x1, 1),
    WDEF(0x2, 1),
    MATK(0x4, 1),
    MDEF(0x8, 1),
    ACC(0x10, 1),
    AVOID(0x20, 1),
    HANDS(0x40, 1),
    SPEED(0x80, 1),
    JUMP(0x100, 1),
    MAGIC_GUARD(0x200, 1),
    DARKSIGHT(0x400, 1),
    BOOSTER(0x800, 1),
    POWERGUARD(0x1000, 1),
    MAXHP(0x2000, 1),
    MAXMP(0x4000, 1),
    INVINCIBLE(0x8000, 1),
    SOULARROW(0x10000, 1),
    //2 - debuff
    //4 - debuff
    //8 - debuff

    //1 - debuff
    COMBO(0x200000, 1),
    SUMMON(0x200000, 1), //hack buffstat for summons ^.- (does/should not increase damage... hopefully <3)
    WK_CHARGE(0x400000, 1),
    DRAGONBLOOD(0x800000, 1),
    HOLY_SYMBOL(0x1000000, 1),
    MESOUP(0x2000000, 1),
    SHADOWPARTNER(0x4000000, 1),
    PICKPOCKET(0x8000000, 1),
    PUPPET(0x8000000, 1), // HACK - shares buffmask with pickpocket - odin special ^.-

    MESOGUARD(0x10000000, 1),
    HP_LOSS_GUARD(0x20000000, 1),
    //4 - debuff
    //8 - debuff

    //1 - debuff
    MORPH(0x2, 2),
    RECOVERY(0x4, 2),
    MAPLE_WARRIOR(0x8, 2),
    STANCE(0x10, 2),
    SHARP_EYES(0x20, 2),
    MANA_REFLECTION(0x40, 2),
    //8 - debuff

    SPIRIT_CLAW(0x100, 2),
    INFINITY(0x200, 2),
    HOLY_SHIELD(0x400, 2), //advanced blessing after ascension
    HAMSTRING(0x800, 2),
    BLIND(0x1000, 2),
    CONCENTRATE(0x2000, 2),
    //4 - debuff
    ECHO_OF_HERO(0x8000, 2),
    BOUNDLESS_RAGE(0x80000000, 5),
    MESO_RATE(0x10000, 2), //confirmed
    GHOST_MORPH(0x20000, 2),
    ARIANT_COSS_IMU(0x40000, 2), // The white ball around you
    //8 - debuff

    DROP_RATE(0x100000, 2), //confirmed
    //2 = unknown
    EXPRATE(0x400000, 2),
    ACASH_RATE(0x800000, 2),
    ILLUSION(0x1000000, 2), //hack buffstat
    //1 = unknown, and for gms, 2 and 4 are unknown
    BERSERK_FURY(0x8000000, 2),
    DIVINE_BODY(0x10000000, 2),
    SPARK(0x20000000, 2),
    ARIANT_COSS_IMU2(0x40000000, 2), // no idea, seems the same
    FINALATTACK(0x80000000, 2),
    //4 = unknown
    ELEMENT_RESET(0x2, 3),
    WIND_WALK(0x4, 3),
    ARAN_COMBO(0x10, 3),
    COMBO_DRAIN(0x20, 3),
    COMBO_BARRIER(0x40, 3),
    BODY_PRESSURE(0x80, 3),
    SMART_KNOCKBACK(0x100, 3),
    PYRAMID_PQ(0x200, 3),
    //1 - unknown
    //2 - debuff
    //4 - debuff
    //8 - debuff

    SLOW(GameConstants.GMS ? 0x4000 : 0x1000, 3),
    MAGIC_SHIELD(GameConstants.GMS ? 0x8000 : 0x2000, 3),
    MAGIC_RESISTANCE(GameConstants.GMS ? 0x10000 : 0x4000, 3),
    SOUL_STONE(GameConstants.GMS ? 0x20000 : 0x8000, 3),
    SOARING(GameConstants.GMS ? 0x40000 : 0x10000, 3),
    //2 - debuff
    LIGHTNING_CHARGE(GameConstants.GMS ? 0x100000 : 0x40000, 3),
    ENRAGE(GameConstants.GMS ? 0x200000 : 0x80000, 3),
    OWL_SPIRIT(GameConstants.GMS ? 0x400000 : 0x100000, 3),
    FINAL_CUT(GameConstants.GMS ? 0x1000000 : 0x400000, 3),
    DAMAGE_BUFF(GameConstants.GMS ? 0x2000000 : 0x800000, 3),
    ATTACK_BUFF(GameConstants.GMS ? 0x4000000 : 0x1000000, 3), //attack %? feline berserk
    RAINING_MINES(GameConstants.GMS ? 0x8000000 : 0x2000000, 3),
    ENHANCED_MAXHP(GameConstants.GMS ? 0x10000000 : 0x4000000, 3),
    ENHANCED_MAXMP(GameConstants.GMS ? 0x20000000 : 0x8000000, 3),
    ENHANCED_WATK(GameConstants.GMS ? 0x40000000 : 0x10000000, 3),
    //2 unknown
    ENHANCED_WDEF(GameConstants.GMS ? 0x1 : 0x40000000, GameConstants.GMS ? 4 : 3),
    ENHANCED_MDEF(GameConstants.GMS ? 0x2 : 0x80000000, GameConstants.GMS ? 4 : 3),
    PERFECT_ARMOR(GameConstants.GMS ? 0x4 : 0x1, 4),
    SATELLITESAFE_PROC(GameConstants.GMS ? 0x8 : 0x2, 4),
    SATELLITESAFE_ABSORB(GameConstants.GMS ? 0x10 : 0x4, 4),
    TORNADO(GameConstants.GMS ? 0x20 : 0x8, 4),
    CRITICAL_RATE_BUFF(GameConstants.GMS ? 0x40 : 0x10, 4),
    MP_BUFF(GameConstants.GMS ? 0x80 : 0x20, 4),
    DAMAGE_TAKEN_BUFF(GameConstants.GMS ? 0x100 : 0x40, 4),
    DODGE_CHANGE_BUFF(GameConstants.GMS ? 0x200 : 0x80, 4),
    CONVERSION(GameConstants.GMS ? 0x400 : 0x100, 4),
    REAPER(GameConstants.GMS ? 0x800 : 0x200, 4),
    INFILTRATE(GameConstants.GMS ? 0x1000 : 0x400, 4),
    MECH_CHANGE(GameConstants.GMS ? 0x2000 : 0x800, 4),
    AURA(GameConstants.GMS ? 0x4000 : 0x1000, 4),
    DARK_AURA(GameConstants.GMS ? 0x8000 : 0x2000, 4),
    BLUE_AURA(GameConstants.GMS ? 0x10000 : 0x4000, 4),
    YELLOW_AURA(GameConstants.GMS ? 0x20000 : 0x8000, 4),
    BODY_BOOST(GameConstants.GMS ? 0x40000 : 0x10000, 4),
    FELINE_BERSERK(GameConstants.GMS ? 0x80000 : 0x20000, 4),
    DICE_ROLL(GameConstants.GMS ? 0x100000 : 0x40000, 4),
    DIVINE_SHIELD(GameConstants.GMS ? 0x200000 : 0x80000, 4),
    PIRATES_REVENGE(GameConstants.GMS ? 0x400000 : 0x100000, 4),
    TELEPORT_MASTERY(GameConstants.GMS ? 0x800000 : 0x200000, 4),
    COMBAT_ORDERS(GameConstants.GMS ? 0x1000000 : 0x400000, 4),
    BEHOLDER(GameConstants.GMS ? 0x2000000 : 0x800000, 4),
    //1 = debuff
    GIANT_POTION(GameConstants.GMS ? 0x8000000 : 0x2000000, 4),
    ONYX_SHROUD(GameConstants.GMS ? 0x10000000 : 0x4000000, 4),
    ONYX_WILL(GameConstants.GMS ? 0x20000000 : 0x8000000, 4),
    //1 = debuff
    BLESS(GameConstants.GMS ? 0x80000000 : 0x20000000, 4),
    //4 8 unknown

    THREATEN_PVP(GameConstants.GMS ? 0x4 : 0x1, 5),
    ICE_KNIGHT(GameConstants.GMS ? 0x8 : 0x2, 5),
    //4 unknown
    STR(GameConstants.GMS ? 0x20 : 0x8, 5),
    DEX(GameConstants.GMS ? 0x40 : 0x10, 5),
    INT(GameConstants.GMS ? 0x80 : 0x20, 5),
    LUK(GameConstants.GMS ? 0x100 : 0x40, 5),
    //8 unknown

    //1 2 unknown
    ANGEL_ATK(GameConstants.GMS ? 0x1000 : 0x400, 5, true),
    ANGEL_MATK(GameConstants.GMS ? 0x2000 : 0x800, 5, true),
    HP_BOOST(GameConstants.GMS ? 0x4000 : 0x1000, 5, true), //indie hp
    MP_BOOST(GameConstants.GMS ? 0x8000 : 0x2000, 5, true),
    ANGEL_ACC(GameConstants.GMS ? 0x10000 : 0x4000, 5, true),
    ANGEL_AVOID(GameConstants.GMS ? 0x20000 : 0x8000, 5, true),
    ANGEL_JUMP(GameConstants.GMS ? 0x40000 : 0x10000, 5, true),
    ANGEL_SPEED(GameConstants.GMS ? 0x80000 : 0x20000, 5, true),
    ANGEL_STAT(GameConstants.GMS ? 0x100000 : 0x40000, 5, true),
    PVP_DAMAGE(GameConstants.GMS ? 0x200000 : 0x4000, 5),
    PVP_ATTACK(GameConstants.GMS ? 0x400000 : 0x8000, 5), //skills
    INVINCIBILITY(GameConstants.GMS ? 0x800000 : 0x10000, 5),
    HIDDEN_POTENTIAL(GameConstants.GMS ? 0x1000000 : 0x20000, 5),
    ELEMENT_WEAKEN(GameConstants.GMS ? 0x2000000 : 0x40000, 5),
    SNATCH(GameConstants.GMS ? 0x4000000 : 0x80000, 5), //however skillid is 90002000, 1500 duration
    FROZEN(GameConstants.GMS ? 0x8000000 : 0x100000, 5),
    //4, unknown
    ICE_SKILL(GameConstants.GMS ? 0x20000000 : 0x400000, 5),
    //1, 2, 4 unknown
    //8 = debuff

    //1, 2 unknown
    HOLY_MAGIC_SHELL(GameConstants.GMS ? 0x10 : 0x4, 6), //max amount of attacks absorbed
    //8 unknown
    ARCANE_AIM(GameConstants.GMS ? 0x40 : 0x10, 6, true),
    BUFF_MASTERY(GameConstants.GMS ? 0x80 : 0x20, 6), //buff duration increase
    //4, 8 unknown
    WATER_SHIELD(GameConstants.GMS ? 0x400 : 0x100, 6),
    SPIRIT_SURGE(GameConstants.GMS ? 0x2000 : 0x200, 6), // fixed yay
    DARK_METAMORPHOSIS(GameConstants.GMS ? 0x800 : 0x80, 6), // mob count
    SPIRIT_LINK(GameConstants.GMS ? 0x4000 : 0x1000, 6), // 0x1000? no..
    //2 unknown 
    VIRTUE_EFFECT(GameConstants.GMS ? 0x10000 : 0x4000, 6),
    //8, 1, 2 unknown
    NO_SLIP(GameConstants.GMS ? 0x100000 : 0x40000, 6),
    FAMILIAR_SHADOW(GameConstants.GMS ? 0x200000 : 0x80000, 6),
    SIDEKICK_PASSIVE(GameConstants.GMS ? 0x400000 : 0x100000, 6), //skillid 79797980  

    ELEMENTAL_STATUS_R(0x10, 6), // GMS is 0x200
    DEFENCE_BOOST_R(0x4000000, 6), // weapon def and magic def
    ABNORMAL_STATUS_R(0x20, 6), // GMS is 0x100
    HP_BOOST_PERCENT(0x200, 7, true),
    MP_BOOST_PERCENT(0x400, 7, true),
    //speshul
    ENERGY_CHARGE(0x8, 3),
    DASH_SPEED(0x10, 3),
    DASH_JUMP(0x20, 3),
    MONSTER_RIDING(0x40, 3),
    SPEED_INFUSION(0x80, 3),
    HOMING_BEACON(0x100, 3, true),
    DEFAULT_BUFFSTAT(0x200, 3),;

    private static final long serialVersionUID = 0L;
    private final int buffstat;
    private final int first;
    private boolean stacked = false;

    private MapleBuffStat(int buffstat, int first) {
        this.buffstat = buffstat;
        this.first = first;
    }

    private MapleBuffStat(int buffstat, int first, boolean stacked) {
        this.buffstat = buffstat;
        this.first = first;
        this.stacked = stacked;
    }

    @Override
    public final int getPosition() {
        return getPosition(false);
    }

    public final int getPosition(boolean fromZero) {
        if (!fromZero) {
            return first; // normal one
        }
        switch (this.first) {
            case 4:
                return 0;
            case 3:
                return 1;
            case 2:
                return 2;
            case 1:
                return 3;
        }
        return 0;
    }

    @Override
    public final int getValue() {
        return buffstat;
    }

    public final boolean canStack() {
        return stacked;
    }
}
