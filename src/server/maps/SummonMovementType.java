package server.maps;

public enum SummonMovementType {
    STATIONARY(0), //octo etc
    FOLLOW(1), //4th job mage
    WALK_STATIONARY(2), //reaper
    CIRCLE_FOLLOW(3), //bowman summons 
    CIRCLE_STATIONARY(4); //gavi only

    private final int val;

    private SummonMovementType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
