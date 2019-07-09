package handling.login;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Triple;

public class LoginInformationProvider {

    public enum JobType {
        UltimateAdventurer(-1, "Ultimate", 0, 130000000),
        Resistance(0, "Resistance", 3000, 931000000),
        Adventurer(1, "冒险家", 0, 0),
        Cygnus(2, "Premium", 1000, 913040000),
        Aran(3, "Orient", 2000, 914000000),
        Evan(4, "Evan", 2001, 900090000),
        Mercedes(5, "", 2002, 0),
        Demon(6, "", 3001, 0);

        public int type, id, map;
        public String job;

        private JobType(int type, String job, int id, int map) {
            this.type = type;
            this.job = job;
            this.id = id;
            this.map = map;
        }

        public static JobType getByJob(String g) {
            for (JobType e : JobType.values()) {
                if (e.job.length() > 0 && g.startsWith(e.job)) {
                    return e;
                }
            }
            return Adventurer;
        }

        public static JobType getByType(int g) {
            for (JobType e : JobType.values()) {
                if (e.type == g) {
                    return e;
                }
            }
            return Adventurer;
        }

        public static JobType getById(int g) {
            for (JobType e : JobType.values()) {
                if (e.id == g) {
                    return e;
                }
            }
            return Adventurer;
        }
    }
    private final static LoginInformationProvider instance = new LoginInformationProvider();
    protected final List<String> ForbiddenName = new ArrayList<String>();
    protected final Map<Triple<Integer, Integer, Integer>, List<Integer>> makeCharInfo = new HashMap<Triple<Integer, Integer, Integer>, List<Integer>>();

    public static LoginInformationProvider getInstance() {
        return instance;
    }

    protected LoginInformationProvider() {
        final MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Etc.wz"));
        MapleData nameData = prov.getData("ForbiddenName.img");
        for (final MapleData data : nameData.getChildren()) {
            ForbiddenName.add(MapleDataTool.getString(data));
        }
        nameData = prov.getData("Curse.img");
        for (final MapleData data : nameData.getChildren()) {
            ForbiddenName.add(MapleDataTool.getString(data).split(",")[0]);
        }
        final MapleData infoData = prov.getData("MakeCharInfo.img");
        final MapleData data = infoData.getChildByPath("Info");
        for (MapleData dat : infoData) {
            int val = -1;
            if (dat.getName().endsWith("Male")) {
                val = 0;
            } else if (dat.getName().endsWith("Female")) {
                val = 1;
            }
            final int job = JobType.getByJob(dat.getName()).type;
            for (MapleData da : dat) {
                final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(val, Integer.parseInt(da.getName()), job);
                List<Integer> our = makeCharInfo.get(key);
                if (our == null) {
                    our = new ArrayList<Integer>();
                    makeCharInfo.put(key, our);
                }
                for (MapleData d : da) {
                    our.add(MapleDataTool.getInt(d, -1));
                }
            }
        }
        for (MapleData dat : infoData) {
            try {
                final int type = JobType.getById(Integer.parseInt(dat.getName())).type;
                for (MapleData d : dat) {
                    int val;
                    if (d.getName().endsWith("male")) {
                        val = 0;
                    } else if (d.getName().endsWith("female")) {
                        val = 1;
                    } else {
                        continue;
                    }
                    for (MapleData da : d) {
                        final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(val, Integer.parseInt(da.getName()), type);
                        List<Integer> our = makeCharInfo.get(key);
                        if (our == null) {
                            our = new ArrayList<Integer>();
                            makeCharInfo.put(key, our);
                        }
                        for (MapleData dd : da) {
                            our.add(MapleDataTool.getInt(dd, -1));
                        }
                    }
                }
            } catch (NumberFormatException e) {
                //System.out.println("LoginInformationProvider : " + e);
            }
        }
        /*final MapleData uA = infoData.getChildByPath("UltimateAdventurer");
        for (MapleData dat : uA) {
            final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(-1, Integer.parseInt(dat.getName()), JobType.UltimateAdventurer.type);
            List<Integer> our = makeCharInfo.get(key);
            if (our == null) {
                our = new ArrayList<Integer>();
                makeCharInfo.put(key, our);
            }
            for (MapleData d : dat) {
                our.add(MapleDataTool.getInt(d, -1));
            }
        }*/
    }

    public final boolean isForbiddenName(final String in) {
        for (final String name : ForbiddenName) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public final boolean isEligibleItem(final int gender, final int val, final int job, final int item) {
        if (item < 0) {
            return false;
        }
        final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(gender, val, job);
        final List<Integer> our = makeCharInfo.get(key);
        if (our == null) {
            return false;
        }
        return our.contains(item);
    }

    public static boolean CheckCreate(byte gender, int face, int hair, int weapon, int top, int bottom, int shoes) {
        boolean pass = true;
        if (gender == 0) {
            if (face != 20100 && face != 20401 && face != 20402) {
                pass = false;
            } else if (hair != 30000 && hair != 30027 && hair != 30030) {
                pass = false;
            } else if (top != 1040002 && top != 1040006 && top != 1040010) {
                pass = false;
            } else if (bottom != 1060002 && bottom != 1060006) {
                pass = false;
            }
        } else if (gender == 1) {
            if (face != 21700 && face != 21201 && face != 21002) {
                pass = false;
            } else if (hair != 31002 && hair != 31047 && hair != 31057) {
                pass = false;
            } else if (top != 1041002 && top != 1041006 && top != 1041010 && top != 1041011) {
                pass = false;
            } else if (bottom != 1061002 && bottom != 1061008) {
                pass = false;
            }
        }
        if (shoes != 1072001 && shoes != 1072005 && shoes != 1072037 && shoes != 1072038) {
            pass = false;
        } else if (weapon != 1302000 && weapon != 1312004 && weapon != 1322005) {
            pass = false;
        }
        return pass;
    }
}
