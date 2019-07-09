var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    if (qm.getMeso() > 10000) {
        qm.gainMeso(-10000);
        qm.forceCompleteQuest();
        //qm.forceStartQuest(6032);
        qm.forceCompleteQuest(6029);
        qm.teachSkill(1007, 1);
        qm.sendNext("听取了威尔斯的技术课程。还是技术课程比较有意思！");
    } else {
        qm.sendNext("你没有10000金币,我无法教导你炼金术课程。");
    }
    qm.dispose();
}
