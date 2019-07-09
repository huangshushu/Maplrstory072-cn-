package tools.data.input;

import java.io.IOException;

public interface SeekableInputStreamBytestream extends ByteInputStream {
    void seek(long offset) throws IOException;
    long getPosition() throws IOException;
    String toString(final boolean b);
}
