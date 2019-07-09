/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

import database.DBConPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.FileoutputUtil;

/**
 *
 * @author wubin
 */
public class DumpMobDropQuests {

    public static void main(String[] args) throws SQLException {

        deleteWhereCharacterId("DELETE FROM drop_data WHERE questid = ?", 0);

    }

    public static void deleteWhereCharacterId(String sql, int questid) throws SQLException {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, questid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
        }
    }

}
