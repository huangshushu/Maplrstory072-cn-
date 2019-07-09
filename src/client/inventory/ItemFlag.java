package client.inventory;

public enum ItemFlag {
    LOCK(0x01),
    SPIKES(0x02),
    COLD(0x04),
    UNTRADEABLE(0x08),
    KARMA_EQ(0x10),
    KARMA_USE(0x02),
    CHARM_EQUIPPED(0x20),
    ANDROID_ACTIVATED(0x40),
    CRAFTED(0x80), 
    CRAFTED_USE(0x10),
    SHIELD_WARD(0x100), //shield icon
    LUCKS_KEY(0x200), //this has some clover leaf thing at bottomleft
    KARMA_ACC_USE(0x400),
    KARMA_ACC(0x1000);
    
    private final int i;

    private ItemFlag(int i) {
        this.i = i;
    }

    public final int getValue() {
        return i;
    }

    public final boolean check(int flag) {
        return (flag & i) == i;
    }
}
