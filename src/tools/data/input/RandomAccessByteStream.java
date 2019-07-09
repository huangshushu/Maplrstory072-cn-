package tools.data.input;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessByteStream implements SeekableInputStreamBytestream {
    private final RandomAccessFile raf;
    private long read = 0;

    public RandomAccessByteStream(final RandomAccessFile raf) {
        super();
        this.raf = raf;
    }

    @Override
    public final int readByte() {
        int temp;
        try {
            temp = raf.read();
            if (temp == -1) {
                throw new RuntimeException("EOF");
            }
            read++;
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void seek(long offset) throws IOException {
        raf.seek(offset);
    }

    @Override
    public final long getPosition() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public final long getBytesRead() {
        return read;
    }

    @Override
    public final long available() {
        try {
            return raf.length() - raf.getFilePointer();
        } catch (IOException e) {
            System.err.println("ERROR" + e);
            return 0;
        }
    }

    @Override
    public final String toString(final boolean b) { //?
        return toString();
    }
}
