var status = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0) {
        cm.removeAll(4001117);
        cm.removeAll(4001120);
        cm.removeAll(4001121);
        cm.removeAll(4001122);
        cm.sendSimple("海贼王的帽子可以通过战利品持续升级哦。\r\n#b#L0#我要离开这里了。#l\r\n#L1#兑换海贼王的帽子。#l#k");
    } else if (status == 1) {
        if (selection == 0) {
            cm.gainItem(4001123, 1);
            cm.gainHdExp_PQ();
            cm.getPlayer().endPartyQuest(1204);
            cm.warp(251010404, 0);
        } else { //TODO JUMP
            if (cm.haveItem(1002574, 1)) {
                cm.sendOk("你已经拥有海贼王的帽子。");
            } else if (cm.haveItem(1002573, 1)) {
                if (cm.haveItem(4001123, 20)) {
                    if (cm.canHold(1002574, 1)) {
                        cm.gainItem(1002573, -1);
                        cm.gainItem(4001123, -20);
                        cm.gainItem(1002574, 1);
                        cm.sendOk("给，这是海贼王的帽子。");
                    } else {
                        cm.sendOk("请检查一下你的背包是否有空格。");
                    }
                } else {
                    cm.sendOk("你没有20个战利品。");
                }
            } else if (cm.haveItem(1002572, 1)) {
                if (cm.haveItem(4001123, 20)) {
                    if (cm.canHold(1002573, 1)) {
                        cm.gainItem(1002572, -1);
                        cm.gainItem(4001123, -20);
                        cm.gainItem(1002573, 1);
                        cm.sendOk("给，这是海贼王的帽子。");
                    } else {
                        cm.sendOk("请检查一下你的背包是否有空格。");
                    }
                } else {
                    cm.sendOk("你没有20个战利品。");
                }
            } else if (cm.haveItem(1002571, 1)) {
                if (cm.haveItem(4001123, 20)) {
                    if (cm.canHold(1002572, 1)) {
                        cm.gainItem(1002571, -1);
                        cm.gainItem(4001123, -20);
                        cm.gainItem(1002572, 1);
                        cm.sendOk("给，这是海贼王的帽子。");
                    } else {
                        cm.sendOk("请检查一下你的背包是否有空格。");
                    }
                } else {
                    cm.sendOk("你没有20个战利品。");
                }
            } else {
                if (cm.haveItem(4001123, 20)) {
                    if (cm.canHold(1002571, 1)) {
                        cm.gainItem(4001123, -20);
                        cm.gainItem(1002571, 1);
                        cm.sendOk("给，这是海贼王的帽子。");
                    } else {
                        cm.sendOk("请检查一下你的背包是否有空格。");
                    }
                } else {
                    cm.sendOk("你没有20个战利品。");
                }
            }
        }
        cm.dispose();
    }
}