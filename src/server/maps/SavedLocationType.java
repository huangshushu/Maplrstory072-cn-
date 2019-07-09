package server.maps;

public enum SavedLocationType {
    FREE_MARKET(0),
    MULUNG_TC(1),
    WORLDTOUR(2),
    FLORINA(3),
    FISHING(4),
    RICHIE(5),
    DONGDONGCHIANG(6),
    EVENT(7),
    AMORIA(8),
    CHRISTMAS(9),
    ARDENTMILL(10),
    PVP(11),
    ARIANT(12),
    ENGLISH(13),
    MONSTER_CARNIVAL(14),
    WEDDING(15),
    PACH(16),
    SLEEP(17),
    ;

    private int index;

    private SavedLocationType(int index) {
        this.index = index;
    }

    public int getValue() {
        return index;
    }

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }
}
