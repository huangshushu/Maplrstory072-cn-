package tools.data;

import constants.ServerConfig;
import java.awt.Point;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LittleEndianAccessor {

    private final ByteArrayByteStream bs;

    public LittleEndianAccessor(final ByteArrayByteStream bs) {
        this.bs = bs;
    }

    public final byte readByte() {
        return (byte) bs.readByte();
    }

    public final int readInt() {
        final int byte1 = bs.readByte();
        final int byte2 = bs.readByte();
        final int byte3 = bs.readByte();
        final int byte4 = bs.readByte();
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public final short readShort() {
        final int byte1 = bs.readByte();
        final int byte2 = bs.readByte();
        return (short) ((byte2 << 8) + byte1);
    }

    public final int readUShort() {
        int quest = readShort();
        if (quest < 0) { //questid 50000 and above, WILL cast to negative, this was tested.
            quest += 65536; //probably not the best fix, but whatever
        }
        return quest;
    }

    public final char readChar() {
        return (char) readShort();
    }

    public final long readLong() {
        final int byte1 = bs.readByte();
        final int byte2 = bs.readByte();
        final int byte3 = bs.readByte();
        final int byte4 = bs.readByte();
        final long byte5 = bs.readByte();
        final long byte6 = bs.readByte();
        final long byte7 = bs.readByte();
        final long byte8 = bs.readByte();

        return (long) ((byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1);
    }

    public final float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public final String readAsciiString(final int n) {
        try {
            final byte ret[] = new byte[n];
            for (int x = 0; x < n; x++) {
                ret[x] = readByte();
            }
            return new String(ret, ServerConfig.ASCII);
        } catch (UnsupportedEncodingException ex) {
        }
        return "";
    }

    public final long getBytesRead() {
        return bs.getBytesRead();
    }

    public final String readMapleAsciiString() {
        return readAsciiString(readShort());
    }

    public final Point readPos() {
        final int x = readShort();
        final int y = readShort();
        return new Point(x, y);
    }

    public final byte[] read(final int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    public final long available() {
        return bs.available();
    }

    public final String toString() {
        return bs.toString();
    }

    public final String toString(final boolean b) {
        return bs.toString(b);
    }

    public final void seek(final long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            System.err.println("Seek failed" + e);
        }
    }

    public final long getPosition() {
        return bs.getPosition();
    }

    public final void skip(final int num) {
        seek(getPosition() + num);
    }
}
