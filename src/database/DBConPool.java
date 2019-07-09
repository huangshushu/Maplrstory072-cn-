/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.alibaba.druid.pool.DruidDataSource;
import constants.ServerConfig;

/**
 *
 * @author wubin
 */
public class DBConPool {

    private static DruidDataSource dataSource = null;
    public static final int RETURN_GENERATED_KEYS = 1;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("[数据库信息] 找不到JDBC驱动.");
            System.exit(0);
        }
    }

    private static class InstanceHolder {

        public static final DBConPool instance = new DBConPool();
    }

    public static DBConPool getInstance() {
        return InstanceHolder.instance;
    }

    private DBConPool() {
    }

    public DruidDataSource getDataSource() {
        if (dataSource == null) {
            InitDBConPool();
        }
        return dataSource;
    }

    private void InitDBConPool() {
        dataSource = new DruidDataSource();
        dataSource.setName("mysql_pool");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:" + ServerConfig.SQL_PORT + "/" + ServerConfig.SQL_DATABASE + "?useUnicode=true&characterEncoding=GBK");
        dataSource.setUsername(ServerConfig.SQL_USER);
        dataSource.setPassword(ServerConfig.SQL_PASSWORD);
        dataSource.setInitialSize(300);
        dataSource.setMinIdle(500);
        dataSource.setMaxActive(6000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 'x'");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setMaxWait(60000);
        dataSource.setUseUnfairLock(true);
    }
}
