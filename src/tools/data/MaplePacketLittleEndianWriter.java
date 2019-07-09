package tools.data;

import constants.ServerConfig;
import constants.ServerConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import tools.HexTool;

public class MaplePacketLittleEndianWriter {

    private final ByteArrayOutputStream baos;
    private static final Charset ASCII = Charset.forName(ServerConfig.ASCII); // ISO-8859-1, UTF-8

    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    public MaplePacketLittleEndianWriter(final int size) {
        this.baos = new ByteArrayOutputStream(size);
    }

    public final byte[] getPacket() {
        return baos.toByteArray();
    }

    public final String toString() {
        return HexTool.toString(baos.toByteArray());
    }

    public final void writeZeroBytes(final int i) {
        for (int x = 0; x < i; x++) {
            baos.write((byte) 0);
        }
    }

    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            baos.write(b[x]);
        }
    }

    public final void write(final byte b) {
        baos.write(b);
    }

    public final void write(final int b) {
        baos.write((byte) b);
    }

    public final void writeShort(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
    }

    public final void writeInt(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) ((i >>> 16) & 0xFF));
        baos.write((byte) ((i >>> 24) & 0xFF));
    }

    public final void writeAsciiString(final String s) {
        write(s.getBytes(ASCII));
    }

    public final void writeAsciiString(String s, final int max) {
        if (s.getBytes(ASCII).length > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.getBytes(ASCII).length; i < max; i++) {
            write(0);
        }
    }

    public final void writeMapleAsciiString(final String s) {
        writeShort((short) s.getBytes(ASCII).length);
        writeAsciiString(s);
    }

    public final void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    public final void writeRect(final Rectangle s) {
        writeInt(s.x);
        writeInt(s.y);
        writeInt(s.x + s.width);
        writeInt(s.y + s.height);
    }

    public final void writeLong(final long l) {
        baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
    }

    public final void writeBoolean(boolean b) {
        baos.write((byte) (b ? 1 : 0));
    }
}
