package scripting;

import client.MapleClient;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {
    private final MaplePortal portal;

    public PortalPlayerInteraction(final MapleClient c, final MaplePortal portal) {
        super(c, portal.getId(), c.getPlayer().getMapId());
        this.portal = portal;
    }

    public final MaplePortal getPortal() {
        return portal;
    }

    public final void inFreeMarket() {
        if (getMapId() != 910000000) {
            if (getPlayer().getLevel() >= 10) {
                saveLocation("FREE_MARKET");
                playPortalSE();
                warp(910000000, "st00");
            } else {
                playerMessage(5, "你的等级必须超过10级才能进入自由市场。");
            }
        }
    }
    
    public final void inPVP() {
        if (getMapId() != 701000210) {
            if (getPlayer().getLevel() >= 15) {
                saveLocation("PVP");
                playPortalSE();
                warp(701000210, "out00");
            } else {
                playerMessage(5, "你的等级必须超过15级才能进入大擂台。");
            }
        }
    }
    
    public final void inArdentmill() {
        if (getMapId() != 910001000) {
            if (getPlayer().getLevel() >= 10) {
                saveLocation("ARDENTMILL");
                playPortalSE();
                warp(910001000, "st00");
            } else {
                playerMessage(5, "You must be level 15 in order to be able to enter the Crafting Town.");
            }
        }
    }

    // summon one monster on reactor location
    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, portal.getPosition());
    }

    // summon monsters on reactor location
    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, portal.getPosition());
    }
}
