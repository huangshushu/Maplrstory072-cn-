package tools.data.input;

import java.awt.Point;

public interface LittleEndianAccessor {

    byte readByte();

    int readByteAsInt();

    char readChar();

    short readShort();

    int readInt();

    long readLong();

    void skip(int num);

    byte[] read(int num);

    float readFloat();

    double readDouble();

    String readAsciiString(int n);

    String readMapleAsciiString();

    String readNullTerminatedAsciiString();

    Point readPos();

    long getBytesRead();

    long available();

    String toString(final boolean b);
}
