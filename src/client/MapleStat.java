package client;

import constants.GameConstants;

public enum MapleStat {
    SKIN(0x1),
    FACE(0x2),
    HAIR(0x4),
    PET(0x38),
    LEVEL(0x40),
    JOB(0x80),
    STR(0x100),
    DEX(0x200),
    INT(0x400),
    LUK(0x800),
    HP(0x1000),
    MAXHP(0x2000),
    MP(0x4000),
    MAXMP(0x8000),
    AVAILABLEAP(0x10000),
    AVAILABLESP(0x20000),
    EXP(0x40000),
    FAME(0x80000),
    MESO(0x100000),
    GACHAPONEXP(0x200000),
    FATIGUE(0x400000),
    CHARISMA(0x800000),
    INSIGHT(0x1000000),
    WILL(0x2000000),
    CRAFT(0x4000000),
    SENSE(0x8000000),
    CHARM(0x10000000),
    TRAIT_LIMIT(0x20000000),
    BATTLE_EXP(0x40000000),
    BATTLE_RANK(0x80000000L),
    BATTLE_POINTS(0x100000000L),
    ICE_GAGE(0x200000000L),
    VIRTUE(0x400000000L);
    
    private final long i;
    
    private MapleStat(long i) {
        this.i = i;
    }

    public long getValue() {
        return i;
    }

    public static final MapleStat getByValue(final long value) {
        for (final MapleStat stat : MapleStat.values()) {
            if (stat.i == value) {
                return stat;
            }
        }
        return null;
    }

    public static enum Temp {
        STR(0x1),
        DEX(0x2),
        INT(0x4),
        LUK(0x8),
        WATK(0x10),
        WDEF(0x20),
        MATK(0x40),
        MDEF(0x80),
        ACC(0x100),
        AVOID(0x200),
        SPEED(0x400),
        JUMP(0x800);
        
        private final int i;

        private Temp(int i) {
            this.i = i;
        }

        public int getValue() {
            return i;
        }
    }
}
