package scripting;

import client.MapleClient;
import database.DBConPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.FileoutputUtil;

public class ReactorScriptManager extends AbstractScriptManager {

    private static final ReactorScriptManager instance = new ReactorScriptManager();
    private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap<Integer, List<ReactorDropEntry>>();

    public static final ReactorScriptManager getInstance() {
        return instance;
    }

    public final void act(final MapleClient c, final MapleReactor reactor) {
        try {
            final Invocable iv = getInvocable("reactor/" + reactor.getReactorId() + ".js", c);
            if (iv == null) {
                return;
            }
            if (c.getPlayer().isAdmin()) {
                c.getPlayer().dropMessage(6, "执行反应堆ID：" + reactor.getReactorId());
            }
            final ScriptEngine scriptengine = (ScriptEngine) iv;
            ReactorActionManager rm = new ReactorActionManager(c, reactor);

            scriptengine.put("rm", rm);
            iv.invokeFunction("act");
        } catch (Exception e) {
            System.err.println("Error executing reactor script. ReactorID: " + reactor.getReactorId() + ", ReactorName: " + reactor.getName() + ":" + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing reactor script. ReactorID: " + reactor.getReactorId() + ", ReactorName: " + reactor.getName() + ":" + e);
        }
    }

    public final List<ReactorDropEntry> getDrops(final int rid) {
        List<ReactorDropEntry> ret = drops.get(rid);
        if (ret != null) {
            return ret;
        }
        ret = new LinkedList<ReactorDropEntry>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            ps = con.prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?");
            ps.setInt(1, rid);
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
            }
            rs.close();
            ps.close();
        } catch (final SQLException e) {
            System.err.println("Could not retrieve drops for reactor " + rid + e);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", e);
            return ret;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
                return ret;
            }
        }
        drops.put(rid, ret);
        return ret;
    }

    public final void clearDrops() {
        drops.clear();
    }
}
