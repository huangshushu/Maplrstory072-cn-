package server.life;

import database.DBConPool;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.FileoutputUtil;
import tools.Pair;

public class MobSkillFactory {

    private final Map<Pair<Integer, Integer>, MobSkill> mobSkillCache = new HashMap<Pair<Integer, Integer>, MobSkill>();
    private static final MobSkillFactory instance = new MobSkillFactory();

    public MobSkillFactory() {
        initialize();
    }

    public static MobSkillFactory getInstance() {
        return instance;
    }

    public static MobSkill getMobSkill(int skillId, int level) {
        return instance.mobSkillCache.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
    }

    private void initialize() {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wz_mobskilldata");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mobSkillCache.put(new Pair<Integer, Integer>(rs.getInt("skillid"), rs.getInt("level")), get(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", e);
        }
    }

    private MobSkill get(ResultSet rs) throws SQLException {
        List<Integer> toSummon = new ArrayList<Integer>();
        final String[] summs = rs.getString("summons").split(", ");
        if (summs.length <= 0 && rs.getString("summons").length() > 0) {
            toSummon.add(Integer.parseInt(rs.getString("summons")));
        }
        for (String s : summs) {
            if (s.length() > 0) {
                toSummon.add(Integer.parseInt(s));
            }
        }
        Point lt = null;
        Point rb = null;
        //make sure the points exist before adding it
        if (rs.getInt("ltx") != 0 || rs.getInt("lty") != 0 || rs.getInt("rbx") != 0 || rs.getInt("rby") != 0) {
            lt = new Point(rs.getInt("ltx"), rs.getInt("lty"));
            rb = new Point(rs.getInt("rbx"), rs.getInt("rby"));
        }
        final MobSkill ret = new MobSkill(rs.getInt("skillid"), rs.getInt("level"));
        ret.addSummons(toSummon);
        ret.setCoolTime(rs.getInt("interval") * 1000);
        ret.setDuration(rs.getInt("time") * 1000);
        ret.setHp(rs.getInt("hp"));
        ret.setMpCon(rs.getInt("mpcon"));
        ret.setSpawnEffect(rs.getInt("spawneffect"));
        ret.setX(rs.getInt("x"));
        ret.setY(rs.getInt("y"));
        ret.setProp(rs.getInt("prop") / 100f);
        ret.setLimit((short) rs.getInt("limit"));
        ret.setOnce(rs.getByte("once") > 0);
        ret.setLtRb(lt, rb);
        return ret;
    }
}
