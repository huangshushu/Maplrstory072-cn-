package client;

import constants.GameConstants;
import handling.Buffstat;
import java.io.Serializable;
import tools.Randomizer;

public enum MapleDisease implements Serializable, Buffstat {
    STUN(0x20000, 1, 123),
    POISON(0x40000, 1, 125),
    SEAL(0x80000, 1, 120),
    DARKNESS(0x100000, 1, 121),
    WEAKEN(0x40000000, 1, 122),
    CURSE(0x80000000, 1, 123),
	
    SLOW(0x1, 2, 126),
    MORPH(0x2, 2, 172),
    SEDUCE(0x80, 2, 128),
    ZOMBIFY(0x4000, 2, 133),
    REVERSE_DIRECTION(0x80000, 2, 132),
	
    POTION(GameConstants.GMS ? 0x800 : 0x200, 3, 134),
    SHADOW(GameConstants.GMS ? 0x1000 : 0x400, 3, 135), //receiving damage/moving
    BLIND(GameConstants.GMS ? 0x2000 : 0x800, 3, 136),
    FREEZE(GameConstants.GMS ? 0x80000 : 0x20000, 3, 137),
	
    DISABLE_POTENTIAL(GameConstants.GMS ? 0x4000000 : 0x1000000, 4, 138),
    TORNADO(GameConstants.GMS ? 0x40000000 : 0x10000000, 4, 173),
    FLAG(GameConstants.GMS ? 0x2 : 0x80000000, GameConstants.GMS ? 6 : 5, 799);
	
    // 0x100 is disable skill except buff
    private static final long serialVersionUID = 0L;
    private int i;
    private int first;
    private int disease;

    private MapleDisease(int i, int first, int disease) {
        this.i = i;
        this.first = first;
        this.disease = disease;
    }

    public int getPosition() {
        return first;
    }

    public int getValue() {
        return i;
    }
	
    public int getDisease() {
	return disease;
    }

    public static final MapleDisease getRandom() {
        while (true) {
            for (MapleDisease dis : MapleDisease.values()) {
                if (Randomizer.nextInt(MapleDisease.values().length) == 0) {
                    return dis;
                }
            }
        }
    }

    public static final MapleDisease getBySkill(final int skill) {
        for (MapleDisease d : MapleDisease.values()) {
            if (d.getDisease() == skill) {
                return d;
            }
        }
        return null;
    }
}
