package server.life;

import constants.GameConstants;
import database.DBConPool;
import java.awt.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Mob.wz"));
    private static final MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Npc.wz"));
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/String.wz"));
    private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Etc.wz"));
    private static final MapleData mobStringData = stringDataWZ.getData("Mob.img");
    private static final MapleData npcStringData = stringDataWZ.getData("Npc.img");
    private static final MapleData npclocData = etcDataWZ.getData("NpcLocation.img");
    private static Map<Integer, String> npcNames = new HashMap<Integer, String>();
    private static Map<Integer, MapleMonsterStats> monsterStats = new HashMap<Integer, MapleMonsterStats>();
    private static Map<Integer, Integer> NPCLoc = new HashMap<Integer, Integer>();
    private static Map<Integer, List<Integer>> questCount = new HashMap<Integer, List<Integer>>();

    public static AbstractLoadedMapleLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        } else if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        } else {
            System.err.println("Unknown Life type: " + type + "");
            return null;
        }
    }

    public static int getNPCLocation(int npcid) {
        if (NPCLoc.containsKey(npcid)) {
            return NPCLoc.get(npcid);
        }
        final int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData, -1);
        NPCLoc.put(npcid, map);
        return map;
    }

    public static final void loadQuestCounts() {
        if (questCount.size() > 0) {
            return;
        }
        for (MapleDataDirectoryEntry mapz : data.getRoot().getSubdirectories()) {
            if (mapz.getName().equals("QuestCountGroup")) {
                for (MapleDataFileEntry entry : mapz.getFiles()) {
                    final int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                    MapleData dat = data.getData("QuestCountGroup/" + entry.getName());
                    if (dat != null && dat.getChildByPath("info") != null) {
                        List<Integer> z = new ArrayList<Integer>();
                        for (MapleData da : dat.getChildByPath("info")) {
                            z.add(MapleDataTool.getInt(da, 0));
                        }
                        questCount.put(id, z);
                    } else {
                        System.out.println("null questcountgroup");
                    }
                }
            }
        }
        /*for (MapleData c : npcStringData) {
            int nid = Integer.parseInt(c.getName());
            String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);
            try {
                if (npcData.getData(n) != null) {//only thing we really have to do is check if it exists. if we wanted to, we could get the script as well :3
                    String name = MapleDataTool.getString("name", c, "MISSINGNO");
                    if (name.contains("Maple TV") || name.contains("Baby Moon Bunny")) {
                        continue;
                    }
                    npcNames.put(nid, name);
                }
            } catch (NullPointerException e) {
            } catch (RuntimeException e) { //swallow, don't add if 
            }
        }*/
    }

    public static final List<Integer> getQuestCount(final int id) {
        return questCount.get(id);
    }

    public static MapleMonster getMonster(int mid) {
        MapleMonsterStats stats = getMonsterStats(mid);
        if (stats == null) {
            return null;
        }
        return new MapleMonster(mid, stats);
    }

    public static MapleMonsterStats getMonsterStats(int mid) {
        MapleMonsterStats stats = monsterStats.get(Integer.valueOf(mid));

        if (stats == null) {
            MapleData monsterData = null;
            try {
                monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
            } catch (RuntimeException e) {
                return null;
            }
            if (monsterData == null) {
                return null;
            }
            MapleData monsterInfoData = monsterData.getChildByPath("info");
            stats = new MapleMonsterStats(mid);

            stats.setHp(GameConstants.getPartyPlayHP(mid) > 0 ? GameConstants.getPartyPlayHP(mid) : MapleDataTool.getIntConvert("maxHP", monsterInfoData));
            stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
            stats.setExp(mid == 9300027 ? 0 : (GameConstants.getPartyPlayEXP(mid) > 0 ? GameConstants.getPartyPlayEXP(mid) : MapleDataTool.getIntConvert("exp", monsterInfoData, 0)));
            stats.setLevel((short) MapleDataTool.getIntConvert("level", monsterInfoData, 1));
            stats.setCharismaEXP((short) MapleDataTool.getIntConvert("charismaEXP", monsterInfoData, 0));
            stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
            stats.setrareItemDropLevel((byte) MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
            stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
            stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
            stats.setBoss(GameConstants.getPartyPlayHP(mid) > 0 || MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0 || mid == 8810018 || mid == 9410066 || (mid >= 8810118 && mid <= 8810122));
            stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
            stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
            stats.setEscort(MapleDataTool.getIntConvert("escort", monsterInfoData, 0) > 0);
            stats.setPartyBonus(GameConstants.getPartyPlayHP(mid) > 0 || MapleDataTool.getIntConvert("partyBonusMob", monsterInfoData, 0) > 0);
            stats.setPartyBonusRate(MapleDataTool.getIntConvert("partyBonusR", monsterInfoData, 0));
            if (mobStringData.getChildByPath(String.valueOf(mid)) != null) {
                stats.setName(MapleDataTool.getString("name", mobStringData.getChildByPath(String.valueOf(mid)), "MISSINGNO"));
            }
            stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
            stats.setChange(MapleDataTool.getIntConvert("changeableMob", monsterInfoData, 0) > 0);
            stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
            stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
            stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
            stats.setCP((byte) MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
            stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
            stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
            stats.setPhysicalAttack(MapleDataTool.getIntConvert("PADamage", monsterInfoData, 0));
            stats.setMagicAttack(MapleDataTool.getIntConvert("MADamage", monsterInfoData, 0));
            stats.setPDRate((byte) MapleDataTool.getIntConvert("PDRate", monsterInfoData, 0));
            stats.setMDRate((byte) MapleDataTool.getIntConvert("MDRate", monsterInfoData, 0));
            stats.setAcc(MapleDataTool.getIntConvert("acc", monsterInfoData, 0));
            stats.setEva(MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
            stats.setSummonType((byte) MapleDataTool.getIntConvert("summonType", monsterInfoData, 0));
            stats.setCategory((byte) MapleDataTool.getIntConvert("category", monsterInfoData, 0));
            stats.setSpeed(MapleDataTool.getIntConvert("speed", monsterInfoData, 0));
            stats.setPushed(MapleDataTool.getIntConvert("pushed", monsterInfoData, 0));
            //final boolean hideHP = MapleDataTool.getIntConvert("HPgaugeHide", monsterInfoData, 0) > 0 || MapleDataTool.getIntConvert("hideHP", monsterInfoData, 0) > 0;
            final MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
            if (selfd != null) {
                stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
                stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", selfd, stats.getRemoveAfter()));
                stats.setSelfD((byte) MapleDataTool.getIntConvert("action", selfd, -1));
            } else {
                stats.setSelfD((byte) -1);
            }
            final MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
            if (firstAttackData != null) {
                if (firstAttackData.getType() == MapleDataType.FLOAT) {
                    stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
                } else {
                    stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
                }
            }
            if (stats.isBoss() || isDmgSponge(mid)) {
                if (monsterInfoData.getChildByPath("hpTagColor") == null || monsterInfoData.getChildByPath("hpTagBgcolor") == null) {
                    stats.setTagColor(0);
                    stats.setTagBgColor(0);
                } else {
                    stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
                    stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
                }
            }
            final MapleData banishData = monsterInfoData.getChildByPath("ban");
            if (banishData != null) {
                stats.setBanishInfo(new BanishInfo(
                        MapleDataTool.getString("banMsg", banishData),
                        MapleDataTool.getInt("banMap/0/field", banishData, -1),
                        MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
            }

            final MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
            if (reviveInfo != null) {
                List<Integer> revives = new LinkedList<Integer>();
                for (MapleData bdata : reviveInfo) {
                    revives.add(MapleDataTool.getInt(bdata));
                }
                stats.setRevives(revives);
            }

            final MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
            if (monsterSkillData != null) {
                int i = 0;
                List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
                while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                    skills.add(new Pair<Integer, Integer>(Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
                    i++;
                }
                stats.setSkills(skills);
            }

            decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));

            final int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
            if (link != 0) { // Store another copy, for faster processing.
                monsterData = data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
            }

            for (MapleData idata : monsterData) {
                if (idata.getName().equals("fly")) {
                    stats.setFly(true);
                    stats.setMobile(true);
                    break;
                } else if (idata.getName().equals("move")) {
                    stats.setMobile(true);
                }
            }

            for (int i = 1; true; i++) {
                final MapleData attackData = monsterData.getChildByPath("attack" + i + "/info");
                if (attackData == null) {
                    break;
                }
                final MobAttackInfo ret = new MobAttackInfo();
                ret.setDeadlyAttack(attackData.getChildByPath("deadlyAttack") != null);
                ret.setMpBurn(MapleDataTool.getInt("mpBurn", attackData, 0));
                ret.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
                ret.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
                ret.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
                ret.attackAfter = MapleDataTool.getInt("attackAfter", attackData, 0);
                ret.PADamage = MapleDataTool.getInt("PADamage", attackData, 0);
                ret.MADamage = MapleDataTool.getInt("PADamage", attackData, 0);
                ret.magic = MapleDataTool.getInt("magic", attackData, 0) > 0;
                if (attackData.getChildByPath("range") != null) {
                    ret.range = MapleDataTool.getInt("range/r", attackData, 0);
                    if (attackData.getChildByPath("range/lt") != null && attackData.getChildByPath("range/rb") != null) {
                        ret.lt = (Point) attackData.getChildByPath("range/lt").getData();
                        ret.rb = (Point) attackData.getChildByPath("range/rb").getData();
                    }
                }
                stats.addMobAttack(ret);
            }

            byte hpdisplaytype = -1;
            if (stats.getTagColor() > 0) {
                hpdisplaytype = 0;
            } else if (stats.isFriendly()) {
                hpdisplaytype = 1;
            } else if (mid >= 9300184 && mid <= 9300215) { // Mulung TC mobs
                hpdisplaytype = 2;
            } else if (!stats.isBoss() || mid == 9410066 || stats.isPartyBonus()) { // Not boss and dong dong chiang
                hpdisplaytype = 3;
            }
            stats.setHPDisplayType(hpdisplaytype);

            monsterStats.put(Integer.valueOf(mid), stats);
        }
        return stats;
    }

    public static final void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(
                    Element.getFromChar(elemAttr.charAt(i)),
                    ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

    private static final boolean isDmgSponge(final int mid) {
        switch (mid) {
            case 8810018:
            case 8810118:
            case 8810119:
            case 8810120:
            case 8810121:
            case 8810122:
            case 8820009:
            case 8820010:
            case 8820011:
            case 8820012:
            case 8820013:
            case 8820014:
                return true;
        }
        return false;
    }

    public static MapleNPC getNPC(final int nid) {

        String name = npcNames.get(nid);
        if (nid == 9330042 || nid == 9209000 || nid == 1002103 || nid == 9102001 || nid == 9209008 || nid == 9209007 || nid == 9330042 || nid == 9310023 || nid == 9310027 || nid == 9310025 || nid == 1002103 || nid == 9310026 || nid == 9310028 || nid == 9310062 || nid == 9310029 || nid == 9110009 || nid == 9310024 || nid == 9110012 || nid == 9310059) {
            return null;
        }

        if (name == null) {
            name = MapleDataTool.getString(nid + "/name", npcStringData, "MISSINGNO");
            npcNames.put(nid, name);
        }
        if (name.contains("Maple TV")) {
            return null;
        }
        return new MapleNPC(nid, name);
    }

    /*public static MapleNPC getNPC(final int nid) {
        String name = npcNames.get(nid);
        if (name == null) {
            return null;
        }
        return new MapleNPC(nid, name);
    }*/

 /*public static int getRandomNPC() {
        List<Integer> vals = new ArrayList<Integer>(npcNames.keySet());
        int ret = 0;
        while (ret <= 0) {
            ret = vals.get(Randomizer.nextInt(vals.size()));
            if (npcNames.get(ret).contains("MISSINGNO")) {
                ret = 0;
            }
        }
        return ret;
    }*/
}
