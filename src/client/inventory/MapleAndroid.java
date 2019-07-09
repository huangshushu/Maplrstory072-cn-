package client.inventory;

;
import database.DBConPool;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Randomizer;



public class MapleAndroid implements Serializable {

    public static enum AndroidFlag {
        HAT(0x01),
        CAPE(0x02),
        FACE(0x04),
        OVERALL(0x08),
        TOP_PANTS(0x10), // wtf?not sure
        SHOES(0x20),
        GLOVES(0x40),
        DEFAULT(0x80); // not true but whatever

        private final int i;

        private AndroidFlag(int i) {
            this.i = i;
        }

        public final int getValue() {
            return i;
        }
    }

    private static final long serialVersionUID = 9179541993413738569L;
    private int stance = 0, uniqueid, itemid, skin, hair, face;
    private String name;
    private Point pos = new Point(0, 0);
    private boolean changed = false;

    private MapleAndroid(final int itemid, final int uniqueid) {
        this.itemid = itemid;
        this.uniqueid = uniqueid;
    }

    public static final MapleAndroid loadFromDb(final int itemid, final int uid) {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            final MapleAndroid ret = new MapleAndroid(itemid, uid);
            PreparedStatement ps = con.prepareStatement("SELECT * FROM androids WHERE uniqueid = ?"); // Get pet details..
            ps.setInt(1, uid);

            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            ret.setSkin(rs.getInt("skin"));
            ret.setHair(rs.getInt("hair"));
            ret.setFace(rs.getInt("face"));
            ret.setName(rs.getString("name"));
            ret.changed = false;

            rs.close();
            ps.close();

            return ret;
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
            return null;
        }
    }

    public final void saveToDb() {
        if (!changed) {
            return;
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE androids SET skin = ?, hair = ?, face = ?, name = ? WHERE uniqueid = ?");
            ps.setInt(1, skin);
            ps.setInt(2, hair);
            ps.setInt(3, face);
            ps.setString(4, name);
            ps.setInt(5, uniqueid); // Set ID
            ps.executeUpdate(); // Execute statement
            ps.close();
            changed = false;
        } catch (final SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
        }
    }

    public static final MapleAndroid create(final int itemid, final int uniqueid) {
        Pair<List<Integer>, List<Integer>> aInfo = MapleItemInformationProvider.getInstance().getAndroidInfo(itemid == 1662006 ? 5 : (itemid - 1661999));
        if (aInfo == null) {
            return null;
        }
        return create(itemid, uniqueid, 0, aInfo.left.get(Randomizer.nextInt(aInfo.left.size())), aInfo.right.get(Randomizer.nextInt(aInfo.right.size())));
    }

    public static final MapleAndroid create(int itemid, int uniqueid, int skin, int hair, int face) {
        if (uniqueid <= -1) { //wah
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement pse = con.prepareStatement("INSERT INTO androids (uniqueid, skin, hair, face, name) VALUES (?, ?, ?, ?, ?)");
            pse.setInt(1, uniqueid);
            pse.setInt(2, skin);
            pse.setInt(3, hair);
            pse.setInt(4, face);
            pse.setString(5, "Android");
            pse.executeUpdate();
            pse.close();
        } catch (final SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
            return null;
        }
        final MapleAndroid and = new MapleAndroid(itemid, uniqueid);
        and.setSkin(skin);
        and.setHair(hair);
        and.setFace(face);
        and.setName("Android");

        return and;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public final void setHair(final int closeness) {
        this.hair = closeness;
        this.changed = true;
    }

    public final int getHair() {
        return hair;
    }

    public final void setFace(final int closeness) {
        this.face = closeness;
        this.changed = true;
    }

    public final int getFace() {
        return face;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
        this.changed = true;
    }

    public final void setSkin(final int s) {
        this.skin = s;
        this.changed = true;
    }

    public final int getSkin() {
        return skin;
    }

    public final Point getPos() {
        return pos;
    }

    public final void setPos(final Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return stance;
    }

    public final void setStance(final int stance) {
        this.stance = stance;
    }

    public final int getItemId() {
        return itemid;
    }

    public final int getType() {
        switch (itemid) {
            case 1662006: { // Princessoid
                return 5;
            }
        }
        return itemid - 1661999;
    }

    public final void updatePosition(final List<LifeMovementFragment> movement) {
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
