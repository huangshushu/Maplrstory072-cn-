var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    if (qm.getMeso() > 10000) {
        qm.gainMeso(-10000);
        qm.forceCompleteQuest();
        qm.forceStartQuest(6032);
        qm.sendNext("听完了秀兹乱糟糟的科学课程。啊，真让人打不起精神！还好，终于结束了！");
    } else {
        qm.sendNext("你没有10000金币,我无法教导你炼金术课程。");
    }
    qm.dispose();
}
