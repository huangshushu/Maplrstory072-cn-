var status = -1;

function start(mode, type, selection) {
    if (qm.getQuestStatus(4647) == 1) {
        status++;
        if (status == 0) {
            qm.sendNext("现在我就赋予你群宠技能。");
        } else {
            qm.gainItem(5460000, -1);
            qm.teachSkill(8, 1, 1);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    } else {
        qm.forceStartQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (qm.getQuestStatus(4647) == 1) {
        status++;
        if (status == 0) {
            qm.sendNext("现在我就赋予你群宠技能。");
        } else {
            qm.gainItem(5460000, -1);
            qm.teachSkill(8, 1, 1);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    } else {
        qm.forceStartQuest();
        qm.dispose();
    }
}