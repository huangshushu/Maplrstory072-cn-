var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            qm.sendNext("是否已经准备好前往沙漠了??");
        } else if (status == 1) {
            qm.sendAcceptDecline("别怪我说没有警告你...");
        } else if (status == 2) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (qm.getPlayerStat("HP") < 50) {
                qm.sendNext("我看你好像还没准备好，准备好再来找我吧。");
                qm.dispose();
            } else {
                qm.sendNext("是否已经准备好前往沙漠了??");
            }

        } else if (status == 1) {
            qm.sendNextPrev("我会想你的。");
        } else if (status == 2) {
            qm.warp(260000200);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}