package server.life;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import client.inventory.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import database.DBConPool;
import handling.channel.ChannelServer;
import handling.world.World;
import java.sql.SQLException;
import java.util.ArrayList;
import server.maps.*;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;

public class PlayerNPC extends MapleNPC {

    private Map<Byte, Integer> equips = new HashMap<Byte, Integer>();
    private int mapid, face, hair, charId;
    private byte skin, gender;
    private int[] pets = new int[3];

    public PlayerNPC(ResultSet rs) throws Exception {
        super(rs.getInt("ScriptId"), rs.getString("name"));
        hair = rs.getInt("hair");
        face = rs.getInt("face");
        mapid = rs.getInt("map");
        skin = rs.getByte("skin");
        charId = rs.getInt("charid");
        gender = rs.getByte("gender");
        setCoords(rs.getInt("x"), rs.getInt("y"), rs.getInt("dir"), rs.getInt("Foothold"));
        String[] pet = rs.getString("pets").split(",");
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null) {
                pets[i] = Integer.parseInt(pet[i]);
            } else {
                pets[i] = 0;
            }
        }

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE NpcId = ?");
            ps.setInt(1, getId());
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
            }
            rs2.close();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", ex);
        }
    }

    public PlayerNPC(MapleCharacter cid, int npc, MapleMap map, MapleCharacter base) {
        super(npc, cid.getName());
        this.charId = cid.getId();
        this.mapid = map.getId();
        setCoords(base.getTruePosition().x, base.getTruePosition().y, 0, base.getFH()); //0 = facing dir? no idea, but 1 dosnt work
        update(cid);
    }

    public void setCoords(int x, int y, int f, int fh) {
        setPosition(new Point(x, y));
        setCy(y);
        setRx0(x - 50);
        setRx1(x + 50);
        setF(f);
        setFh(fh);
    }

    public static void loadAll() {
        List<PlayerNPC> toAdd = new ArrayList<PlayerNPC>();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                toAdd.add(new PlayerNPC(rs));
            }
            rs.close();
            ps.close();
        } catch (Exception se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
        }
        for (PlayerNPC npc : toAdd) {
            npc.addToServer();
        }
    }

    public static void updateByCharId(MapleCharacter chr, Connection con) {
        if (World.Find.findChannel(chr.getId()) > 0) { //if character is in cserv
            for (PlayerNPC npc : ChannelServer.getInstance(World.Find.findChannel(chr.getId())).getAllPlayerNPC()) {
                npc.update(chr, con);
            }
        }
    }

    public void addToServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.addPlayerNPC(this);
        }
    }

    public void removeFromServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.removePlayerNPC(this);
        }
    }

    public void update(MapleCharacter chr) {
        update(chr, null);
    }

    public void update(MapleCharacter chr, Connection con) {
        if (chr == null || charId != chr.getId()) {
            return; //cant use name as it mightve been change actually..
        }
        setName(chr.getName());
        setHair(chr.getHair());
        setFace(chr.getFace());
        setSkin((byte) (chr.getSkinColor()));
        setGender(chr.getGender());
        setPets(chr.getPets());

        equips = new HashMap<>();
        for (Item item : chr.getInventory(MapleInventoryType.EQUIPPED).newList()) {
            if (item.getPosition() < -127) {
                continue;
            }
            equips.put((byte) item.getPosition(), item.getItemId());
        }
        if (con != null) {
            saveToDB(con);
        } else {
            saveToDB();
        }
    }

    public void destroy() {
        destroy(false); //just sql
    }

    public void destroy(boolean remove) {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM playernpcs WHERE scriptid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
            if (remove) {
                removeFromServer();
            }
        } catch (Exception se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
        }
    }

    public void saveToDB() {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            saveToDB(con);
        } catch (Exception se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
        }
    }

    public void saveToDB(Connection con) {
        try {
            if (getNPCFromWZ() == null) {
                destroy(true);
                return;
            }
            destroy();
            PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs(name, hair, face, skin, x, y, map, charid, scriptid, foothold, dir, gender, pets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, getName());
            ps.setInt(2, getHair());
            ps.setInt(3, getFace());
            ps.setInt(4, getSkin());
            ps.setInt(5, getTruePosition().x);
            ps.setInt(6, getTruePosition().y);
            ps.setInt(7, getMapId());
            ps.setInt(8, getCharId());
            ps.setInt(9, getId());
            ps.setInt(10, getFh());
            ps.setInt(11, getF());
            ps.setInt(12, getGender());
            String[] pet = {"0", "0", "0"};
            for (int i = 0; i < 3; i++) {
                if (pets[i] > 0) {
                    pet[i] = String.valueOf(pets[i]);
                }
            }
            ps.setString(13, pet[0] + "," + pet[1] + "," + pet[2]);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO playernpcs_equip(npcid, charid, equipid, equippos) VALUES (?, ?, ?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, getCharId());
            for (Entry<Byte, Integer> equip : equips.entrySet()) {
                ps.setInt(3, equip.getValue());
                ps.setInt(4, equip.getKey());
                ps.executeUpdate();
            }
            ps.close();
        } catch (Exception se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
        }
    }

    public Map<Byte, Integer> getEquips() {
        return equips;
    }

    public byte getSkin() {
        return skin;
    }

    public int getGender() {
        return gender;
    }

    public int getFace() {
        return face;
    }

    public int getHair() {
        return hair;
    }

    public int getCharId() {
        return charId;
    }

    public int getMapId() {
        return mapid;
    }

    public void setSkin(byte s) {
        this.skin = s;
    }

    public void setFace(int f) {
        this.face = f;
    }

    public void setHair(int h) {
        this.hair = h;
    }

    public void setGender(int g) {
        this.gender = (byte) g;
    }

    public int getPet(int i) {
        return pets[i] > 0 ? pets[i] : 0;
    }

    public void setPets(List<MaplePet> p) {
        for (int i = 0; i < 3; i++) {
            if (p != null && p.size() > i && p.get(i) != null) {
                this.pets[i] = p.get(i).getPetItemId();
            } else {
                this.pets[i] = 0;
            }
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnNPC(this, true));
        client.getSession().write(MaplePacketCreator.spawnPlayerNPC(this));
        client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, true));
    }

    public MapleNPC getNPCFromWZ() {
        MapleNPC npc = MapleLifeFactory.getNPC(getId());
        if (npc != null) {
            npc.setName(getName());
        }
        return npc;
    }
}
