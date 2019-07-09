package tools.data.output;

import io.netty.buffer.ByteBuf;

public class ByteBufferOutputstream implements ByteOutputStream {

    private final ByteBuf bb;

    public ByteBufferOutputstream(final ByteBuf bb) {
        super();
        this.bb = bb;
    }

    @Override
    public void writeByte(final byte b) {
        this.bb.writeByte(b);
    }
}
