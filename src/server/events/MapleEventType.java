package server.events;

public enum MapleEventType {
    //椰子比赛(new int[]{109080000}), //just using one
    //CokePlay(new int[]{109080010}), //just using one
    向高地(new int[]{109040000, 109040001, 109040002, 109040003, 109040004}),
    上楼上楼(new int[]{109030001, 109030002, 109030003}),
    //OX问答(new int[]{109020001}),
    //Survival(new int[]{809040000, 809040100}),
    寻宝(new int[]{109060000}); //just using one
    
    public int[] mapids;

    private MapleEventType(int[] mapids) {
        this.mapids = mapids;
    }

    public static final MapleEventType getByString(final String splitted) {
        for (MapleEventType t : MapleEventType.values()) {
            if (t.name().equalsIgnoreCase(splitted)) {
                return t;
            }
        }
        return null;
    }
}
