package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import client.messages.commands.CommandExecute.TradeExecute;
import constants.GameConstants;
import constants.ServerConstants.PlayerGMRank;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RankingWorker;
import server.RankingWorker.RankingInformation;
import server.life.MapleMonster;
import server.maps.*;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.StringUtil;

public class PlayerCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.NORMAL;
    }

    public static class STR extends DistributeStatCommands {

        public STR() {
            stat = MapleStat.STR;
        }
    }

    public static class DEX extends DistributeStatCommands {

        public DEX() {
            stat = MapleStat.DEX;
        }
    }

    public static class INT extends DistributeStatCommands {

        public INT() {
            stat = MapleStat.INT;
        }
    }

    public static class LUK extends DistributeStatCommands {

        public LUK() {
            stat = MapleStat.LUK;
        }
    }

    public abstract static class DistributeStatCommands extends CommandExecute {

        protected MapleStat stat = null;
        private static int statLim = 999;

        private void setStat(MapleCharacter player, int amount) {
            switch (stat) {
                case STR:
                    player.getStat().setStr((short) amount, player);
                    player.updateSingleStat(MapleStat.STR, player.getStat().getStr());
                    break;
                case DEX:
                    player.getStat().setDex((short) amount, player);
                    player.updateSingleStat(MapleStat.DEX, player.getStat().getDex());
                    break;
                case INT:
                    player.getStat().setInt((short) amount, player);
                    player.updateSingleStat(MapleStat.INT, player.getStat().getInt());
                    break;
                case LUK:
                    player.getStat().setLuk((short) amount, player);
                    player.updateSingleStat(MapleStat.LUK, player.getStat().getLuk());
                    break;
            }
        }

        private int getStat(MapleCharacter player) {
            switch (stat) {
                case STR:
                    return player.getStat().getStr();
                case DEX:
                    return player.getStat().getDex();
                case INT:
                    return player.getStat().getInt();
                case LUK:
                    return player.getStat().getLuk();
                default:
                    throw new RuntimeException(); //Will never happen.
            }
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return 0;
            }
            int change = 0;
            try {
                change = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return 0;
            }
            if (change <= 0) {
                c.getPlayer().dropMessage(5, "You must enter a number greater than 0.");
                return 0;
            }
            if (c.getPlayer().getRemainingAp() < change) {
                c.getPlayer().dropMessage(5, "You don't have enough AP for that.");
                return 0;
            }
            if (getStat(c.getPlayer()) + change > statLim) {
                c.getPlayer().dropMessage(5, "The stat limit is " + statLim + ".");
                return 0;
            }
            setStat(c.getPlayer(), getStat(c.getPlayer()) + change);
            c.getPlayer().setRemainingAp((short) (c.getPlayer().getRemainingAp() - change));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
            c.getPlayer().dropMessage(5, StringUtil.makeEnumHumanReadable(stat.name()) + " has been raised by " + change + ".");
            return 1;
        }
    }

    public static class Mob extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            MapleMonster mob = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 100000, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.isAlive()) {
                    c.getPlayer().dropMessage(6, "Monster " + mob.toString());
                    break; //only one
                }
            }
            if (mob == null) {
                c.getPlayer().dropMessage(6, "No monster was found.");
            }
            return 1;
        }
    }

    public abstract static class OpenNPCCommand extends CommandExecute {

        protected int npc = -1;
        private static int[] npcs = { //Ish yur job to make sure these are in order and correct ;(
            9270035,
            9000017,
            9000001,
            9000030,
            9010000};

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (npc != 6 && npc != 5 && npc != 4 && npc != 3 && npc != 1 && c.getPlayer().getMapId() != 910000000) { //drpcash can use anywhere
                if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200) {
                    c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                    return 0;
                }
                if (c.getPlayer().isInBlockedMap()) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                    return 0;
                }
            } else if (npc == 1) {
                if (c.getPlayer().getLevel() < 70) {
                    c.getPlayer().dropMessage(5, "You must be over level 70 to use this command.");
                    return 0;
                }
            }
            if (c.getPlayer().hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return 0;
            }
            NPCScriptManager.getInstance().start(c, npcs[npc]);
            return 1;
        }
    }

    /*public static class Npc extends OpenNPCCommand {

        public Npc() {
            npc = 0;
        }
    }

    public static class DCash extends OpenNPCCommand {

        public DCash() {
            npc = 1;
        }
    }*/

    public static class Event extends OpenNPCCommand {

        public Event() {
            npc = 2;
        }
    }
    public static class CheckDrop extends OpenNPCCommand {

        public CheckDrop() {
            npc = 4;
        }
    }

    /*public static class FM extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            for (int i : GameConstants.blockedMaps) {
                if (c.getPlayer().getMapId() == i) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                    return 0;
                }
            }
            if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200) {
                c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                return 0;
            }*/
    //if (c.getPlayer().hasBlockedInventory() || c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000/* || FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())*/) {
    // c.getPlayer().dropMessage(5, "You may not use this command here.");
    //return 0;
    // }
    /*if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010) || (c.getPlayer().getMapId() / 10 == 13003000)) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return 0;
            }
            c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET, c.getPlayer().getMap().getReturnMap().getId());
            MapleMap map = c.getChannelServer().getMapFactory().getMap(910000000);
            c.getPlayer().changeMap(map, map.getPortal(0));
            return 1;
        }
    }*/
    public static class 解卡 extends EA {
    }

    public static class EA extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            return 1;
        }
    }

    /*public static class TSmega extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().setSmega();
            return 1;
        }
    }*/

 /*public static class Ranking extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) { //job start end
                c.getPlayer().dropMessage(5, "Use @ranking [job] [start number] [end number] where start and end are ranks of the players");
                final StringBuilder builder = new StringBuilder("JOBS: ");
                for (String b : RankingWorker.getJobCommands().keySet()) {
                    builder.append(b);
                    builder.append(" ");
                }
                c.getPlayer().dropMessage(5, builder.toString());
            } else {
                int start = 1, end = 20;
                try {
                    start = Integer.parseInt(splitted[2]);
                    end = Integer.parseInt(splitted[3]);
                } catch (NumberFormatException e) {
                    c.getPlayer().dropMessage(5, "You didn't specify start and end number correctly, the default values of 1 and 20 will be used.");
                }
                if (end < start || end - start > 20) {
                    c.getPlayer().dropMessage(5, "End number must be greater, and end number must be within a range of 20 from the start number.");
                } else {
                    final Integer job = RankingWorker.getJobCommand(splitted[1]);
                    if (job == null) {
                        c.getPlayer().dropMessage(5, "Please use @ranking to check the job names.");
                    } else {
                        final List<RankingInformation> ranks = RankingWorker.getRankingInfo(job.intValue());
                        if (ranks == null || ranks.size() <= 0) {
                            c.getPlayer().dropMessage(5, "Please try again later.");
                        } else {
                            int num = 0;
                            for (RankingInformation rank : ranks) {
                                if (rank.rank >= start && rank.rank <= end) {
                                    if (num == 0) {
                                        c.getPlayer().dropMessage(6, "Rankings for " + splitted[1] + " - from " + start + " to " + end);
                                        c.getPlayer().dropMessage(6, "--------------------------------------");
                                    }
                                    c.getPlayer().dropMessage(6, rank.toString());
                                    num++;
                                }
                            }
                            if (num == 0) {
                                c.getPlayer().dropMessage(5, "No ranking was returned.");
                            }
                        }
                    }
                }
            }
            return 1;
        }
    }*/

 /*public static class Check extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getCSPoints(1) + " Cash.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getPoints() + " donation points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getVPoints() + " voting points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getIntNoRecord(GameConstants.BOSS_PQ) + " Boss Party Quest points.");
            c.getPlayer().dropMessage(6, "The time is currently " + FileoutputUtil.CurrentReadable_TimeGMT() + " GMT.");
            return 1;
        }
    }*/

 /*public static class CheckRingID extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "Each Ring will cost 20,000 A-Cash");
            c.getPlayer().dropMessage(5, "Please do not purchase more than 1 ring");
            c.getPlayer().dropMessage(5, "Here the Ring ID for Friendship Ring");
            c.getPlayer().dropMessage(5, "----------------");
            c.getPlayer().dropMessage(5, "Friendship Ring Clover RingID = 1112800");
            c.getPlayer().dropMessage(5, "Friendship Ring Flower Petal RingID = 1112801");
            c.getPlayer().dropMessage(5, "Friendship Ring Star RingID = 1112802");
            c.getPlayer().dropMessage(5, "----------------");
            c.getPlayer().dropMessage(5, "Here the Ring ID for Crush Ring");
            c.getPlayer().dropMessage(5, "----------------");
            c.getPlayer().dropMessage(5, "Couple Ring RingID = 1112001");
            c.getPlayer().dropMessage(5, "Heart Couple Couple Ring RingID = 1112002");
            c.getPlayer().dropMessage(5, "Cupid's Ring RingID = 1112003");
            c.getPlayer().dropMessage(5, "Venus Firework RingID = 1112005");
            c.getPlayer().dropMessage(5, "Crossed Hearts RingID = 1112006");
            c.getPlayer().dropMessage(5, "----------------");
            c.getPlayer().dropMessage(5, "Please do report to any GM if the ring id is invaild");
            return 1;
        }
    }*/

 /*public static class BuyRing extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "Use @BuyRing <Partner IGN> <ringID>");
                c.getPlayer().dropMessage(6, "Use @checkRingID for more information");
                return 0;
            }
            if (c.getPlayer().getCSPoints(1) < 20000) {
                c.getPlayer().dropMessage(6, "Lack of A-Cash, please come back again later.");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[2]);
            if (!GameConstants.isEffectRing(itemId)) {
                c.getPlayer().dropMessage(6, "Invalid itemID.");
            } else {
                MapleCharacter fff = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (fff == null) {
                    c.getPlayer().dropMessage(6, "Player must be online");
                } else {
                    int[] ringID = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
                    try {
                        MapleCharacter[] chrz = {fff, c.getPlayer()};
                        for (int i = 0; i < chrz.length; i++) {
                            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId, ringID[i]);
                            if (eq == null) {
                                c.getPlayer().dropMessage(6, "Invalid itemID.");
                                return 0;
                            }
                            MapleInventoryManipulator.addbyItem(chrz[i].getClient(), eq.copy());
                            chrz[i].dropMessage(6, "Successfully ring with " + chrz[i == 0 ? 1 : 0].getName());
                            c.getPlayer().modifyCSPoints(1, -20000);
                        }
                        MapleRing.addToDB(itemId, c.getPlayer(), fff.getName(), fff.getId(), ringID);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return 1;
        }
    }*/
    public static class Help extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "@str, @dex, @int, @luk < 要增加的属性点数 >");
            //c.getPlayer().dropMessage(5, "@mob < Information on the closest monster >");
            // c.getPlayer().dropMessage(5, "@loot < Loot items that drop from the mobs >");
            //c.getPlayer().dropMessage(5, "@check < Displays various information >");
            //c.getPlayer().dropMessage(5, "@fm < Warp to FM >");
            //c.getPlayer().dropMessage(5, "@npc < Universal Town Warp / Event NPC>");
            //c.getPlayer().dropMessage(5, "@tsmega < Toggle super megaphone on/off >");
            c.getPlayer().dropMessage(5, "@ea 和 @解卡< 在不能攻击或不能跟npc对话,请在聊天框打 @解卡/@ea 来解除异常状态 >");
            //c.getPlayer().dropMessage(5, "@eventrewards < Use this command to go to the Rewards Map >");
            //c.getPlayer().dropMessage(5, "@ranking < Use @ranking for more details >");
            c.getPlayer().dropMessage(5, "@checkdrop < 使用 @checkdrop 来查询当前地图怪物爆率 >");
            //c.getPlayer().dropMessage(5, "@expedition < Warp you to the Expedition Map. Minimum level : 50 >");
            //c.getPlayer().dropMessage(5, "@christmas < Warp you to Christmap Map Minimum level : 10 >");
            //c.getPlayer().dropMessage(5, "@warptodcash < Warp you to Cash Dropper Map. Minimum level : 10 >");
            //c.getPlayer().dropMessage(5, "@quest < Warp you to Quest Map. Minimum level : 30 >");
            //c.getPlayer().dropMessage(5, "@CheckRingID < Check Ring ID and available Ring >");
            //c.getPlayer().dropMessage(5, "@BuyRing < Purchase Ring @BuyRing <Ringid> <Partner's IGN> >");
            return 1;
        }
    }

    /*public static class TradeHelp extends TradeExecute {

        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(-2, "[System] : <@offerequip, @offeruse, @offersetup, @offeretc, @offercash> <quantity> <name of the item>");
            return 1;
        }
    }*/

 /*public abstract static class OfferCommand extends TradeExecute {

        protected int invType = -1;

        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-2, "[Error] : <quantity> <name of item>");
            } else if (c.getPlayer().getLevel() < 70) {
                c.getPlayer().dropMessage(-2, "[Error] : Only level 70+ may use this command");
            } else {
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(splitted[1]);
                } catch (Exception e) { //swallow and just use 1
                }
                String search = StringUtil.joinStringFrom(splitted, 2).toLowerCase();
                Item found = null;
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Item inv : c.getPlayer().getInventory(MapleInventoryType.getByType((byte) invType))) {
                    if (ii.getName(inv.getItemId()) != null && ii.getName(inv.getItemId()).toLowerCase().contains(search)) {
                        found = inv;
                        break;
                    }
                }
                if (found == null) {
                    c.getPlayer().dropMessage(-2, "[Error] : No such item was found (" + search + ")");
                    return 0;
                }
                if (GameConstants.isPet(found.getItemId()) || GameConstants.isRechargable(found.getItemId())) {
                    c.getPlayer().dropMessage(-2, "[Error] : You may not trade this item using this command");
                    return 0;
                }
                if (quantity > found.getQuantity() || quantity <= 0 || quantity > ii.getSlotMax(found.getItemId())) {
                    c.getPlayer().dropMessage(-2, "[Error] : Invalid quantity");
                    return 0;
                }
                if (!c.getPlayer().getTrade().setItems(c, found, (byte) -1, quantity)) {
                    c.getPlayer().dropMessage(-2, "[Error] : This item could not be placed");
                    return 0;
                } else {
                    c.getPlayer().getTrade().chatAuto("[System] : " + c.getPlayer().getName() + " offered " + ii.getName(found.getItemId()) + " x " + quantity);
                }
            }
            return 1;
        }
    }*/

 /*public static class OfferEquip extends OfferCommand {

        public OfferEquip() {
            invType = 1;
        }
    }

    public static class OfferUse extends OfferCommand {

        public OfferUse() {
            invType = 2;
        }
    }

    public static class OfferSetup extends OfferCommand {

        public OfferSetup() {
            invType = 3;
        }
    }

    public static class OfferEtc extends OfferCommand {

        public OfferEtc() {
            invType = 4;
        }
    }

    public static class OfferCash extends OfferCommand {

        public OfferCash() {
            invType = 5;
        }
    }*/

 /*public static class eventrewards extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getLevel() >= 10) {
                final MapleMap map = c.getChannelServer().getMapFactory().getMap(220000304);
                c.getPlayer().changeMap(map);
            } else {
                c.getPlayer().dropMessage(-3, "You're not level 10.");
            }
            return 1;
        }
    }

    public static class quest extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getLevel() >= 30) {
                final MapleMap map = c.getChannelServer().getMapFactory().getMap(240070000);
                c.getPlayer().changeMap(map);
            } else {
                c.getPlayer().dropMessage(-3, "You're not level 30.");
            }
            return 1;
        }
    }

    public static class warptodcash extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getLevel() >= 10) {
                final MapleMap map = c.getChannelServer().getMapFactory().getMap(910020100);
                c.getPlayer().changeMap(map);
            } else {
                c.getPlayer().dropMessage(-3, "You're not level 10.");
            }
            return 1;
        }
    }

    public static class expedition extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getLevel() >= 50) {
                final MapleMap map = c.getChannelServer().getMapFactory().getMap(910000013);
                c.getPlayer().changeMap(map);
            } else {
                c.getPlayer().dropMessage(-3, "You're not level 50.");
            }
            return 1;
        }
    }

    public static class christmas extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getLevel() >= 10) {
                final MapleMap map = c.getChannelServer().getMapFactory().getMap(209000000);
                c.getPlayer().changeMap(map);
            } else {
                c.getPlayer().dropMessage(-3, "You're not level 10.");
            }
            return 1;
        }
    }

    public static class Loot extends CommandExecute {

        public int execute(MapleClient c, String[] splitted) {
            final int[] map = {280030001, 280030000, 240060201, 240060200, 551030200, 271040100};
            for (int i : map) {
                if (c.getPlayer().getMapId() != i) {
                    if (c.getPlayer().getPets() != null) {
                        List<MapleMapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getTruePosition(), c.getPlayer().getRange(), Arrays.asList(MapleMapObjectType.ITEM));
                        for (MapleMapObject item : items) {
                            MapleMapItem mapItem = (MapleMapItem) item;
                            if (mapItem.isPickedUp()) {
                                c.getSession().write(MaplePacketCreator.getInventoryFull());
                                continue;
                            }
                            if (mapItem.getOwner() != c.getPlayer().getId() && mapItem.isPlayerDrop()) {
                                continue;
                            }
                            if (mapItem.getOwner() != c.getPlayer().getId() && ((!mapItem.isPlayerDrop() && mapItem.getDropType() == 0) || (mapItem.isPlayerDrop() && c.getPlayer().getMap().getEverlast()))) {
                                c.getSession().write(MaplePacketCreator.enableActions());
                                continue;
                            }
                            if (!mapItem.isPlayerDrop() && mapItem.getDropType() == 1 && mapItem.getOwner() != c.getPlayer().getId()) {
                                c.getSession().write(MaplePacketCreator.enableActions());
                                continue;
                            }

                            if (mapItem.getMeso() > 0) {
                                c.getPlayer().gainMeso(mapItem.getMeso(), true);
                                mapItem.setPickedUp(true);
                                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapItem.getObjectId(), 5, c.getPlayer().getId(), 0));
                                c.getPlayer().getMap().removeMapObject(mapItem);
                            } else {
                                if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapItem.getItemId()) || mapItem.getItemId() / 10000 == 291) {
                                    c.getSession().write(MaplePacketCreator.enableActions());
                                } else if (MapleInventoryManipulator.checkSpace(c, mapItem.getItemId(), mapItem.getItem().getQuantity(), mapItem.getItem().getOwner())) {
                                    if (mapItem.getItem().getQuantity() >= 50 && mapItem.getItemId() == 2340000) {
                                        c.setMonitored(true); //hack check
                                    }
                                    MapleInventoryManipulator.addFromDrop(c, mapItem.getItem(), true, mapItem.getDropper() instanceof MapleMonster, false);
                                }
                                mapItem.setPickedUp(true);
                                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapItem.getObjectId(), 5, c.getPlayer().getId(), 0));
                                c.getPlayer().getMap().removeMapObject(mapItem);
                            }
                        }
                    } else {
                        c.getPlayer().dropMessage(-5, "You need to have at least 1 pet to use this.");
                    }
                } else {
                    c.getPlayer().dropMessage(-5, "Sorry, this command is not available in boss map");
                }
            }
            return 1;
        }
    }*/
}
