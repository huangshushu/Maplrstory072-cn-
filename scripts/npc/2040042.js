/*
 Sky-Blue Balloon - LudiPQ 7th stage NPC
 **/

var status;
var exp = 4620;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var eim = cm.getEventInstance();
    var stage7status = eim.getProperty("stage7status");

    if (stage7status == null) {
        if (cm.isLeader()) { // Leader
            var stage7leader = eim.getProperty("stage7leader");
            if (stage7leader == "done") {

                if (cm.getMap().getAllMonstersThreadsafe().size() == 0) { // Clear stage
                    if (cm.haveItem(4001022, 3)) { // Clear stage
                        cm.sendNext("恭喜！你已经通过了第七阶段。快点现在，去第8阶段。");
                        cm.removeAll(4001022);
                        clear(7, eim, cm);
                        cm.givePartyExp(exp, eim.getPlayers());
                        cm.dispose();
                    } else { // Not done yet
                        cm.sendNext("你确定你有收集了#r3张#t4001022##k？");
                    }
                } else { // Not done yet
                    cm.sendNext("当前地图还有怪物。");
                }
                cm.dispose();
            } else {
                cm.sendOk("欢迎来到第七阶段。#b废弃的塔#k 请收集#r#t4001022##k来找我即可完成任务。");
                eim.setProperty("stage7leader", "done");
                cm.dispose();
            }
        } else { // Members
            cm.sendNext("欢迎来到第七阶段。#b废弃的塔#k请收集#r#t4001022##k给你的队长，然后叫队长来找我即可完成任务。");
            cm.dispose();
        }
    } else {
        cm.sendNext("恭喜！你已经通过了第七阶段。快点现在，去第8阶段。");
        cm.dispose();
    }
}

function clear(stage, eim, cm) {
    eim.setProperty("stage" + stage.toString() + "status", "clear");

    cm.showEffect(true, "quest/party/clear");
    cm.playSound(true, "Party1/Clear");
    cm.environmentChange(true, "gate");
}