package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class ChairMovement extends AbstractLifeMovement {

    private int unk;
    private Point pixelsPerSecond;

    public ChairMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public int getUnk() {
        return unk;
    }

    public void setUnk(int unk) {
        this.unk = unk;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(pixelsPerSecond);
        lew.writeShort(unk);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
