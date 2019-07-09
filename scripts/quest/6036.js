var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
        if (qm.haveItem(4031980)) {
            qm.gainItem(4031980, -1);
            qm.forceCompleteQuest(6036);
            qm.teachSkill(1007, 3);
            qm.dispose();
            return;
        } else {
            cm.sendOk("#b你没有黄金砧子。~");
            qm.dispose();
            return;
        }
    
}
