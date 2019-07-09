package server.maps;

import client.MapleClient;
import tools.packet.MaplePacketCreator;
import tools.packet.CSPacket;

public class MapleMapEffect {
    private String msg = "";
    private int itemId = 0;
    private boolean active = true;
    private boolean jukebox = false;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setJukebox(boolean actie) {
        this.jukebox = actie;
    }

    public boolean isJukebox() {
        return this.jukebox;
    }

    public byte[] makeDestroyData() { //jukebox doesn't REALLY have a destroy, but 0 stops all music
        return jukebox ? CSPacket.playCashSong(0, "") : MaplePacketCreator.removeMapEffect();
    }

    public byte[] makeStartData() {
        return jukebox ? CSPacket.playCashSong(itemId, msg) : MaplePacketCreator.startMapEffect(msg, itemId, active);
    }

    public void sendStartData(MapleClient c) {
        c.getSession().write(makeStartData());
    }
}
