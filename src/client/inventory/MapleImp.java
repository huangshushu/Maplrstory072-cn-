package client.inventory;

import java.io.Serializable;

public class MapleImp implements Serializable {
    public static enum ImpFlag {
	REMOVED(0x1),
	SUMMONED(0x2),
	TYPE(0x4),
	STATE(0x8),
	FULLNESS(0x10),
	CLOSENESS(0x20),
	CLOSENESS_LEFT(0x40),
	MINUTES_LEFT(0x80),
	LEVEL(0x100),
	FULLNESS_2(0x200),
	UPDATE_TIME(0x400),
	CREATE_TIME(0x800),
	AWAKE_TIME(0x1000),//idk, i think it only writes sleep time
	SLEEP_TIME(0x2000), 
	MAX_CLOSENESS(0x4000),
	MAX_DELAY(0x8000),
	MAX_FULLNESS(0x10000),
	MAX_ALIVE(0x20000),
	MAX_MINUTES(0x40000);
	//probably more but idk them

	private final int i;
        
	private ImpFlag(int i) {
	    this.i = i;
	}

	public final int getValue() {
	    return i;
	}

	public final boolean check(int flag) {
	    return (flag & i) == i;
	}
    }

    private static final long serialVersionUID = 91795493413738569L;
    private int itemid;
    private short fullness = 0, closeness = 0;
	private byte state = 1, level = 1;

    public MapleImp(final int itemid) {
        this.itemid = itemid;
    }
	
    public final int getItemId() {
        return itemid;
    }
	
    public final byte getState() {
        return state;
    }

    public final void setState(final int state) {
        this.state = (byte) state;
    }
	
    public final byte getLevel() {
        return level;
    }

    public final void setLevel(final int level) {
        this.level = (byte) level;
    }
	
    public final short getCloseness() {
        return closeness;
    }

    public final void setCloseness(final int closeness) {
        this.closeness = (short) Math.min(100, closeness);
    }

    public final short getFullness() {
        return fullness;
    }

    public final void setFullness(final int fullness) {
        this.fullness = (short) Math.min(1000, fullness);
    }
}

