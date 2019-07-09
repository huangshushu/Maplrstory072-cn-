package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import java.util.List;
import server.shops.AbstractPlayerStore.BoughtItem;
import tools.Pair;

public interface IMaplePlayerShop {
    public final static byte HIRED_MERCHANT = 1;
    public final static byte PLAYER_SHOP = 2;
    public final static byte OMOK = 3;
    public final static byte MATCH_CARD = 4;

    public String getOwnerName();
    public String getDescription();
    public String getPassword();

    public List<Pair<Byte, MapleCharacter>> getVisitors();
    public List<MaplePlayerShopItem> getItems();
    public List<BoughtItem> getBoughtItems();
    public List<Pair<String, Byte>> getMessages();

    public boolean isOpen();
    public boolean removeItem(int item);
    public boolean isOwner(MapleCharacter chr);
    public boolean isAvailable();

    public byte getShopType();
    public byte getVisitorSlot(MapleCharacter visitor);
    public byte getFreeSlot();

    public int getItemId();
    public int getMeso();
    public int getOwnerId();
    public int getOwnerAccId();
    public int getMaxSize();
    public int getSize();
    public int getGameType();
    public int getItemType();

    public void setOpen(boolean open);
    public void setMeso(int meso);
    public void addItem(MaplePlayerShopItem item);
    public void removeFromSlot(int slot);
    public void broadcastToVisitors(byte[] packet);
    public void addVisitor(MapleCharacter visitor);
    public void removeVisitor(MapleCharacter visitor);
    public void removeAllVisitors(int error, int type);
    public void buy(MapleClient c, int item, short quantity);
    public void closeShop(boolean saveItems, boolean remove);
    public void update();
    public void setAvailable(boolean b);
}
