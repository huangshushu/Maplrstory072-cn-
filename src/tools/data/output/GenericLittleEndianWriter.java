package tools.data.output;

import constants.ServerConfig;
import java.awt.Point;
import java.nio.charset.Charset;

public class GenericLittleEndianWriter implements LittleEndianWriter {
//  See http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
    private static final Charset ASCII = Charset.forName(ServerConfig.ASCII); // ISO-8859-1, UTF-8
    private ByteOutputStream bos;

    protected GenericLittleEndianWriter() {
    }

    protected void setByteOutputStream(final ByteOutputStream bos) {
        this.bos = bos;
    }

    public GenericLittleEndianWriter(final ByteOutputStream bos) {
        this.bos = bos;
    }

    @Override
    public final void writeZeroBytes(final int i) {
        for (int x = 0; x < i; x++) {
            bos.writeByte((byte) 0);
        }
    }

    @Override
    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            bos.writeByte(b[x]);
        }
    }

    @Override
    public final void write(final byte b) {
        bos.writeByte(b);
    }

    @Override
    public final void write(final int b) {
        bos.writeByte((byte) b);
    }

    @Override
    public final void writeShort(final short i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }

    @Override
    public final void writeShort(final int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }

    @Override
    public final void writeInt(final int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
        bos.writeByte((byte) ((i >>> 16) & 0xFF));
        bos.writeByte((byte) ((i >>> 24) & 0xFF));
    }

    @Override
    public final void writeAsciiString(final String s) {
        write(s.getBytes(ASCII));
    }

    @Override
    public final void writeAsciiString(String s, final int max) {
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.length(); i < max; i++) {
            write(0);
        }
    }

    @Override
    public final void writeMapleAsciiString(final String s) {
        writeShort((short) s.getBytes(ASCII).length);
        writeAsciiString(s);
    }


    @Override
    public final void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    @Override
    public final void writeLong(final long l) {
        bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) ((l >>> 8) & 0xFF));
        bos.writeByte((byte) ((l >>> 16) & 0xFF));
        bos.writeByte((byte) ((l >>> 24) & 0xFF));
        bos.writeByte((byte) ((l >>> 32) & 0xFF));
        bos.writeByte((byte) ((l >>> 40) & 0xFF));
        bos.writeByte((byte) ((l >>> 48) & 0xFF));
        bos.writeByte((byte) ((l >>> 56) & 0xFF));
    }
}
