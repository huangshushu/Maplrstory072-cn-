/* Dawnveil
 World Tour Guide
 Spinel
 Made by Daenerys
 */
var status = -1;
var sel;
var map;
var back = true;
var togos = [500000000, 702000000, 800000000];
var maps = [104020000, 200000000, 211000000, 220000000, 221000000, 222000000, 230000000, 240000000, 250000000, 251000000, 260000000, 261000000, 270000000];

function start() {
    switch (cm.getMapId()) {
        case 500000000:
        case 702000000:
        case 800000000:    
            map = cm.getSavedLocation("WORLDTOUR");
            cm.sendSimple("还满意冒险游程吗? \n\r #b#L0# 已经游览了，想回到#m" + map + "#。#l");
            break;
        default:
            back = false;
            cm.sendNext("为了从繁忙的日常中解脱，去享受一趟旅游怎么样？不仅可以体检新颖的异国文化，还能学到不少东西的机会！我们冒险岛旅游公司为您准备了，丰富有趣的#b世界旅游#k套餐。谁说环游世界很贵？请放一万个心。我们#b冒险岛世界旅游套餐#k只需要#b3000金币#k就可以享乐全程。");
            break;
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if ((status <= 2 && mode == 0) || (status == 4 && mode == 1)) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if (!back) {
            if (status == 0) {
                cm.sendSimple("现在就可以去往 #b世界各地充满冒险岛特色的景点#k游览一番。在每个旅游地我都会为大家提供满意热诚的服务。那么请准备好，我们出发去\r\n#L0# 水上市场（泰国）#l\r\n#L1# 嵩山镇（少林）#l\r\n#L2# 古代神社（日本）#l");
            } else if (status == 1) {
                sel = selection;
                cm.sendNext("这是个不错的地方，在那里玩肯定很开心的。");
            } else if (status == 2) {
                if (cm.getMeso() < 3000) {
                    cm.sendNext("请确认你是否带有足够的盘缠。");
                    cm.dispose();
                } else {
                    cm.gainMeso(-3000);
                    cm.saveLocation("WORLDTOUR");
                    cm.warp(togos[sel]);
                    cm.dispose();
                }
            }
        } else {
            if (sel == 123456) {
                cm.warp(maps[selection]);
                cm.dispose();
            } else {
                cm.warp(map == -1 ? 100000000 : map);
                cm.clearSavedLocation("WORLDTOUR");
                cm.dispose();
            }
        }
    }
}