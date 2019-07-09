package server;

import database.DBConPool;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.CashItemInfo.CashModInfo;
import tools.FileoutputUtil;

public class CashItemFactory {

    private final static CashItemFactory instance = new CashItemFactory();
    private final static int[] bestItems = new int[]{50300000, 50400016, 50400048, 50500042, 30200008};
    private final Map<Integer, CashItemInfo> itemStats = new HashMap<>();
    private final Map<Integer, List<Integer>> itemPackage = new HashMap<>();
    private final Map<Integer, CashModInfo> itemMods = new HashMap<>();
    private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Etc.wz"));

    public static final CashItemFactory getInstance() {
        return instance;
    }

    public void initialize() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<MapleData> cccc = data.getData("Commodity.img").getChildren();
        for (MapleData field : cccc) {
            final int SN = MapleDataTool.getIntConvert("SN", field, 0);

            final CashItemInfo stats = new CashItemInfo(MapleDataTool.getIntConvert("ItemId", field, 0),
                    MapleDataTool.getIntConvert("Count", field, 1),
                    MapleDataTool.getIntConvert("Price", field, 0), SN,
                    MapleDataTool.getIntConvert("Period", field, 0),
                    MapleDataTool.getIntConvert("Gender", field, 2),
                    MapleDataTool.getIntConvert("OnSale", field, 0) > 0 && MapleDataTool.getIntConvert("Price", field, 0) > 0);

            if (SN > 0) {
                itemStats.put(SN, stats);
            }
        }
        final MapleData b = data.getData("CashPackage.img");
        for (MapleData c : b.getChildren()) {
            if (c.getChildByPath("SN") == null) {
                continue;
            }
            final List<Integer> packageItems = new ArrayList<Integer>();
            for (MapleData d : c.getChildByPath("SN").getChildren()) {
                packageItems.add(MapleDataTool.getIntConvert(d));
            }
            itemPackage.put(Integer.parseInt(c.getName()), packageItems);
        }

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_modified_items")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {

                    Boolean showUp = rs.getInt("showup") > 0;
                    int sn = rs.getInt("serial");
                    int item = rs.getInt("itemid");
                    CashModInfo ret = new CashModInfo(sn, rs.getInt("discount_price"), rs.getInt("mark"), showUp, item, rs.getInt("priority"), rs.getInt("package") > 0, rs.getInt("period"), rs.getInt("gender"), rs.getInt("count"), rs.getInt("meso"), rs.getInt("unk_1"), rs.getInt("unk_2"), rs.getInt("unk_3"), rs.getInt("extra_flags"));
                    if (sn < 70000000) {
                        if (!ii.itemExists(item)) {
                            continue;
                        }
                    }
                    itemMods.put(ret.sn, ret);
                    //if (ret.showUp) {
                    if (showUp) {
                        final CashItemInfo cc = itemStats.get(ret.sn);
                        if (cc != null) {
                            ret.toCItem(cc); //init
                        }
                    }
                }
                rs.close();
            }
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("logs/数据库异常.txt", e);
        }
    }

    public final CashItemInfo getSimpleItem(int sn) {
        return itemStats.get(sn);
    }

    public final CashItemInfo getItem(int sn) {
        final CashItemInfo stats = itemStats.get(Integer.valueOf(sn));
        final CashModInfo z = getModInfo(sn);
        if (z != null && z.showUp) {
            return z.toCItem(stats); //null doesnt matter
        }
        if (stats == null || !stats.onSale()) {
            return null;
        }
        //hmm
        return stats;
    }

    public final List<Integer> getPackageItems(int itemId) {
        return itemPackage.get(itemId);
    }

    public final CashModInfo getModInfo(int sn) {
        return itemMods.get(sn);
    }

    public final Collection<CashModInfo> getAllModInfo() {
        return itemMods.values();
    }

    public final int[] getBestItems() {
        return bestItems;
    }

    public final int getItemSN(int itemid) {
        for (Entry<Integer, CashItemInfo> ci : itemStats.entrySet()) {
            if (ci.getValue().getId() == itemid) {
                return ci.getValue().getSN();
            }
        }
        return 0;
    }
}
