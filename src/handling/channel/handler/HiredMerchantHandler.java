package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DBConPool;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MerchItemPackage;
import tools.FileoutputUtil;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.MaplePacketCreator;
import tools.packet.PlayerShopPacket;

public class HiredMerchantHandler {

    public static final boolean UseHiredMerchant(final MapleClient c, final boolean packet) {
        if (c.getPlayer().getMap() != null && c.getPlayer().getMap().allowPersonalShop()) {
            final byte state = checkExistance(c.getPlayer().getAccountID(), c.getPlayer().getId());
            switch (state) {
                case 1:
                    c.getPlayer().dropMessage(1, "请先去找弗兰德里领取你的东西.");
                    break;
                case 0:
                    boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
                    if (!merch) {
                        if (c.getChannelServer().isShutdown()) {
                            c.getPlayer().dropMessage(1, "服务器即将关闭，不能执行此操作.");
                            return false;
                        }
                        if (packet) {
                            c.getSession().write(PlayerShopPacket.sendTitleBox());
                        }
                        return true;
                    } else {
                        c.getSession().write(PlayerShopPacket.ShowMerchItemStore(9030000, World.getMerchantMap(c.getPlayer()), World.getMerchantChannel(c.getPlayer())));
                        //c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
                    }
                    break;
                default:
                    c.getPlayer().dropMessage(1, "发生未知的错误.");
                    break;
            }
        } else {
            c.getPlayer().ban("修改数据包 - 在非自由市场洞口内开店", true, true, false);
            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封号系统]" + c.getPlayer().getName() + " 因为使用非法软件而被永久封号。"));
            c.getSession().close();
        }
        return false;
    }

    private static final byte checkExistance(final int accid, final int cid) {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, cid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ps.close();
                rs.close();
                return 1;
            }
            rs.close();
            ps.close();
            return 0;
        } catch (SQLException se) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", se);
            return -1;
        }
    }

    public static final void displayMerch(MapleClient c) {
        final int conv = c.getPlayer().getConversation();
        boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
        if (merch) {
            //c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
            c.getSession().write(PlayerShopPacket.ShowMerchItemStore(9030000, World.getMerchantMap(c.getPlayer()), World.getMerchantChannel(c.getPlayer())));
            c.getPlayer().setConversation(0);
        } else if (c.getChannelServer().isShutdown()) {
            c.getPlayer().dropMessage(1, "服务器即将关闭，不能执行此操作.");
            c.getPlayer().setConversation(0);
        } else if (conv == 3) { // Hired Merch
            final MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getAccountID());

            if (pack == null) {
                //c.getPlayer().dropMessage(1, "You do not have any item(s) with Fredrick.");
                c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x25));
                c.getPlayer().setConversation(0);
            } else if (pack.getItems().size() <= 0) { //error fix for complainers.
                if (!check(c.getPlayer(), pack)) {
                    c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x21));
                    return;
                }
                if (deletePackage(c.getPlayer().getAccountID(), pack.getPackageid(), c.getPlayer().getId())) {
                    FileoutputUtil.logToFile_chr(c.getPlayer(), "雇佣金币领取记录.txt", " 领回金币 " + pack.getMesos());
                    c.getPlayer().gainMeso(pack.getMesos(), false);
                    // c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x1d));
                    //c.getPlayer().dropMessage(1, "You have retrieved your mesos.");
                    c.getPlayer().dropMessage(6, "领取金币" + pack.getMesos());
                } else {
                    c.getPlayer().dropMessage(1, "发生未知错误.");
                }
                c.getPlayer().setConversation(0);
            } else {
                c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
            }
        }
    }

    public static final void MerchantItemStore(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        final byte operation = slea.readByte();

        if (operation == 20) {
            displayMerch(c);
        } else if (operation == 25) {
            if (c.getPlayer().getConversation() != 3) {
                return;
            }
            c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x24));
        } else if (operation == 26) {
            if (c.getPlayer().getConversation() != 3) {
                c.getPlayer().dropMessage(1, "发生未知错误.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
            if (merch) {
                c.getPlayer().dropMessage(1, "发生未知错误,请稍后再试.");
                c.getPlayer().setConversation(0);
                return;
            }
            final MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getAccountID());

            if (pack == null) {
                //c.getPlayer().dropMessage(1, "An unknown error occured.");
                c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x25));
                return;
            } else if (c.getChannelServer().isShutdown()) {
                c.getPlayer().dropMessage(1, "服务器即将关闭，不能执行此操作.");
                c.getPlayer().setConversation(0);
                return;
            }
            if (!check(c.getPlayer(), pack)) {
                c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x21));
                return;
            }
            if (deletePackage(c.getPlayer().getAccountID(), pack.getPackageid(), c.getPlayer().getId())) {
                c.getPlayer().gainMeso(pack.getMesos(), false);
                for (Item item : pack.getItems()) {
                    MapleInventoryManipulator.addFromDrop(c, item, false);
                }
                c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x1D));
                String item_id = "";
                String item_name = "";
                for (Item item : pack.getItems()) {
                    item_id += item.getItemId() + "(" + item.getQuantity() + "), ";
                    item_name += MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "(" + item.getQuantity() + "), ";
                }
                c.getPlayer().setConversation(0);
                FileoutputUtil.logToFile_chr(c.getPlayer(), "雇佣领取记录.txt", " 领回金币 " + pack.getMesos() + " 领回道具数量 " + pack.getItems().size() + " 道具 " + item_id);
                FileoutputUtil.logToFile_chr(c.getPlayer(), "雇佣领取记录2.txt", " 领回金币 " + pack.getMesos() + " 领回道具数量 " + pack.getItems().size() + " 道具 " + item_name);
            } else {
                c.getPlayer().dropMessage(1, "发生未知错误.");
            }
        } else if (operation == 27) {
            c.getPlayer().setConversation(0);
        }
    }

    private static final boolean check(final MapleCharacter chr, final MerchItemPackage pack) {
        if (chr.getMeso() + pack.getMesos() < 0) {
            return false;
        }
        byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
        for (Item item : pack.getItems()) {
            final MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
            if (invtype == MapleInventoryType.EQUIP) {
                eq++;
            } else if (invtype == MapleInventoryType.USE) {
                use++;
            } else if (invtype == MapleInventoryType.SETUP) {
                setup++;
            } else if (invtype == MapleInventoryType.ETC) {
                etc++;
            } else if (invtype == MapleInventoryType.CASH) {
                cash++;
            }
            if (MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId()) && chr.haveItem(item.getItemId(), 1)) {
                return false;
            }
        }
        if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
            return false;
        }
        return true;
    }

    private static final boolean deletePackage(final int accid, final int packageid, final int chrId) {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where accountid = ? OR packageid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, packageid);
            ps.setInt(3, chrId);
            ps.executeUpdate();
            ps.close();
            ItemLoader.HIRED_MERCHANT.saveItems(null, packageid);
            return true;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", e);
            return false;
        }
    }

    private static final MerchItemPackage loadItemFrom_Database(final int accountid) {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ?");
            ps.setInt(1, accountid);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            }
            final int packageid = rs.getInt("PackageId");

            final MerchItemPackage pack = new MerchItemPackage();
            pack.setPackageid(packageid);
            pack.setMesos(rs.getInt("Mesos"));
            pack.setSentTime(rs.getLong("time"));

            ps.close();
            rs.close();

            Map<Long, Pair<Item, MapleInventoryType>> items = ItemLoader.HIRED_MERCHANT.loadItems(false, packageid);
            if (items != null) {
                List<Item> iters = new ArrayList<Item>();
                for (Pair<Item, MapleInventoryType> z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }

            return pack;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", e);
            return null;
        }
    }
}
