package tools.data;

import java.io.IOException;
import tools.HexTool;

public class ByteArrayByteStream {
    private int pos = 0;
    private long bytesRead = 0;
    private final byte[] arr;

    public ByteArrayByteStream(final byte[] arr) {
        this.arr = arr;
    }
 
    public long getPosition() {
        return pos;
    }
    
    public void seek(final long offset) throws IOException {
        pos = (int) offset;
    }

    public long getBytesRead() {
        return bytesRead;
    }
    
    public int readByte() {
        bytesRead++;
        return (arr[pos++]) & 0xFF;
    }

    public String toString() {
        return toString(false);
    }

    
    public String toString(final boolean b) {
        String nows = "";
        if (arr.length - pos > 0) {
            byte[] now = new byte[arr.length - pos];
            System.arraycopy(arr, pos, now, 0, arr.length - pos);
            nows = HexTool.toString(now);
        }
        if (b) {
            return "All: " + HexTool.toString(arr) + "\nNow: " + nows;
        } else {
            return "Data: " + nows;
        }
    }
    
    public long available() {
        return arr.length - pos;
    }
}
