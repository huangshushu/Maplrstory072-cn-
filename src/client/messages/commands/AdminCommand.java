package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ServerConstants.PlayerGMRank;
import database.DBConPool;
import handling.channel.ChannelServer;
import handling.world.World;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.MapleInventoryManipulator;
import server.ShutdownServer;
import server.Timer.EventTimer;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.CPUSampler;
import tools.LoadPacket;
import tools.packet.MaplePacketCreator;

public class AdminCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.ADMIN;
    }

    public static class StripEveryone extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ChannelServer cs = c.getChannelServer();
            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                if (mchr.isGM()) {
                    continue;
                }
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                List<Short> ids = new ArrayList<Short>();
                for (Item item : equipped.newList()) {
                    ids.add(item.getPosition());
                }
                for (short id : ids) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
                }
            }
            return 1;
        }
    }

    /*public static class 给所有人金币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(Integer.parseInt(splitted[1]), true);
                }
            }
            return 1;
        }
    }*/

    public static class 所有人金币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <金币量>");
                return 0;
            }
            int ret = 0;
            int gain = Integer.parseInt(splitted[1]);
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                mch.gainMeso(gain, true);
                ret++;
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    mch.startMapEffect("管理员发放" + gain + "冒险币给在线的所有玩家！祝您玩的开心玩的快乐", 5121009);
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("命令使用成功，当前共有: ").append(ret).append(" 个玩家获得: ").append(gain).append(" 冒险币 ").append(" 总计: ").append(ret * gain).toString());

            return 1;
        }
    }
    
    public static class 给所有人点卷 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            if (splitted.length > 2) {
                int type = Integer.parseInt(splitted[1]);
                int quantity = Integer.parseInt(splitted[2]);
                if (type == 1) {
                    type = 1;
                } else if (type == 2) {
                    type = 2;
                } else {
                    c.getPlayer().dropMessage(6, "用法: !给所有人点卷 [点卷类型1-2] [点卷数量][1是点卷.2是抵用卷]");
                    return 0;
                }
                if (quantity > 10000) {
                    quantity = 10000;
                }
                int ret = 0;
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                        mch.modifyCSPoints(type, quantity);
                        ret++;
                    }
                }
                String show = type == 1 ? "点卷" : "抵用卷";
                for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.startMapEffect("管理员发放" + quantity + show + "点卷给在线的所有玩家！祝您开心玩的快乐", 5121009);
                    }
                }
                c.getPlayer().dropMessage(6, new StringBuilder().append("命令使用成功，当前共有: ").append(ret).append(" 个玩家获得: ").append(quantity).append(" 点的").append(type == 1 ? "点券 " : " 抵用券 ").append(" 总计: ").append(ret * quantity).toString());
            } else {
                c.getPlayer().dropMessage(6, "用法: !给所有人点卷 [点卷类型1-2] [点卷数量][1是点卷.2是抵用卷]");
            }
            return 1;
        }
    }
    
    public static class 给所有人经验 extends CommandExecute {

        public int execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6,splitted[0] + " <经验量>");
                return 0;
            }
            int gain = Integer.parseInt(splitted[1]);
            int ret = 0;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainExp(gain, true, true, true);
                    ret++;
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    mch.startMapEffect("管理员发放" + gain + "经验给在线的所有玩家！祝您玩的开心玩的快乐", 5121009);
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("命令使用成功，当前共有: ").append(ret).append(" 个玩家获得: ").append(gain).append(" 点的").append(" 经验 ").append(" 总计: ").append(ret * gain).toString());

            return 1;
        }
    }

    public static class ExpRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                final int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setExpRate(rate);
                    }
                } else {
                    c.getChannelServer().setExpRate(rate);
                }
                c.getPlayer().dropMessage(6, "Exprate has been changed to " + rate + "x");
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !exprate <number> [all]");
            }
            return 1;
        }
    }

    public static class DropRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                final int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setDropRate(rate);
                    }
                } else {
                    c.getChannelServer().setDropRate(rate);
                }
                c.getPlayer().dropMessage(6, "Exprate has been changed to " + rate + "x");
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !droprate <number> [all]");
            }
            return 1;
        }
    }

    public static class MesoRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                final int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setMesoRate(rate);
                    }
                } else {
                    c.getChannelServer().setMesoRate(rate);
                }
                c.getPlayer().dropMessage(6, "Meso Rate has been changed to " + rate + "x");
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !mesorate <number> [all]");
            }
            return 1;
        }
    }

    public static class DCAll extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int range = -1;
            if (splitted[1].equals("m")) {
                range = 0;
            } else if (splitted[1].equals("c")) {
                range = 1;
            } else if (splitted[1].equals("w")) {
                range = 2;
            }
            if (range == -1) {
                range = 1;
            }
            if (range == 0) {
                c.getPlayer().getMap().disconnectAll();
            } else if (range == 1) {
                c.getChannelServer().getPlayerStorage().disconnectAll(true);
            } else if (range == 2) {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.getPlayerStorage().disconnectAll(true);
                }
            }
            return 1;
        }
    }

    public static class 关闭服务器 extends CommandExecute {

        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Shutting down...");
            if (t == null || !t.isAlive()) {
                t = new Thread(ShutdownServer.getInstance());
                ShutdownServer.getInstance().shutdown();
                t.start();
            } else {
                c.getPlayer().dropMessage(6, "A shutdown thread is already in progress or shutdown has not been done. Please wait.");
            }
            return 1;
        }
    }

    public static class 关闭服务器时间 extends 关闭服务器 {

        private static ScheduledFuture<?> ts = null;
        private int minutesLeft = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(0, splitted[0] + " <时间:分钟>");
                return 0;
            }
            minutesLeft = Integer.parseInt(splitted[1]);
            c.getPlayer().dropMessage(6, "服务器将在 " + minutesLeft + " 分钟后关闭");
            if (ts == null && (t == null || !t.isAlive())) {
                t = new Thread(ShutdownServer.getInstance());
                ts = EventTimer.getInstance().register(new Runnable() {

                    public void run() {
                        if (minutesLeft == 0) {
                            ShutdownServer.getInstance().shutdown();
                            t.start();
                            ts.cancel(false);
                            return;
                        }
                        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "服务器将在" + minutesLeft + "分钟后进行停机维护, 请及时安全下线, 以免造成不必要的损失."));
                        minutesLeft--;
                    }
                }, 60000);
            } else {
                c.getPlayer().dropMessage(6, "关闭进程正在进行或者关闭已完成, 请稍候.");
            }
            return 1;
        }
    }

    public static class StartProfiling extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            CPUSampler sampler = CPUSampler.getInstance();
            sampler.addIncluded("client");
            sampler.addIncluded("constants"); //or should we do Packages.constants etc.?
            sampler.addIncluded("database");
            sampler.addIncluded("handling");
            sampler.addIncluded("provider");
            sampler.addIncluded("scripting");
            sampler.addIncluded("server");
            sampler.addIncluded("tools");
            sampler.start();
            return 1;
        }
    }

    public static class StopProfiling extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            CPUSampler sampler = CPUSampler.getInstance();
            try {
                String filename = "odinprofile.txt";
                if (splitted.length > 1) {
                    filename = splitted[1];
                }
                File file = new File(filename);
                if (file.exists()) {
                    c.getPlayer().dropMessage(6, "The entered filename already exists, choose a different one");
                    return 0;
                }
                sampler.stop();
                FileWriter fw = new FileWriter(file);
                sampler.save(fw, 1, 10);
                fw.close();
            } catch (IOException e) {
                System.err.println("Error saving profile" + e);
            }
            sampler.reset();
            return 1;
        }
    }

    public static class 文件封包 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getSession().write(LoadPacket.getPacket());
            return 1;
        }
    }

    public static class 拉所有玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != c.getPlayer().getMapId()) {
                    mch.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                }
            }
            return 1;
        }
    }

    public static class 满技能 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            player.maxSkills();
            return 1;
        }
    }
    public static class 满属性 extends CommandExecute {

        public int execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            player.getStat().setMaxHp((short) 30000, player);
            player.getStat().setMaxMp((short) 30000, player);
            player.getStat().setStr(Short.MAX_VALUE, player);
            player.getStat().setDex(Short.MAX_VALUE, player);
            player.getStat().setInt(Short.MAX_VALUE, player);
            player.getStat().setLuk(Short.MAX_VALUE, player);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
            player.updateSingleStat(MapleStat.STR, Short.MAX_VALUE);
            player.updateSingleStat(MapleStat.DEX, Short.MAX_VALUE);
            player.updateSingleStat(MapleStat.INT, Short.MAX_VALUE);
            player.updateSingleStat(MapleStat.LUK, Short.MAX_VALUE);
            player.getStat().heal(player);
            return 1;
        }
    }
    
    public static class 设置永久NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "!设置永久NPC <npcid>");
                return 0;
            }
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                final int xpos = c.getPlayer().getPosition().x;
                final int ypos = c.getPlayer().getPosition().y;
                final int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos);
                npc.setRx1(xpos);
                npc.setFh(fh);
                npc.setCustom(true);
                try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO wz_customlife (dataid, f, hide, fh, cy, rx0, rx1, type, x, y, mid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, npcId);
                        ps.setInt(2, 0); // 1 = right , 0 = left
                        ps.setInt(3, 0); // 1 = hide, 0 = show
                        ps.setInt(4, fh);
                        ps.setInt(5, ypos);
                        ps.setInt(6, xpos);
                        ps.setInt(7, xpos);
                        ps.setString(8, "n");
                        ps.setInt(9, xpos);
                        ps.setInt(10, ypos);
                        ps.setInt(11, c.getPlayer().getMapId());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    System.err.println("Failed to save NPC to the database." + e);
                    c.getPlayer().dropMessage(6, "Failed to save NPC to the database.");
                }
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.getMapFactory().getMap(c.getPlayer().getMapId()).addMapObject(npc);
                    cserv.getMapFactory().getMap(c.getPlayer().getMapId()).broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
                }
                c.getPlayer().dropMessage(6, "Please do not reload this map or else the NPC will disappear untill the next restart.");
            } else {
                c.getPlayer().dropMessage(6, "查无此 Npc ");
                return 0;
            }
            return 1;
        }
    }
}
