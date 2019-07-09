package handling.netty;

import client.MapleClient;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.concurrent.locks.Lock;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.MapleCustomEncryption;
import tools.StringUtil;

public class MaplePacketEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf buffer) throws Exception {
        final MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();

        if (client != null) {
            final MapleAESOFB send_crypto = client.getSendCrypto();

            final byte[] inputInitialPacket = ((byte[]) message);

            int pHeader = ((inputInitialPacket[0]) & 0xFF) + (((inputInitialPacket[1]) & 0xFF) << 8);
            String op = SendPacketOpcode.nameOf(pHeader);
            if (LoginServer.isLogPackets() && !SendPacketOpcode.isSpamHeader(SendPacketOpcode.valueOf(op))) {
                int packetLen = inputInitialPacket.length;
                String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                pHeaderStr = "0x" + StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
                String tab = "";
                for (int i = 4; i > op.length() / 8; i--) {
                    tab += "\t";
                }
                String t = packetLen >= 10 ? packetLen >= 100 ? packetLen >= 1000 ? "" : " " : "  " : "   ";
                final StringBuilder sb = new StringBuilder("[发送]\t" + op + tab + "\t包头:" + pHeaderStr + t + "[" + packetLen/* + "\r\nCaller: " + Thread.currentThread().getStackTrace()[2] */ + "字节]");
                System.out.println(sb.toString());
                sb.append("\r\n\r\n").append(HexTool.toString((byte[]) message)).append("\r\n").append(HexTool.toStringFromAscii((byte[]) message));
                FileoutputUtil.log("logs/数据包_收发.txt", "\r\n\r\n" + sb.toString() + "\r\n\r\n");
            }

            final byte[] unencrypted = new byte[inputInitialPacket.length];
            System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length); // Copy the input > "unencrypted"
            final byte[] ret = new byte[unencrypted.length + 4]; // Create new bytes with length = "unencrypted" + 4

            final Lock mutex = client.getLock();
            mutex.lock();
            try {
                final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
                MapleCustomEncryption.encryptData(unencrypted); // Encrypting Data
                send_crypto.crypt(unencrypted); // Crypt it with IV
                System.arraycopy(header, 0, ret, 0, 4); // Copy the header > "Ret", first 4 bytes
                System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length); // Copy the unencrypted > "ret"
                buffer.writeBytes(ret);
            } finally {
                mutex.unlock();
            }
        } else { // no client object created yet, send unencrypted (hello)
            buffer.writeBytes((byte[]) message);
        }
    }
}
