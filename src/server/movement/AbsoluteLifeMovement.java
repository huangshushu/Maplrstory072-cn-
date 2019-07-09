package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {
    private Point pixelsPerSecond, offset;
    private short unk, fh;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public void setOffset(Point wobble) {
        this.offset = wobble;
    }

    public void setFh(short fh) {
        this.fh = fh;
    }

    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return unk;
    }

    public void defaulted() {
        unk = 0;
        fh = 0;
        pixelsPerSecond = new Point(0, 0);
        offset = new Point(0, 0);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writePos(pixelsPerSecond);
        lew.writeShort(unk);
        if (getType() == 0xF) {
            lew.writeShort(fh);
        }
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
