/*
Lord Jonathan - Nautilus' Port
*/

function start() {
    if (cm.getJob() == 522 && cm.getPlayerStat("LVL") >= 120) {
	if (!cm.hasSkill(5221003)) {
            cm.forceCompleteQuest(6400);
	    cm.teachSkill(5221003, 0, 10);
	}
    }
    cm.sendOk("你是谁，你在跟我说话？如果你只是无聊，去找別人！！");
}

function action(mode, type, selection) {
    cm.dispose();
}
