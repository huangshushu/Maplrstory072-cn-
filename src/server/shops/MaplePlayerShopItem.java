package server.shops;

import client.inventory.Item;

public class MaplePlayerShopItem {
    public Item item;
    public short bundles;
    public int price;

    public MaplePlayerShopItem(Item item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
    }
}
