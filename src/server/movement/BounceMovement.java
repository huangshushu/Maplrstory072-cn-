package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class BounceMovement extends AbstractLifeMovement {
    private Point offset;

    public BounceMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setOffset(Point offset) {
        this.offset = offset;
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writePos(offset);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
