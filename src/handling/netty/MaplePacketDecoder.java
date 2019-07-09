package handling.netty;

import client.MapleClient;
import handling.RecvPacketOpcode;
import handling.login.LoginServer;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.MapleCustomEncryption;
import tools.data.input.GenericLittleEndianAccessor;

public class MaplePacketDecoder extends ByteToMessageDecoder {

    @SuppressWarnings("deprecation")
    public static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.newInstance(MaplePacketDecoder.class.getName() + ".STATE");

    public static class DecoderState {

        public int packetlength = -1;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> message) throws Exception {
        final DecoderState decoderState = ctx.channel().attr(DECODER_STATE_KEY).get();
        final MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();

        if (decoderState.packetlength == -1) {
            if (in.readableBytes() >= 4) {
                final int packetHeader = in.readInt();
                if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
                    System.out.println("[警告] 封包可能不正确.");
                    ctx.channel().disconnect();
                    return;
                }
                decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
            } else {
                return;
            }
        }
        if (in.readableBytes() >= decoderState.packetlength) {
            final byte decryptedPacket[] = new byte[decoderState.packetlength];
            in.readBytes(decryptedPacket);
            decoderState.packetlength = -1;

            client.getReceiveCrypto().crypt(decryptedPacket);
            MapleCustomEncryption.decryptData(decryptedPacket);
            message.add(decryptedPacket);
            int packetLen = decryptedPacket.length;
            short pHeader = new GenericLittleEndianAccessor(new tools.data.input.ByteArrayByteStream(decryptedPacket)).readShort();
            String op = RecvPacketOpcode.nameOf(pHeader);
            if (LoginServer.isLogPackets() && !RecvPacketOpcode.isSpamHeader(RecvPacketOpcode.valueOf(op))) {
                String tab = "";
                for (int i = 4; i > op.length() / 8; i--) {
                    tab += "\t";
                }
                String t = packetLen >= 10 ? packetLen >= 100 ? packetLen >= 1000 ? "" : " " : "  " : "   ";
                final StringBuilder sb = new StringBuilder("[接收]\t" + op + tab + "\t包头:" + HexTool.getOpcodeToString(pHeader) + t + "[" + packetLen + "字节]");
                System.out.println(sb.toString());
                sb.append("\r\n\r\n").append(HexTool.toString(decryptedPacket)).append("\r\n").append(HexTool.toStringFromAscii(decryptedPacket));
                FileoutputUtil.log("logs/数据包_收发.txt", "\r\n\r\n" + sb.toString() + "\r\n\r\n");
            }
        }
    }
}
