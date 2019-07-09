var status = -1;
var minLevel = 51; // 35
var maxLevel = 200; // 65

var minPartySize = 5;
var maxPartySize = 6;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
            cm.dispose();
            return;
        }
        status--;
    }
    if (cm.getMapId() == 920010000) { //inside orbis pq
        cm.sendOk("我们必须拯救他 需要20个云的碎片!");
        cm.dispose();
        return;
    }
    if (status == 0) {
        for (var i = 4001044; i < 4001064; i++) {
            cm.removeAll(i); //holy
        }
        if (cm.getParty() == null) { // No Party
            cm.sendSimple("你的组队不符合一下要求...:\r\n\r\n#r要求: " + minPartySize + " 组队成员, 所有成员等级必须在 " + minLevel + " 到等级 " + maxLevel + ".#b\r\n#L0#我要兑换女神手镯。#l");
        } else if (!cm.isLeader()) { // Not Party Leader
            cm.sendSimple("如果你想执行组队儿女无，请让 #b队长#k 来跟我交谈。#b\r\n#L0#我要兑换女神手镯。#l");
        } else {
            // Check if all party members are within PQ levels
            var party = cm.getParty().getMembers();
            var mapId = cm.getMapId();
            var next = true;
            var levelValid = 0;
            var inMap = 0;
            var it = party.iterator();

            while (it.hasNext()) {
                var cPlayer = it.next();
                if ((cPlayer.getLevel() >= minLevel) && (cPlayer.getLevel() <= maxLevel)) {
                    levelValid += 1;
                } else {
                    next = false;
                }
                if (cPlayer.getMapid() == mapId) {
                    inMap += (cPlayer.getJobId() == 900 ? 6 : 1);
                }
            }
            if (party.size() > maxPartySize || inMap < minPartySize) {
                next = false;
            }
            if (next) {
                var em = cm.getEventManager("OrbisPQ");
                if (em == null) {
                    cm.sendSimple("发生未知错误，请稍后再试。#b\r\n#L0#我要兑换女神手镯。#l");
                } else {
                    var prop = em.getProperty("state");
                    if (prop.equals("0") || prop == null) {
                        em.startInstance(cm.getParty(), cm.getMap(), 120);
                        cm.dispose();
                        return;
                    } else {
                        cm.sendSimple("其他队伍已经在进行#r组队任务#k了 请尝试更换频道或等待其他队伍完成。#b\r\n#L0#我要兑换女神手镯。#l");
                    }
                }
            } else {
                cm.sendSimple("你的组队不符合一下要求...:\r\n\r\n#r要求: " + minPartySize + " 组队成员, 所有成员等级必须在 " + minLevel + " 到等级 " + maxLevel + ".#b\r\n#L0#我要兑换女神手镯。#l");
            }
        }
    } else { //broken glass
        if (selection == 0) {
            if (!cm.canHold(1082232, 1)) {
                cm.sendOk("做好了。");
            } else if (cm.haveItem(4001158, 40)) {
                cm.gainItem(1082232, 1, true);
                cm.gainItem(4001158, -40, true);
            } else {
                cm.sendOk("你没有40个 #t4001158#.");
            }
        }
        cm.dispose();

    }
}