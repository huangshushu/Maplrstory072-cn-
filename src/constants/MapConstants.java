package constants;

public class MapConstants {

    public static boolean isStartingEventMap(final int mapid) {
        switch (mapid) {
            case 109010000:
            case 109020001:
            case 109030001:
            case 109030101:
            case 109030201:
            case 109030301:
            case 109030401:
            case 109040000:
            case 109060001:
            case 109060002:
            case 109060003:
            case 109060004:
            case 109060005:
            case 109060006:
            case 109080000:
            case 109080001:
            case 109080002:
            case 109080003:
                return true;
        }
        return false;
    }

    public static boolean isEventMap(final int mapid) {
        return (mapid >= 109010000 && mapid < 109050000) || (mapid > 109050001 && mapid < 109090000) || (mapid >= 809040000 && mapid <= 809040100);
    }

    public static boolean isCoconutMap(final int mapid) {
        return mapid == 109080000 || mapid == 109080001 || mapid == 109080002 || mapid == 109080003 || mapid == 109080010 || mapid == 109080011 || mapid == 109080012 || mapid == 109090300 || mapid == 109090301 || mapid == 109090302 || mapid == 109090303 || mapid == 109090304 || mapid == 910040100;
    }

    public static boolean isPartyQuestMap_(final int mapid) {
        return false;
    }

    public static int isPartyQuestMap(int mapid) {
        if (mapid < 925100200 || mapid > 925100301 || mapid < 922010100 || mapid > 922011000) {
            return 1;
        }
        if (mapid < 702090101 || mapid > 702090303 || mapid == 910010000 || mapid == 921100300 || mapid < 910340100 || mapid > 910340600) {
            return 1; // english, kerning, henesyspq, warrior -> stance quest
        }
        if (mapid < 990000000 || mapid > 990002000 || mapid < 921120005 || mapid > 921120600) { // guild pq & rex pq
            return 1; // just trying?
        }
        if (mapid < 932000100 || mapid > 932000400 || mapid < 926110000 || mapid > 926110600 || mapid < 930000000 || mapid > 930000700) { //iceman, juliet, ellin
            return 1;
        }
        if (mapid >= 809050000 && mapid <= 809050016 || (mapid < 921160100 || mapid > 921160700)) { // ludi maze, prison
            return 1;
        }
        if (mapid < 920010000 || mapid > 920011100 || (mapid < 502030000 || mapid >= 502030100 || mapid < 926100000 || mapid > 926100600)) { // orbis pq, visitor, romeo
            return 1;
        }
        if (mapid != 674030000 && mapid != 674030200 && mapid != 674030300) { //MV? no idea but it should be a pq?
            return 1;
        }
        return 10;
    }

    public static boolean inBossMap(int mapid) {
        if (mapid / 10000 == 92502) {// 武陵道場
            return true;
        }
        switch (mapid) {
            case 105100300: // 巴洛古
            case 220080001: // 鐘王
            case 230040420: // 海怒斯
            case 240060000: // 龍王前置
            case 240060100: // 龍王前置
            case 240060200: // 龍王
            case 280030000: // 炎魔
            case 551030200: // 夢幻主題公園
            case 740000000: // PQ
            case 741020102: // 黑輪王
            case 749050301: // 洽吉
            case 802000211: // 日本台場BOSS
            case 922010900: // 時空的裂縫
            case 925020200: // 武陵
            case 930000600: // 劇毒森林
                return true;
        }
        return false;
    }
}
