package server;

import client.SkillFactory;
import client.inventory.MapleInventoryIdentifier;
import constants.BeansConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.guild.MapleGuild;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Timer.BuffTimer;
import server.Timer.CheatTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.FileoutputUtil;

public class Start {

    public static long startTime = System.currentTimeMillis();
    public static final Start instance = new Start();

    public void run() throws InterruptedException {
        long start = System.currentTimeMillis();
        try (PreparedStatement ps = DBConPool.getInstance().getDataSource().getConnection().prepareStatement("UPDATE accounts SET loggedin = 0")) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
            throw new RuntimeException("执行中出现异常 - 无法连线到数据库.");
        }

        System.out.println("正在载入 " + ServerProperties.getProperty("login.serverName"));
        World.init();
        System.out.println("主机位置: " + ServerConfig.interface_ + ":" + LoginServer.PORT);
        System.out.println("客户端版本: " + ServerConfig.MAPLE_VERSION + "." + ServerConfig.MAPLE_PATCH);
        System.out.println("正在加载线程...");
        WorldTimer.getInstance().start();
        EtcTimer.getInstance().start();
        MapTimer.getInstance().start();
        CloneTimer.getInstance().start();
        EventTimer.getInstance().start();
        BuffTimer.getInstance().start();
        PingTimer.getInstance().start();
        MapleGuildRanking.getInstance().load();
        MapleGuild.loadAll(); //(this);
        MapleFamily.loadAll(); //(this);
        MapleLifeFactory.loadQuestCounts();
        MapleQuest.initQuests();
        MapleItemInformationProvider.getInstance().runEtc();
        MapleMonsterInformationProvider.getInstance().load();
        MapleItemInformationProvider.getInstance().runItems();
        try {
            SkillFactory.load();
        } catch (Exception e) {
            System.out.println(e);
        }
        LoginInformationProvider.getInstance();
        //BeansConstants.getInstance();
        RandomRewards.load();
        MapleOxQuizFactory.getInstance();
        MapleCarnivalFactory.getInstance();
        MobSkillFactory.getInstance();
        SpeedRunner.loadSpeedRuns();
        //System.out.println("Loading MTSStorage :::");
        //MTSStorage.load();
        MapleInventoryIdentifier.getInstance();
        CashItemFactory.getInstance().initialize();
        LoginServer.run_startup_configurations();
        ChannelServer.startChannel_Main();
        CashShopServer.run_startup_configurations();
        CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000);
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        World.registerRespawn();
        ShutdownServer.registerMBean();
        PlayerNPC.loadAll();// touch - so we see database problems early...
        MapleMonsterInformationProvider.getInstance().addExtra();
        LoginServer.setOn(); //now or later
        MapleMapFactory.loadCustomLife();
        RankingWorker.run();
        if (Boolean.parseBoolean(ServerProperties.getProperty("world.admin")) || ServerConstants.Use_Localhost) {
            System.out.println("管理员模式已开启");
        }

        if (Boolean.parseBoolean(ServerProperties.getProperty("world.logpackets"))) {
            System.out.println("数据包记录已开启.");
        }
        if (ServerConfig.USE_FIXED_IV) {
            System.out.println("反抓包已开启.");
        }

        long now = System.currentTimeMillis() - start;
        long seconds = now / 1000;
        long ms = now % 1000;
        System.out.println("总加载时间: " + seconds + "秒 " + ms + "毫秒");
    }

    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
            ShutdownServer.getInstance().run();
        }
    }

    public static void main(final String args[]) throws InterruptedException {
        instance.run();
    }
}
