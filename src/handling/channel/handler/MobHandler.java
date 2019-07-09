package handling.channel.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MonsterFamiliar;
import client.SkillFactory;
import client.SkillFactory.FamiliarEntry;
import client.anticheat.CheatingOffense;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.world.World;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.StructFamiliar;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleNodes.MapleNodeInfo;
import server.movement.AbsoluteLifeMovement;
import server.movement.AbstractLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.MobPacket;

public class MobHandler {

    public static final void MoveMonster(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        try {
            if (chr == null || chr.getMap() == null) {
                return;
            }
            final int oid = slea.readInt();
            final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

            if (monster == null) { // movin something which is not a monster
                return;
            }
            if (monster.getLinkCID() > 0) {
                return;
            }
            final short moveid = slea.readShort();
            final boolean useSkill = slea.readByte() > 0;
            final byte skill = slea.readByte();
            final int unk = slea.readInt();
            int realskill = 0;
            int level = 0;

            if (useSkill) {// && (skill == -1 || skill == 0)) {
                final byte size = monster.getNoSkills();
                boolean used = false;

                if (size > 0) {
                    final Pair<Integer, Integer> skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
                    realskill = skillToUse.getLeft();
                    level = skillToUse.getRight();
                    final MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);

                    if (mobSkill != null && !mobSkill.checkCurrentBuff(chr, monster)) {
                        final long now = System.currentTimeMillis();
                        final long ls = monster.getLastSkillUsed(realskill);

                        if (ls == 0 || (((now - ls) > mobSkill.getCoolTime()) && !mobSkill.onlyOnce())) {
                            monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

                            final int reqHp = (int) (((float) monster.getHp() / monster.getMobMaxHp()) * 100); // In case this monster have 2.1b and above HP
                            if (reqHp <= mobSkill.getHP()) {
                                used = true;
                                mobSkill.applyEffect(chr, monster, true);
                            }
                        }
                    }
                }
                if (!used) {
                    realskill = 0;
                    level = 0;
                }
            }
            slea.skip(1);
            slea.skip(4); // sometimes 0, 1
            final Point startPos = slea.readPos();
            List<LifeMovementFragment> res = null;
            try {
                res = MovementParse.parseMovement(slea, 2, startPos);
            } catch (ArrayIndexOutOfBoundsException e) {
                FileoutputUtil.outputFileError(FileoutputUtil.Movement_Log, e);
                FileoutputUtil.log(FileoutputUtil.Movement_Log, "MOBID " + monster.getId() + ", AIOBE Type2:\n" + slea.toString(true));
                return;
            }

            try {
                CheckMobVac(c, monster, res, startPos);
            } catch (Exception ex) {
            }

            if (monster.getController() != c.getPlayer()) {
                if (monster.isAttackedBy(c.getPlayer())) {// aggro and controller change
                    monster.switchController(c.getPlayer(), true);
                } else {
                    return;
                }
            } else if (skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isFirstAttack()) {
                monster.setControllerHasAggro(false);
                monster.setControllerKnowsAboutAggro(false);
            }
            boolean aggro = monster.isControllerHasAggro();
            if (aggro) {
                monster.setControllerKnowsAboutAggro(true);
            }

            if (res != null && chr != null && res.size() > 0) {
                final MapleMap map = chr.getMap();
                for (final LifeMovementFragment move : res) {
                    if (move instanceof AbsoluteLifeMovement) {
                        final Point endPos = ((LifeMovement) move).getPosition();
                        if (endPos.x < (map.getLeft() - 250) || endPos.y < (map.getTop() - 250) || endPos.x > (map.getRight() + 250) || endPos.y > (map.getBottom() + 250)) { //experimental
                            chr.getCheatTracker().checkMoveMonster(endPos);
                            return;
                        }
                    }
                }
                c.getSession().write(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level));

                if (slea.available() != 8) {
                    System.out.println("slea.available != 8 (怪物移动错误) 剩余封包长度: " + slea.available());
                    FileoutputUtil.log("logs\\移动封包出错\\怪物移动出错.log", "slea.available != 8 (怪物移动错误)\r\n怪物ID: " + monster.getId() + "\r\n" + slea.toString(true));
                    return;
                }
                MovementParse.updatePosition(res, monster, -1);
                final Point endPos = monster.getTruePosition();
                map.moveMonster(monster, endPos);
                map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, skill, unk, monster.getObjectId(), startPos, res), endPos);
                chr.getCheatTracker().checkMoveMonster(endPos);
            }
        } catch (Exception ex) {
            FileoutputUtil.log("logs/怪物移动出错.txt", "\r\n\r\n" + ex + "\r\n\r\n");
        }
    }

    public static final void FriendlyDamage(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        slea.skip(4); // Player ID
        final MapleMonster mobto = map.getMonsterByOid(slea.readInt());

        if (mobfrom != null && mobto != null && mobfrom != mobto && mobto.getStats().isFriendly()) {
            final int damage = (mobto.getStats().getLevel() * Randomizer.nextInt(mobto.getStats().getLevel())) / 2; // Temp for now until I figure out something more effective
            mobto.damage(chr, damage, true);
            checkShammos(chr, mobto, map);
        }
    }

    public static final void MobBomb(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        slea.skip(4); // something, 9E 07
        slea.readInt(); //-204?

        if (mobfrom != null && mobfrom.getBuff(MonsterStatus.MONSTER_BOMB) != null) {

        }
    }

    public static final void checkShammos(final MapleCharacter chr, final MapleMonster mobto, final MapleMap map) {
        if (!mobto.isAlive() && mobto.getStats().isEscort()) { //shammos
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) { //check for 2022698
                if (chrz.getParty() != null && chrz.getParty().getLeader().getId() == chrz.getId()) {
                    //leader
                    if (chrz.haveItem(2022698)) {
                        MapleInventoryManipulator.removeById(chrz.getClient(), MapleInventoryType.USE, 2022698, 1, false, true);
                        mobto.heal((int) mobto.getMobMaxHp(), mobto.getMobMaxMp(), true);
                        return;
                    }
                    break;
                }
            }
            map.broadcastMessage(MaplePacketCreator.serverNotice(6, "Your party has failed to protect the monster."));
            final MapleMap mapp = chr.getMap().getForcedReturnMap();
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) {
                chrz.changeMap(mapp, mapp.getPortal(0));
            }
        } else if (mobto.getStats().isEscort() && mobto.getEventInstance() != null) {
            mobto.getEventInstance().setProperty("HP", String.valueOf(mobto.getHp()));
        }
    }

    public static final void MonsterBomb(final int oid, final MapleCharacter chr) {
        final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

        if (monster == null || !chr.isAlive() || chr.isHidden() || monster.getLinkCID() > 0) {
            return;
        }
        final byte selfd = monster.getStats().getSelfD();
        if (selfd != -1) {
            chr.getMap().killMonster(monster, chr, false, false, selfd);
        }
    }

    public static final void AutoAggro(final int monsteroid, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.isHidden()) { //no evidence :)
            return;
        }
        final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);

        if (monster != null && chr.getTruePosition().distanceSq(monster.getTruePosition()) < 200000 && monster.getLinkCID() <= 0) {
            if (monster.getController() != null) {
                if (chr.getMap().getCharacterById(monster.getController().getId()) == null) {
                    monster.switchController(chr, true);
                } else {
                    monster.switchController(monster.getController(), true);
                }
            } else {
                monster.switchController(chr, true);
            }
        }
    }

    public static final void HypnotizeDmg(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        slea.skip(4); // Player ID
        final int to = slea.readInt(); // mobto
        slea.skip(1); // Same as player damage, -1 = bump, integer = skill ID
        final int damage = slea.readInt();
//	slea.skip(1); // Facing direction
//	slea.skip(4); // Some type of pos, damage display, I think

        final MapleMonster mob_to = chr.getMap().getMonsterByOid(to);

        if (mob_from != null && mob_to != null && mob_to.getStats().isFriendly()) { //temp for now
            if (damage > 30000) {
                return;
            }
            mob_to.damage(chr, damage, true);
            checkShammos(chr, mob_to, chr.getMap());
        }
    }

    public static final void DisplayNode(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        if (mob_from != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getNodeProperties(mob_from, chr.getMap()));
        }
    }

    public static final void MobNode(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        final int newNode = slea.readInt();
        final int nodeSize = chr.getMap().getNodes().size();
        if (mob_from != null && nodeSize > 0) {
            final MapleNodeInfo mni = chr.getMap().getNode(newNode);
            if (mni == null) {
                return;
            }
            if (mni.attr == 2) { //talk
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120035, mob_from.getObjectId()); //temporary for now. itemID is located in WZ file
                        break;
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120051, mob_from.getObjectId()); //temporary for now. itemID is located in WZ file
                        break;
                }
            }
            mob_from.setLastNode(newNode);
            if (chr.getMap().isLastNode(newNode)) { //the last node on the map.
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "Proceed to the next stage."));
                        chr.getMap().removeMonster(mob_from);
                        break;

                }
            }
        }
    }

    public static final void RenameFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        MonsterFamiliar mf = c.getPlayer().getFamiliars().get(slea.readInt());
        String newName = slea.readMapleAsciiString();
        if (mf != null && mf.getName().equals(mf.getOriginalName()) && MapleCharacterUtil.isEligibleCharName(newName, false)) {
            mf.setName(newName);
            //no packet... lol
        } else {
            chr.dropMessage(1, "Name was not eligible.");
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static final void SpawnFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        final int mId = slea.readInt();
        c.getSession().write(MaplePacketCreator.enableActions());
        c.getPlayer().removeFamiliar();
        if (c.getPlayer().getFamiliars().containsKey(mId) && slea.readByte() > 0) {
            final MonsterFamiliar mf = c.getPlayer().getFamiliars().get(mId);
            if (mf.getFatigue() > 0) {
                c.getPlayer().dropMessage(1, "Please wait " + (mf.getFatigue()) + " seconds to summon it.");
            } else {
                c.getPlayer().spawnFamiliar(mf);
            }
        }
    }

    public static final void MoveFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        slea.skip(13); //0, monster ID, pos, pos
        final Point Pos = slea.readPos();
        final List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 6, Pos);
        if (chr != null && chr.getSummonedFamiliar() != null && res.size() > 0) {
            final Point pos = chr.getSummonedFamiliar().getPosition();
            MovementParse.updatePosition(res, chr.getSummonedFamiliar(), 0);
            chr.getSummonedFamiliar().updatePosition(res);
            if (!chr.isHidden()) {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.moveFamiliar(chr.getId(), pos, res), chr.getTruePosition());
            }
        }
    }

    public static final void AttackFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr.getSummonedFamiliar() == null) {
            return;
        }
        slea.skip(6); //byte 0 and monster id, then something we don't need
        final int skillid = slea.readInt();
        FamiliarEntry f = SkillFactory.getFamiliar(skillid);
        if (f == null) {
            return;
        }
        final byte unk = slea.readByte();
        final byte size = slea.readByte();
        final List<Triple<Integer, Integer, List<Integer>>> attackPair = new ArrayList<Triple<Integer, Integer, List<Integer>>>(size);
        for (int i = 0; i < size; i++) {
            final int oid = slea.readInt();
            final int type = slea.readInt();
            slea.skip(10);
            final byte si = slea.readByte();
            List<Integer> attack = new ArrayList<Integer>(si);
            for (int x = 0; x < si; x++) {
                attack.add(slea.readInt());
            }
            attackPair.add(new Triple<Integer, Integer, List<Integer>>(oid, type, attack));
        }
        if (attackPair.isEmpty() || !chr.getCheatTracker().checkFamiliarAttack(chr) || attackPair.size() > f.targetCount) {
            return;
        }
        final MapleMonsterStats oStats = chr.getSummonedFamiliar().getOriginalStats();
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.familiarAttack(chr.getId(), unk, attackPair), chr.getTruePosition());
        for (Triple<Integer, Integer, List<Integer>> attack : attackPair) {
            final MapleMonster mons = chr.getMap().getMonsterByOid(attack.left);
            if (mons == null || !mons.isAlive() || mons.getStats().isFriendly() || mons.getLinkCID() > 0 || attack.right.size() > f.attackCount) {
                continue;
            }
            if (chr.getTruePosition().distanceSq(mons.getTruePosition()) > 640000.0 || chr.getSummonedFamiliar().getTruePosition().distanceSq(mons.getTruePosition()) > GameConstants.getAttackRange(f.lt, f.rb)) {
                chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_SUMMON);
            }
            for (int damage : attack.right) {
                if (damage <= (oStats.getPhysicalAttack() * 4)) { //approx.
                    mons.damage(chr, damage, true);
                }
            }
            if (f.makeChanceResult() && mons.isAlive()) {
                for (MonsterStatus s : f.status) {
                    mons.applyStatus(chr, new MonsterStatusEffect(s, (int) f.speed, MonsterStatusEffect.genericSkill(s), null, false), false, f.time * 1000, false, null);
                }
                if (f.knockback) {
                    mons.switchController(chr, true);
                }
            }
        }
        chr.getSummonedFamiliar().addFatigue(chr, attackPair.size());
    }

    public static final void TouchFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        //probably where familiar goes upto mob to attack; no skill
        if (chr.getSummonedFamiliar() == null) {
            return;
        }
        slea.skip(6); //byte 0 and monster id, then something we don't need
        final byte unk = slea.readByte();

        final MapleMonster target = chr.getMap().getMonsterByOid(slea.readInt());
        if (target == null) {
            return;
        }
        final int type = slea.readInt(); //always 7?
        slea.skip(4);
        int damage = slea.readInt();
        final int maxDamage = (chr.getSummonedFamiliar().getOriginalStats().getPhysicalAttack() * 5);
        if (damage < maxDamage) {
            damage = maxDamage;
        }
        if (!target.getStats().isFriendly() && chr.getCheatTracker().checkFamiliarAttack(chr)) { //approx.
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.touchFamiliar(chr.getId(), unk, target.getObjectId(), type, 600, damage), chr.getTruePosition());
            target.damage(chr, damage, true);
            chr.getSummonedFamiliar().addFatigue(chr);
        }
    }

    public static final void UseFamiliar(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        final short slot = slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        c.getSession().write(MaplePacketCreator.enableActions());
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 287) {
            return;
        }
        final StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(itemId);
        if (MapleLifeFactory.getMonsterStats(f.mob).getLevel() <= c.getPlayer().getLevel()) {
            MonsterFamiliar mf = c.getPlayer().getFamiliars().get(f.familiar);
            if (mf != null) {
                if (mf.getVitality() >= 3) {
                    mf.setExpiry((long) Math.min(System.currentTimeMillis() + 90 * 24 * 60 * 60000L, mf.getExpiry() + 30 * 24 * 60 * 60000L));
                } else {
                    mf.setVitality(mf.getVitality() + 1);
                    mf.setExpiry((long) (mf.getExpiry() + 30 * 24 * 60 * 60000L));
                }
            } else {
                mf = new MonsterFamiliar(c.getPlayer().getId(), f.familiar, (long) (System.currentTimeMillis() + 30 * 24 * 60 * 60000L));
                c.getPlayer().getFamiliars().put(f.familiar, mf);
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, false);
            c.getSession().write(MaplePacketCreator.registerFamiliar(mf));
            return;
        }
    }

    public static void CheckMobVac(MapleClient c, MapleMonster monster, List<LifeMovementFragment> res, Point startPos) {
        MapleCharacter chr = c.getPlayer();
        try {
            boolean fly = monster.getStats().getFly();
            Point endPos = null;
            int reduce_x = 0;
            int reduce_y = 0;
            for (LifeMovementFragment move : res) {
                if ((move instanceof AbstractLifeMovement)) {
                    endPos = ((LifeMovement) move).getPosition();
                    try {
                        reduce_x = Math.abs(startPos.x - endPos.x);
                        reduce_y = Math.abs(startPos.y - endPos.y);
                    } catch (Exception ex) {
                    }
                }
            }

            if (!fly) {
                int GeneallyDistance_y = 150;
                int GeneallyDistance_x = 200;
                int Check_x = 250;
                int max_x = 450;
                switch (chr.getMapId()) {
                    case 100040001:
                    case 926013500:
                        GeneallyDistance_y = 200;
                        break;
                    case 200010300:
                        GeneallyDistance_x = 1000;
                        GeneallyDistance_y = 500;
                        break;
                    case 220010600:
                    case 926013300:
                        GeneallyDistance_x = 200;
                        break;
                    case 211040001:
                        GeneallyDistance_x = 220;
                        break;
                    case 101030105:
                        GeneallyDistance_x = 250;
                        break;
                    case 541020500:
                        Check_x = 300;
                        break;
                }
                switch (monster.getId()) {
                    case 4230100:
                        GeneallyDistance_y = 200;
                        break;
                    case 9410066:
                        Check_x = 1000;
                        break;
                }
                if (GeneallyDistance_x > max_x) {
                    max_x = GeneallyDistance_x;
                }
                if (((reduce_x > GeneallyDistance_x || reduce_y > GeneallyDistance_y) && reduce_y != 0) || (reduce_x > Check_x && reduce_y == 0) || reduce_x > max_x) {
                    chr.addMobVac();
                    if (c.getPlayer().getMobVac() % 50 == 0 || reduce_x > max_x) {
                        //c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.吸怪, "(地图: " + chr.getMapId() + " 怪物数量:" + chr.getMobVac() + ")");
                        World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[外挂检测] " + chr.getName() + " (编号: " + chr.getId() + ")使用吸怪(" + chr.getMobVac() + ")! - 地图:" + chr.getMapId() + "(" + chr.getMap().getMapName() + ")"));
                        StringBuilder sb = new StringBuilder();
                        sb.append("\r\n");
                        sb.append(FileoutputUtil.CurrentReadable_TimeGMT());
                        sb.append(" 玩家: ");
                        sb.append(StringUtil.getRightPaddedStr(c.getPlayer().getName(), ' ', 13));
                        sb.append("(编号:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getId()), ' ', 5));
                        sb.append(" )怪物: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getId()), ' ', 7));
                        sb.append("(");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getObjectId()), ' ', 6));
                        sb.append(")");
                        sb.append(" 地图: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getMapId()), ' ', 9));
                        sb.append(" 初始座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(startPos.x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(startPos.y), ' ', 4));
                        sb.append(" 移动座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(endPos.x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(endPos.y), ' ', 4));
                        sb.append(" 相差座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(reduce_x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(reduce_y), ' ', 4));
                        FileoutputUtil.logToFile("Hack/吸怪.txt", sb.toString());
                        if (chr.hasGmLevel(1)) {
                            c.getPlayer().dropMessage(6, "触发吸怪 --  x: " + reduce_x + ", y: " + reduce_y);
                        }
                    }
                }
            }

        } catch (Exception ex) {

        }
    }
}
