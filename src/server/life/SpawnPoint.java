package server.life;

import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ServerConfig;
import java.awt.Point;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleStatEffect;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import tools.packet.MaplePacketCreator;

public class SpawnPoint extends Spawns {

    private MapleMonsterStats monster;
    private Point pos;
    private long nextPossibleSpawn;
    private int mobTime, carnival = -1, fh, f, id, level = -1;
    private AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private String msg;
    private byte carnivalTeam;

    public SpawnPoint(final MapleMonster monster, final Point pos, final int mobTime, final byte carnivalTeam, final String msg) {
        this.monster = monster.getStats();
        this.pos = pos;
        this.id = monster.getId();
        this.fh = monster.getFh();
        this.f = monster.getF();
        this.mobTime = (mobTime < 0 ? -1 : (mobTime * 1000));
        this.carnivalTeam = carnivalTeam;
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public final void setCarnival(int c) {
        this.carnival = c;
    }

    public final void setLevel(int c) {
        this.level = c;
    }

    public final int getF() {
        return f;
    }

    public final int getFh() {
        return fh;
    }

    @Override
    public final Point getPosition() {
        return pos;
    }

    @Override
    public final MapleMonsterStats getMonster() {
        return monster;
    }

    @Override
    public final byte getCarnivalTeam() {
        return carnivalTeam;
    }

    @Override
    public final int getCarnivalId() {
        return carnival;
    }

    @Override
    public final boolean shouldSpawn(long time) {
        if (mobTime < 0) {
            return false;
        }
        // regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
        // maximum of 1
        if (((mobTime != 0 || !monster.getMobile()) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1 && !ServerConfig.MoonlightRevamp || spawnedMonsters.get() > ServerConfig.monsterSpawn && ServerConfig.MoonlightRevamp) {
            return false;
        }
        return nextPossibleSpawn <= time;
    }

    @Override
    public final MapleMonster spawnMonster(final MapleMap map) {
        final MapleMonster mob = new MapleMonster(id, monster);
        mob.setPosition(pos);
        mob.setCy(pos.y);
        mob.setRx0(pos.x - 50);
        mob.setRx1(pos.x + 50); //these dont matter for mobs
        mob.setFh(fh);
        mob.setF(f);
        mob.setCarnivalTeam(carnivalTeam);
        if (level > -1) {
            mob.changeLevel(level);
        }
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();
                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        map.spawnMonster(mob, -2);
        if (carnivalTeam > -1) {
            for (MapleReactor r : map.getAllReactorsThreadsafe()) { //parsing through everytime a monster is spawned? not good idea
                if (r.getName().startsWith(String.valueOf(carnivalTeam)) && r.getReactorId() == (9980000 + carnivalTeam) && r.getState() < 5) {
                    final int num = Integer.parseInt(r.getName().substring(1, 2)); //00, 01, etc
                    final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
                    if (skil != null) {
                        skil.getSkill().applyEffect(null, mob, false);
                    }
                }
            }
        }
        for (MapleSummon s : map.getAllSummonsThreadsafe()) {
            if (s.getSkill() == 35111005) {
                final MapleStatEffect effect = SkillFactory.getSkill(s.getSkill()).getEffect(s.getSkillLevel());
                for (Map.Entry<MonsterStatus, Integer> stat : effect.getMonsterStati().entrySet()) {
                    mob.applyStatus(s.getOwner(), new MonsterStatusEffect(stat.getKey(), stat.getValue(), s.getSkill(), null, false), false, effect.getDuration(), true, effect);
                }
                break;
            }
        }
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
