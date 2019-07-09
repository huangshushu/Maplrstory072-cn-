package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import server.MaplePortal;
import tools.packet.MaplePacketCreator;

public class MapleDoor extends MapleMapObject {
    private WeakReference<MapleCharacter> owner;
    private MapleMap town;
    private MaplePortal townPortal;
    private MapleMap target;
    private int skillId, ownerId;
    private Point targetPosition;
    public boolean first = true;

    public MapleDoor(final MapleCharacter owner, final Point targetPosition, final int skillId) {
        super();
        this.owner = new WeakReference<MapleCharacter>(owner);
        this.ownerId = owner.getId();
        this.target = owner.getMap();
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = this.target.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
    }

    public MapleDoor(final MapleDoor origDoor) {
        super();
        this.owner = new WeakReference<MapleCharacter>(origDoor.owner.get());
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = new Point(origDoor.targetPosition);
        this.skillId = origDoor.skillId;
        this.ownerId = origDoor.ownerId;
        setPosition(townPortal.getPosition());
    }

    public final int getSkill() {
        return skillId;
    }

    public final int getOwnerId() {
        return ownerId;
    }

    private final MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<MaplePortal>();

        for (final MaplePortal port : town.getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        Collections.sort(freePortals, new Comparator<MaplePortal>() {

            @Override
            public final int compare(final MaplePortal o1, final MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                } else if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (final MapleMapObject obj : town.getAllDoorsThreadsafe()) {
            final MapleDoor door = (MapleDoor) obj;

            if (door.getOwner() != null && door.getOwner().getParty() != null && getOwner() != null && getOwner().getParty() != null && getOwner().getParty().getId() == door.getOwner().getParty().getId()) {
               return null; //one per
            }
	    freePortals.remove(door.getTownPortal());
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return freePortals.iterator().next();
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
	if (getOwner() == null || target == null || client.getPlayer() == null) {
	    return;
	}
        if (target.getId() == client.getPlayer().getMapId() || getOwnerId() == client.getPlayer().getId() || (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
            client.getSession().write(MaplePacketCreator.spawnDoor(getOwnerId(), target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition(), target.getId() == client.getPlayer().getMapId() ? first : false)); //spawnDoor always has same position.
            if (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && (getOwnerId() == client.getPlayer().getId() || getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
                client.getSession().write(MaplePacketCreator.partyPortal(town.getId(), target.getId(), skillId, target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition(), first));
            }
            client.getSession().write(MaplePacketCreator.spawnPortal(town.getId(), target.getId(), skillId, target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition()));
        }
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
	if (client.getPlayer() == null || getOwner() == null || target == null) {
	    return;
	}
        if (target.getId() == client.getPlayer().getMapId() || getOwnerId() == client.getPlayer().getId() || (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
            client.getSession().write(MaplePacketCreator.removeDoor(getOwnerId(),false));
            if (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && (getOwnerId() == client.getPlayer().getId() || getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
                client.getSession().write(MaplePacketCreator.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
            }
	    client.getSession().write(MaplePacketCreator.spawnPortal(999999999, 999999999, 0, null));
        }
    }

    public final void warp(final MapleCharacter chr, final boolean toTown) {
        if (chr.getId() == getOwnerId() || (getOwner() != null && getOwner().getParty() != null && chr.getParty() != null && getOwner().getParty().getId() == chr.getParty().getId())) {
            if (!toTown) {
		chr.changeMap(target, target.findClosestPortal(targetPosition));
            } else {
                chr.changeMap(town, townPortal);
            }
        } else {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public final MapleCharacter getOwner() {
        return owner.get();
    }

    public final MapleMap getTown() {
        return town;
    }

    public final MaplePortal getTownPortal() {
        return townPortal;
    }

    public final MapleMap getTarget() {
        return target;
    }

    public final Point getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
