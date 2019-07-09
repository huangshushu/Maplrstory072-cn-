package handling.cashshop.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.MTSCSConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.World;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.AutobanManager;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MTSCart;
import server.MTSStorage;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.packet.MaplePacketCreator;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CSPacket;
import tools.packet.MTSPacket;

public class CashShopOperation {

    public static void LeaveCS(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        //CashShopServer.getPlayerStorageMTS().deregisterPlayer(chr);
        CashShopServer.getPlayerStorage().deregisterPlayer(chr);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        try {
            final String s = c.getSessionIPAddress();
            //LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));
            World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
            c.getSession().write(MaplePacketCreator.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
        } finally {
            chr.saveToDB(false, true);
            c.setPlayer(null);
            c.setReceiving(false);
            //c.getSession().close();
        }
    }

    public static void EnterCS(final int playerid, final MapleClient c) {
        CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
        if (transfer == null) {
            c.getSession().close();
            return;
        }
        MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);
        c.setPlayer(chr);
        c.setAccID(chr.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }

        final int state = c.getLoginState();
        boolean allowLogin = false;
        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
            if (!World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()))) {
                allowLogin = true;
            }
        }
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close();
            return;
        }
        c.getPlayer().saveToDB(false, false);
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        CashShopServer.getPlayerStorage().registerPlayer(chr);
        c.getSession().write(CSPacket.warpCS(c));
        CSUpdate(c);
    }

    public static void CSUpdate(final MapleClient c) {
        c.getSession().write(CSPacket.getCSGifts(c));
        doCSPackets(c);
        c.getSession().write(CSPacket.sendWishList(c.getPlayer(), false));
    }

    public static void CouponCode(final String code, final MapleClient c) {
        if (code.length() <= 0) {
            return;
        }
        Triple<Boolean, Integer, Integer> info = null;
        try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (info != null && info.left) {
            int type = info.mid, item = info.right;
            try {
                MapleCharacterUtil.setNXCodeUsed(c.getPlayer().getName(), code);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            /*
             * Explanation of type!
             * Basically, this makes coupon codes do
             * different things!
             *
             * Type 1: A-Cash,
             * Type 2: Maple Points
             * Type 3: Item.. use SN
             * Type 4: Mesos
             */
            Map<Integer, Item> itemz = new HashMap<Integer, Item>();
            int maplePoints = 0, mesos = 0;
            switch (type) {
                case 1:
                case 2:
                    c.getPlayer().modifyCSPoints(type, item, false);
                    maplePoints = item;
                    break;
                case 3:
                    CashItemInfo itez = CashItemFactory.getInstance().getItem(item);
                    if (itez == null) {
                        c.getSession().write(CSPacket.sendCSFail(0));
                        return;
                    }
                    byte slot = MapleInventoryManipulator.addId(c, itez.getId(), (short) 1, "", "Cash shop: coupon code" + " on " + FileoutputUtil.CurrentReadable_Date());
                    if (slot <= -1) {
                        c.getSession().write(CSPacket.sendCSFail(0));
                        return;
                    } else {
                        itemz.put(item, c.getPlayer().getInventory(GameConstants.getInventoryType(item)).getItem(slot));
                    }
                    break;
                case 4:
                    c.getPlayer().gainMeso(item, false);
                    mesos = item;
                    break;
            }
            c.getSession().write(CSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
        } else {
            c.getSession().write(CSPacket.sendCSFail(info == null ? 0xA7 : 0xA5)); //A1, 9F
        }
    }

    public static final void BuyCashItem(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        final int action = slea.readByte();
        if (action == 0) {
            slea.skip(2);
            CouponCode(slea.readMapleAsciiString(), c);
        } else if (action == 3) {
            final int toCharge = slea.readByte() + 1;
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());

            if (item != null) {
                System.out.println("购买事件ID: " + action + " 物品代码: " + item.getId() + " 物品SN:" + item.getSN());
            }

            if (item != null && chr.getCSPoints(toCharge) >= item.getPrice()) {
                if (!item.genderEquals(c.getPlayer().getGender())) {
                    c.getSession().write(CSPacket.sendCSFail(0xA6));
                    doCSPackets(c);
                    return;
                } else if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                    c.getSession().write(CSPacket.sendCSFail(0xB1));
                    doCSPackets(c);
                    return;
                }
                for (int i : MTSCSConstants.cashBlock) {
                    if (item.getId() == i) {
                        c.getPlayer().dropMessage(1, MTSCSConstants.getCashBlockedMsg(item.getId()));
                        doCSPackets(c);
                        return;
                    }
                }
                if ((item.getId() == 5220000 && toCharge == 2) || (item.getId() == 5220010 && toCharge == 2) || (item.getId() == 5220040 && toCharge == 2)) {
                    chr.dropMessage(1, "该道具无法使用低用券购买");
                    doCSPackets(c);
                    return;
                }
                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
                if (item.getId() == 2140003 || item.getId() == 2140004 || item.getId() == 2140005 || item.getId() == 2140006 || item.getId() == 2140007 || item.getId() == 2140008) {
                    c.getPlayer().modifyCSPoints(2, item.getPrice(), false);
                    chr.dropMessage(1, "成功购买抵用券:" + item.getPrice());
                    doCSPackets(c);
                    return;
                }
                Item itemz = chr.getCashInventory().toItem(item);
                if (itemz != null && itemz.getUniqueId() > 0 && itemz.getItemId() == item.getId() && itemz.getQuantity() == item.getCount()) {
                    chr.getCashInventory().addToInventory(itemz);
                    c.getSession().write(CSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0));
                }
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
        } else if (action == 4 /*|| action == 32*/) { //gift, package
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            String partnerName = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            if (item == null || c.getPlayer().getCSPoints(1) < item.getPrice() || msg.length() > 73 || msg.length() < 1) { //dont want packet editors gifting random stuff =P
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if (info == null || info.getLeft() <= 0 || info.getLeft() == c.getPlayer().getId() || info.getMid() == c.getAccID()) {
                c.getSession().write(CSPacket.sendCSFail(0xA2)); //9E v75
                doCSPackets(c);
                return;
            } else if (!item.genderEquals(info.getRight())) {
                c.getSession().write(CSPacket.sendCSFail(0xA3));
                doCSPackets(c);
                return;
            } else {
                for (int i : MTSCSConstants.cashBlock) {
                    if (item.getId() == i) {
                        c.getPlayer().dropMessage(1, MTSCSConstants.getCashBlockedMsg(item.getId()));
                        doCSPackets(c);
                        return;
                    }
                }
                c.getPlayer().getCashInventory().gift(info.getLeft(), c.getPlayer().getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
                c.getPlayer().modifyCSPoints(1, -item.getPrice(), false);
                c.getSession().write(CSPacket.sendGift(item.getPrice(), item.getId(), item.getCount(), partnerName));
            }
        } else if (action == 5) { // Wishlist
            chr.clearWishlist();
            if (slea.available() < 40) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            int[] wishlist = new int[10];
            for (int i = 0; i < 10; i++) {
                wishlist[i] = slea.readInt();
            }
            chr.setWishlist(wishlist);
            c.getSession().write(CSPacket.sendWishList(chr, true));

        } else if (action == 6) { // Increase inv
            final int toCharge = slea.readByte() + 1;
            final boolean coupon = slea.readByte() > 0;
            if (coupon) {
                final MapleInventoryType type = getInventoryType(slea.readInt());

                if (chr.getCSPoints(toCharge) >= 1100 && chr.getInventory(type).getSlotLimit() < 89) {
                    chr.modifyCSPoints(toCharge, -1100, false);
                    chr.getInventory(type).addSlot((byte) 8);
                    chr.dropMessage(1, "栏位已增加到 " + chr.getInventory(type).getSlotLimit());
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0));
                }
            } else {
                final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());

                if (chr.getCSPoints(toCharge) >= 600 && chr.getInventory(type).getSlotLimit() < 93) {
                    chr.modifyCSPoints(toCharge, -600, false);
                    chr.getInventory(type).addSlot((byte) 4);
                    chr.dropMessage(1, "栏位已增加到 " + chr.getInventory(type).getSlotLimit());
                } else {
                    chr.dropMessage(1, "栏位已满");
                    c.getSession().write(CSPacket.sendCSFail(0));
                }
            }

        } else if (action == 7) { // Increase slot space
            final int toCharge = slea.readByte() + 1;
            //final int coupon = slea.readByte() > 0 ? 2 : 1;
            if (chr.getCSPoints(toCharge) >= 600 /**
                     * coupon
                     */
                    && chr.getStorage().getSlots() < (49 - (4 /**
                     * coupon
                     */
                    ))) {
                chr.modifyCSPoints(toCharge, -600 /**
                         * coupon
                         */
                        , false);
                chr.getStorage().increaseSlots((byte) (4 /**
                         * coupon
                         */
                        ));
                chr.getStorage().saveToDB();
                chr.dropMessage(1, "仓库栏位已增加到 " + chr.getStorage().getSlots());
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
        } else if (action == 8) { //...9 = pendant slot expansion
            slea.skip(1);
            final int toCharge = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            int slots = c.getCharacterSlots();
            if (item == null || c.getPlayer().getCSPoints(toCharge) < item.getPrice() || slots > 15 || item.getId() != 5430000) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (c.gainCharacterSlot()) {
                c.getPlayer().modifyCSPoints(toCharge, -item.getPrice(), false);
                chr.dropMessage(1, "角色栏位增加到: " + (slots + 1));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }/*
        } else if (action == 9) { //...9 = pendant slot expansion
            slea.readByte();
	    final int sn = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(sn);
            int slots = c.getCharacterSlots();
            if (item == null || c.getPlayer().getCSPoints(1) < item.getPrice() || item.getId() / 10000 != 555) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            MapleQuestStatus marr = c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
	    if (marr != null && marr.getCustomData() != null && Long.parseLong(marr.getCustomData()) >= System.currentTimeMillis()) {
                c.getSession().write(CSPacket.sendCSFail(0));
	    } else {
		c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(String.valueOf(System.currentTimeMillis() + ((long)item.getPeriod() * 24 * 60 * 60000)));
		c.getPlayer().modifyCSPoints(1, -item.getPrice(), false);
                chr.dropMessage(1, "Additional pendant slot gained.");
            }*/
        } else if (action == 0x0C) { //get item from csinventory
            Item item = c.getPlayer().getCashInventory().findByCashId((int) slea.readLong());
            if (item != null && item.getQuantity() > 0 && MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                Item item_ = item.copy();
                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
                if (pos >= 0) {
                    if (item_.getPet() != null) {
                        item_.getPet().setInventoryPosition(pos);
                        c.getPlayer().addPet(item_.getPet());
                    }
                    c.getPlayer().getCashInventory().removeFromInventory(item);
                    c.getSession().write(CSPacket.confirmFromCSInventory(item_, pos));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xB1));
                }
            } else {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
            }
        } else if (action == 0x0D) { //put item in cash inventory
            int uniqueid = (int) slea.readLong();
            int sn;
            byte mi = slea.readByte();
            MapleInventory type = chr.getInventory(MapleInventoryType.getByType(mi));
            Item item = type.findByUniqueId(uniqueid);
            if (item != null && item.getQuantity() > 0 && item.getUniqueId() > 0 && c.getPlayer().getCashInventory().getItemsSize() < 100) {
                //Item item_ = item.copy();
                //MapleInventoryManipulator.removeFromSlot(c, type, item.getPosition(), item.getQuantity(), false);
                sn = CashItemFactory.getInstance().getItemSN(item.getItemId());
                type.removeSlot(item.getPosition());
                c.getPlayer().getCashInventory().addToInventory(item);
                c.getSession().write(CSPacket.confirmToCSInventory(item, c.getAccID(), sn));
            } else {
                //c.getSession().write(CSPacket.sendCSFail(0xB1));
                chr.dropMessage(1, "移动失败。");
            }
        } else if (action == 0x19) {
            int toCharge = 2;//抵用卷
            long uniqueId = (int) slea.readLong();
            Item item = c.getPlayer().getCashInventory().findByCashId((int) uniqueId);

            if (item == null) {
                c.getSession().write(CSPacket.showNXMapleTokens(chr));
                return;
            }
            int sn = CashItemFactory.getInstance().getItemSN(item.getItemId());
            CashItemInfo cItem = CashItemFactory.getInstance().getItem((int) sn);

            if (!MapleItemInformationProvider.getInstance().isCash(cItem.getId())) {
                AutobanManager.getInstance().autoban(chr.getClient(), "商城非法换购道具.");
                return;
            }
            int Money = cItem.getPrice() / 10 * 3;
            c.getPlayer().getCashInventory().removeFromInventory(item);
            chr.modifyCSPoints(toCharge, Money, false);
            chr.dropMessage(1, "成功换购抵用券" + Money + "点.");
            doCSPackets(c);
        } else if (action == 28 || action == 35) { //36 = friendship, 30 = crush
            final int toCharge = 1;
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            final String partnerName = slea.readMapleAsciiString();
            final String msg = slea.readMapleAsciiString();
            System.out.println("购买事件ID: " + action + " 物品代码: " + item.getId() + " 物品SN:" + item.getSN());
            if (item == null || !GameConstants.isEffectRing(item.getId()) || c.getPlayer().getCSPoints(toCharge) < item.getPrice() || msg.length() > 73 || msg.length() < 1) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (!item.genderEquals(c.getPlayer().getGender())) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            for (int i : MTSCSConstants.cashBlock) { //just incase hacker
                if (item.getId() == i) {
                    c.getPlayer().dropMessage(1, MTSCSConstants.getCashBlockedMsg(item.getId()));
                    doCSPackets(c);
                    return;
                }
            }
            Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if (info == null || info.getLeft() <= 0 || info.getLeft() == c.getPlayer().getId()) {
                c.getSession().write(CSPacket.sendCSFail(0)); //9E v75
                doCSPackets(c);
                return;
            } else if (info.getMid() == c.getAccID()) {
                c.getSession().write(CSPacket.sendCSFail(0)); //9D v75
                doCSPackets(c);
                return;
            } else {
                if (info.getRight() == c.getPlayer().getGender() && action == 28) {
                    c.getSession().write(CSPacket.sendCSFail(0)); //9B v75
                    doCSPackets(c);
                    return;
                }
                int err = MapleRing.createRing(item.getId(), c.getPlayer(), partnerName, msg, info.getLeft(), item.getSN());
                if (err != 1) {
                    c.getSession().write(CSPacket.sendCSFail(0)); //9E v75
                    doCSPackets(c);
                    return;
                }
                c.getPlayer().modifyCSPoints(toCharge, -item.getPrice(), false);
                c.getSession().write(CSPacket.sendGift(item.getPrice(), item.getId(), item.getCount(), partnerName));
            }
        } else if (action == 30 || action == 99) {
            final int toCharge = slea.readByte() + 1;
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (item != null) {
                System.out.println("购买事件ID: " + action + " 物品SN:" + item.getSN());
            }
            List<Integer> ccc = null;
            if (item != null) {
                ccc = CashItemFactory.getInstance().getPackageItems(item.getId());
            }
            if (item == null || ccc == null || c.getPlayer().getCSPoints(toCharge) < item.getPrice()) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (!item.genderEquals(c.getPlayer().getGender())) {
                c.getSession().write(CSPacket.sendCSFail(0xA6));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getCashInventory().getItemsSize() >= (100 - ccc.size())) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }
            for (int iz : MTSCSConstants.cashBlock) {
                if (item.getId() == iz) {
                    c.getPlayer().dropMessage(1, MTSCSConstants.getCashBlockedMsg(item.getId()));
                    doCSPackets(c);
                    return;
                }
            }
            Item itemz = null;
            Map<Integer, Item> ccz = new HashMap<>();
            for (int i : ccc) {
                final CashItemInfo cii = CashItemFactory.getInstance().getSimpleItem(i);
                if (cii == null) {
                    continue;
                }
                itemz = c.getPlayer().getCashInventory().toItem(cii);
                if (itemz == null || itemz.getUniqueId() <= 0) {
                    continue;
                }
                for (int iz : MTSCSConstants.cashBlock) {
                    if (itemz.getItemId() == iz) {
                        continue;
                    }
                }
                ccz.put(i, itemz);
                c.getPlayer().getCashInventory().addToInventory(itemz);
            }
            chr.modifyCSPoints(toCharge, -item.getPrice(), false);
            //c.getSession().write(CSPacket.showBoughtCSPackage(ccz, c.getAccID()));
            c.getSession().write(CSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
        } else if (action == 32) {
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (item == null || !MapleItemInformationProvider.getInstance().isQuestItem(item.getId())) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getMeso() < item.getPrice()) {
                c.getSession().write(CSPacket.sendCSFail(0xB8));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getInventory(GameConstants.getInventoryType(item.getId())).getNextFreeSlot() < 0) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }
            for (int iz : MTSCSConstants.cashBlock) {
                if (item.getId() == iz) {
                    c.getPlayer().dropMessage(1, MTSCSConstants.getCashBlockedMsg(item.getId()));
                    doCSPackets(c);
                    return;
                }
            }
            byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null, "Cash shop: quest item" + " on " + FileoutputUtil.CurrentReadable_Date());
            if (pos < 0) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }
            chr.gainMeso(-item.getPrice(), false);
            chr.dropMessage(1, "购买成功。");
            c.getSession().write(CSPacket.showBoughtCSQuestItem(item.getPrice(), (short) item.getCount(), pos, item.getId()));
        } else if (action == 41) { //idk
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (item == null || c.getPlayer().getCSPoints(1) < item.getPrice()) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            c.getPlayer().modifyCSPoints(2, item.getPrice(), false);
            chr.dropMessage(1, "成功购买抵用券:" + item.getPrice());
            c.getPlayer().modifyCSPoints(1, -item.getPrice(), false);
        } else if (action == (GameConstants.GMS ? 46 : 45)) { //idk
            c.getSession().write(CSPacket.redeemResponse());
        } else {
            c.getSession().write(CSPacket.sendCSFail(0));
        }
        doCSPackets(c);
    }

    private static final MapleInventoryType getInventoryType(final int id) {
        switch (id) {
            case 50200018:
                return MapleInventoryType.EQUIP;
            case 50200019:
                return MapleInventoryType.USE;
            case 50200020:
                return MapleInventoryType.SETUP;
            case 50200021:
                return MapleInventoryType.ETC;
            default:
                System.out.println("未处理的 InventoryType : " + id);
                return MapleInventoryType.UNDEFINED;
        }
    }

    public static final void doCSPackets(MapleClient c) {
        c.getSession().write(CSPacket.getCSInventory(c));
        c.getSession().write(CSPacket.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(CSPacket.enableCSUse());
        c.getPlayer().getCashInventory().checkExpire(c);
    }
}
