var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    if (qm.getMeso() > 10000) {
        qm.gainMeso(-10000);
        qm.forceCompleteQuest();
        qm.forceStartQuest(6031);
        qm.sendNext("在玛加提亚的蒙特鸠协会会长卡森那里学习了无聊的炼金术课程。啊，终于结束了！");
    } else {
        qm.sendNext("你没有10000金币,我无法教导你炼金术课程。");
    }
    qm.dispose();
}
