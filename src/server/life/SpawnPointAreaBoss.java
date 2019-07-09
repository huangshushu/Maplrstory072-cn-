package server.life;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;
import server.maps.MapleMap;
import tools.packet.MaplePacketCreator;
import tools.Randomizer;

public class SpawnPointAreaBoss extends Spawns {
    private MapleMonsterStats monster;
    private Point pos1;
    private Point pos2;
    private Point pos3;
    private long nextPossibleSpawn;
    private int mobTime, fh, f, id;
    private AtomicBoolean spawned = new AtomicBoolean(false);
    private String msg;

    public SpawnPointAreaBoss(final MapleMonster monster, final Point pos1, final Point pos2, final Point pos3, final int mobTime, final String msg, final boolean shouldSpawn) {
        this.monster = monster.getStats();
        this.id = monster.getId();
	this.fh = monster.getFh();
	this.f = monster.getF();
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
        this.mobTime = (mobTime < 0 ? -1 : (mobTime * 1000));
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis() + (shouldSpawn ? 0 : this.mobTime);
    }

    public final int getF() {
	return f;
    }

    public final int getFh() {
	return fh;
    }

    @Override
    public final MapleMonsterStats getMonster() {
        return monster;
    }

    @Override
    public final byte getCarnivalTeam() {
        return -1;
    }

    @Override
    public final int getCarnivalId() {
        return -1;
    }

    @Override
    public final boolean shouldSpawn(long time) {
        if (mobTime < 0 || spawned.get()) {
            return false;
        }
        return nextPossibleSpawn <= time;
    }

    @Override
    public final Point getPosition() {
        final int rand = Randomizer.nextInt(3);
        return rand == 0 ? pos1 : rand == 1 ? pos2 : pos3;
    }

    @Override
    public final MapleMonster spawnMonster(final MapleMap map) {
	final Point pos = getPosition();
        final MapleMonster mob = new MapleMonster(id, monster);
        mob.setPosition(pos);
	mob.setCy(pos.y);
	mob.setRx0(pos.x - 50);
	mob.setRx1(pos.x + 50); //these dont matter for mobs
	mob.setFh(fh);
	mob.setF(f);
        spawned.set(true);
        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();
                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawned.set(false);
            }
        });
        map.spawnMonster(mob, -2);

        if (msg != null) {
            map.broadcastMessage(MaplePacketCreator.serverNotice(6, msg));
        }
        return mob;
    }

    @Override
    public final int getMobTime() {
        return mobTime;
    }
}
