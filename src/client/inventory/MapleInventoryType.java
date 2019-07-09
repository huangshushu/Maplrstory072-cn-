package client.inventory;

public enum MapleInventoryType {
    UNDEFINED(0), //2
    EQUIP(1), //4
    USE(2), //8
    SETUP(3), //10
    ETC(4), //20
    CASH(5), //40
    EQUIPPED(-1);
    final byte type;

    private MapleInventoryType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    public static MapleInventoryType getByType(byte type) {
        for (MapleInventoryType l : MapleInventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleInventoryType getByWZName(String name) {
        if (name.equals("Install")) {
            return SETUP;
        } else if (name.equals("Consume")) {
            return USE;
        } else if (name.equals("Etc")) {
            return ETC;
        } else if (name.equals("Eqp")) {
            return EQUIP;
        } else if (name.equals("Cash")) {
            return CASH;
        } else if (name.equals("Pet")) {
            return CASH;
        }
        return UNDEFINED;
    }
}
