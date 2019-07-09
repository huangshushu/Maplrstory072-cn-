/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import tools.data.MaplePacketLittleEndianWriter;

/**
 *
 * @author wubin
 */
public class LoadPacket {

    public static byte[] getPacket() {
        Properties packetProps = new Properties();
        InputStreamReader is;
        try {
            is = new FileReader("文件封包.txt");
            packetProps.load(is);
            is.close();
        } catch (IOException ex) {
            System.out.println("读取 文件封包.txt 失败" + ex);
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packetProps.getProperty("packet")));
        return mplew.getPacket();
    }
}
