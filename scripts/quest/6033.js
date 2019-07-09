var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    if (qm.getQuestStatus(6033) != 1 && qm.getQuestStatus(6033) != 2) {
        qm.forceStartQuest(6033);
        qm.dispose();
        return;
    }
    if (qm.getQuestStatus(6033) == 1) {
        if (qm.haveItem(4260003)) {
            qm.gainItem(4260003, -1);
            qm.forceCompleteQuest(6033);
            qm.teachSkill(1007, 2);
            qm.dispose();
            return;
        } else {
            cm.sendOk("#b你没有中等怪物结晶C。~");
            qm.dispose();
            return;
        }
    }

    qm.dispose();
}
