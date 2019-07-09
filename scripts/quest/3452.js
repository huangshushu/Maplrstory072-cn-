var status = -1;

function start(mode, type, selection) {
    if (qm.getQuestStatus(3452) == 1) {
        status++;
        if (status == 0) {
            qm.sendNext("非常感谢你的帮助，这是给你的奖励。\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000011# 50个 #t2000011#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#  8000 经验");
        } else {
            qm.gainItem(4000099, -1);
            qm.gainItem(2000011, 50);
            qm.givePartyExp(8000);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    } else {
        qm.forceStartQuest();
        qm.dispose();
    }
}
function end(mode, type, selection) {
    if (qm.getQuestStatus(3452) == 1) {
        status++;
        if (status == 0) {
            qm.sendNext("非常感谢你的帮助，这是给你的奖励。\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000011# 50个 #t2000011#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#  8000 经验");
        } else {
            qm.gainItem(4000099, -1);
            qm.gainItem(2000011, 50);
            qm.givePartyExp(8000);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    } else {
        qm.forceStartQuest();
        qm.dispose();
    }
}
