package server;

import client.MapleCharacter;
import constants.GameConstants;
import handling.world.MaplePartyCharacter;
import java.lang.ref.WeakReference;

public class MapleCarnivalChallenge {

    WeakReference<MapleCharacter> challenger;
    String challengeinfo = "";

    public MapleCarnivalChallenge(MapleCharacter challenger) {
        this.challenger = new WeakReference<MapleCharacter>(challenger);
        challengeinfo += "#b";
        for (MaplePartyCharacter pc : challenger.getParty().getMembers()) {
            MapleCharacter c = challenger.getMap().getCharacterById(pc.getId());
            if (c != null) {
                challengeinfo += (c.getName() + " / " + c.getLevel() + "级 / " + getJobNameById(c.getJob()));
            }
        }
        challengeinfo += "#k";
    }

    public MapleCharacter getChallenger() {
        return challenger.get();
    }

    public String getChallengeInfo() {
        return challengeinfo;
    }

    public static final String getJobNameById(int job) {
        switch (job) {
            case 0:
            case 1:
                return "新手";
            case 1000:
                return "Nobless";
            case 2000:
                return "Legend";
            case 2001:
                return "Evan";
            case 3000:
                return "Citizen";

            case 100:
                return "战士";// Warrior
            case 110:
                return "剑客";
            case 111:
                return "勇士";
            case 112:
                return "英雄";
            case 120:
                return "准骑士";
            case 121:
                return "骑士";
            case 122:
                return "圣骑士";
            case 130:
                return "枪战士";
            case 131:
                return "龙骑士";
            case 132:
                return "黑骑士";

            case 200:
                return "魔法师";
            case 210:
                return "法师(火,毒)";
            case 211:
                return "巫师(火,毒)";
            case 212:
                return "魔导师(火,毒)";
            case 220:
                return "法师(冰,雷)";
            case 221:
                return "巫师(冰,雷)";
            case 222:
                return "魔导师(冰,雷)";
            case 230:
                return "牧师";
            case 231:
                return "祭司";
            case 232:
                return "主角";

            case 300:
                return "弓箭手";
            case 310:
                return "猎人";
            case 311:
                return "射手";
            case 312:
                return "神射手";
            case 320:
                return "弩弓手";
            case 321:
                return "游侠";
            case 322:
                return "箭神";

            case 400:
                return "飞侠";
            case 410:
                return "刺客";
            case 411:
                return "无影人";
            case 412:
                return "隐士";
            case 420:
                return "侠客";
            case 421:
                return "独行客";
            case 422:
                return "侠盗";
            case 430:
                return "Blade Recruit";
            case 431:
                return "Blade Acolyte";
            case 432:
                return "Blade Specialist";
            case 433:
                return "Blade Lord";
            case 434:
                return "Blade Master";

            case 500:
                return "海盗";
            case 510:
                return "拳手";
            case 511:
                return "斗士";
            case 512:
                return "冲锋队长";
            case 520:
                return "火枪手";
            case 521:
                return "大副";
            case 522:
                return "船长";
            case 501:
                return "Pirate (Cannoneer)";
            case 530:
                return "Cannoneer";
            case 531:
                return "Cannon Blaster";
            case 532:
                return "Cannon Master";

            case 1100:
            case 1110:
            case 1111:
            case 1112:
                return "Soul Master";

            case 1200:
            case 1210:
            case 1211:
            case 1212:
                return "Flame Wizard";

            case 1300:
            case 1310:
            case 1311:
            case 1312:
                return "Wind Breaker";

            case 1400:
            case 1410:
            case 1411:
            case 1412:
                return "Night Walker";

            case 1500:
            case 1510:
            case 1511:
            case 1512:
                return "Striker";

            case 2100:
            case 2110:
            case 2111:
            case 2112:
                return "Aran";

            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
                return "Evan";

            case 2002:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
                return "Mercedes";

            case 3001:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                return "Demon Slayer";

            case 3200:
            case 3210:
            case 3211:
            case 3212:
                return "Battle Mage";

            case 3300:
            case 3310:
            case 3311:
            case 3312:
                return "Wild Hunter";

            case 3500:
            case 3510:
            case 3511:
            case 3512:
                return "Mechanic";

            case 900:
                return "管理员";
            case 910:
                return "SuperGM";
            case 800:
                return "管理者";

            default:
                return "";
        }
    }

    public static final String getJobBasicNameById(int job) {
        switch (job) {
            case 0:
            case 1:
            case 1000:
            case 2000:
            case 2001:
            case 2002:
            case 3000:
            case 3001:
                return "新手";

            case 3100:
            case 3110:
            case 3111:
            case 3112:
            case 2100:
            case 2110:
            case 2111:
            case 2112:
            case 1100:
            case 1110:
            case 1111:
            case 1112:
                return "战士";

            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
            case 3200:
            case 3210:
            case 3211:
            case 3212:
            case 1200:
            case 1210:
            case 1211:
            case 1212:
                return "魔法师";

            case 3300:
            case 3310:
            case 3311:
            case 3312:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
            case 1300:
            case 1310:
            case 1311:
            case 1312:
                return "弓箭手";

            case 1400:
            case 1410:
            case 1411:
            case 1412:
            case 430:
            case 431:
            case 432:
            case 433:
            case 434:
                return "飞侠";

            case 3500:
            case 3510:
            case 3511:
            case 3512:
            case 1500:
            case 1510:
            case 1511:
            case 1512:
            case 501:
            case 530:
            case 531:
            case 532:
                return "海盗";

            case 100:
                return "战士";
            case 110:
                return "剑客";
            case 111:
                return "勇士";
            case 112:
                return "英雄";
            case 120:
                return "准骑士";
            case 121:
                return "骑士";
            case 122:
                return "圣骑士";
            case 130:
                return "枪战士";
            case 131:
                return "龙骑士";
            case 132:
                return "黑骑士";
            case 200:
                return "魔法师";
            case 210:
                return "法师（火，毒）";
            case 211:
                return "巫师（火，毒）";
            case 212:
                return "魔导师（火，毒）";
            case 220:
                return "法师（冰，雷）";
            case 221:
                return "巫师（冰，雷）";
            case 222:
                return "魔导师（冰，雷）";
            case 230:
                return "牧师";
            case 231:
                return "祭司";
            case 232:
                return "主教";
            case 300:
                return "弓箭手";
            case 310:
                return "猎手";
            case 311:
                return "射手";
            case 312:
                return "神射手";
            case 320:
                return "弩弓手";
            case 321:
                return "游侠";
            case 322:
                return "箭神";
            case 400:
                return "飞侠";
            case 410:
                return "刺客";
            case 411:
                return "无影人";
            case 412:
                return "隐士";
            case 420:
                return "侠客";
            case 421:
                return "独行客";
            case 422:
                return "侠盗";
            case 500:
                return "海盗";
            case 510:
                return "拳手";
            case 511:
                return "斗士";
            case 512:
                return "冲锋队长";
            case 520:
                return "火枪手";
            case 521:
                return "大副";
            case 522:
                return "船长";
            case 800:
                return "管理者";
            case 900:
                return "管理员";
            default:
                return "";
        }
    }
}
