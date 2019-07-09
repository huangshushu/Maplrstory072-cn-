package tools.data.output;

import java.awt.Point;

public interface LittleEndianWriter {
    public void writeZeroBytes(final int i);
    public void write(final int b);
    public void writeInt(final int i);
    public void writeShort(final int i);
    
    public void write(final byte b[]);
    public void write(final byte b);

    public void writeShort(final short s);
    
    public void writeLong(final long l);

    void writeAsciiString(final String s);
    void writeAsciiString(String s, final int max);
    void writePos(final Point s);
    void writeMapleAsciiString(final String s);
}
