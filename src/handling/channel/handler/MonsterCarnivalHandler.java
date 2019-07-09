package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import java.util.List;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.packet.MonsterCarnivalPacket;

public class MonsterCarnivalHandler {

    public static final void MonsterCarnival(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().getCarnivalParty() == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final int tab = slea.readByte();
        final int num = slea.readInt();


        if (tab == 0) {
            final List<Pair<Integer, Integer>> mobs = c.getPlayer().getMap().getMobsToSpawn();
            if (num >= mobs.size() || c.getPlayer().getAvailableCP() < mobs.get(num).right) {
                c.getPlayer().dropMessage(5, "你没有 CP.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            final MapleMonster mons = MapleLifeFactory.getMonster(mobs.get(num).left);
            if (mons != null && c.getPlayer().getMap().makeCarnivalSpawn(c.getPlayer().getCarnivalParty().getTeam(), mons, num)) {
                c.getPlayer().getCarnivalParty().useCP(c.getPlayer(), mobs.get(num).right);
                c.getPlayer().CPUpdate(false, c.getPlayer().getAvailableCP(), c.getPlayer().getTotalCP(), 0);
                for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getPlayer().getCarnivalParty().getAvailableCP(), c.getPlayer().getCarnivalParty().getTotalCP(), c.getPlayer().getCarnivalParty().getTeam());
                }
                c.getPlayer().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getPlayer().getName(), tab, num));
                c.getSession().write(MaplePacketCreator.enableActions());
            } else {
                c.getPlayer().dropMessage(5, "你不能再召唤怪物.");
                c.getSession().write(MaplePacketCreator.enableActions());
            }

        } else if (tab == 1) { //debuff
	    final List<Integer> skillid = c.getPlayer().getMap().getSkillIds();
	    if (num >= skillid.size()) {
                c.getPlayer().dropMessage(5, "发生错误.");
                c.getSession().write(MaplePacketCreator.enableActions());
		return;
	    }
            final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(skillid.get(num)); //ugh wtf
            if (skil == null || c.getPlayer().getAvailableCP() < skil.cpLoss) {
                c.getPlayer().dropMessage(5, "你没有 CP.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            final MapleDisease dis = skil.getDisease();
            boolean found = false;
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (chr.getParty() == null || (c.getPlayer().getParty() != null && chr.getParty().getId() != c.getPlayer().getParty().getId())) {
                    if (skil.targetsAll || Randomizer.nextBoolean()) {
                        found = true;
                        if (dis == null) {
                            chr.dispel();
                        } else if (skil.getSkill() == null) {
                            chr.giveDebuff(dis, 1, 30000, dis.getDisease(), 1);
                        } else {
                            chr.giveDebuff(dis, skil.getSkill());
                        }
                        if (!skil.targetsAll) {
                            break;
                        }
                    }
                }
            }
            if (found) {
                c.getPlayer().getCarnivalParty().useCP(c.getPlayer(), skil.cpLoss);
                c.getPlayer().CPUpdate(false, c.getPlayer().getAvailableCP(), c.getPlayer().getTotalCP(), 0);
                for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getPlayer().getCarnivalParty().getAvailableCP(), c.getPlayer().getCarnivalParty().getTotalCP(), c.getPlayer().getCarnivalParty().getTeam());
                    //chr.dropMessage(5, "[" + (c.getPlayer().getCarnivalParty().getTeam() == 0 ? "Red" : "Blue") + "] " + c.getPlayer().getName() + " has used a skill. [" + dis.name() + "].");
                }
                c.getPlayer().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getPlayer().getName(), tab, num));
                c.getSession().write(MaplePacketCreator.enableActions());
            } else {
                c.getPlayer().dropMessage(5, "发生错误.");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else if (tab == 2) { //skill
            final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            if (skil == null || c.getPlayer().getAvailableCP() < skil.cpLoss) {
                c.getPlayer().dropMessage(5, "你没有 CP.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (c.getPlayer().getMap().makeCarnivalReactor(c.getPlayer().getCarnivalParty().getTeam(), num)) {
                c.getPlayer().getCarnivalParty().useCP(c.getPlayer(), skil.cpLoss);
                c.getPlayer().CPUpdate(false, c.getPlayer().getAvailableCP(), c.getPlayer().getTotalCP(), 0);
                for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getPlayer().getCarnivalParty().getAvailableCP(), c.getPlayer().getCarnivalParty().getTotalCP(), c.getPlayer().getCarnivalParty().getTeam());
                }
                c.getPlayer().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getPlayer().getName(), tab, num));
                c.getSession().write(MaplePacketCreator.enableActions());
            } else {
                c.getPlayer().dropMessage(5, "你不能再召唤存在.");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        }
    }
}
