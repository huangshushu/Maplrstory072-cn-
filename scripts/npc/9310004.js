function start() {
	if ((cm.getQuestStatus(4103) == 1 && cm.haveItem(4031289 ,1)) || cm.getQuestStatus(8510) == 2) {
		cm.warp(701010321);
		cm.dispose();
		} else {
		cm.sendOk("要么你已经完成了#r警察#k所交付你的任务，或还没完成#b农民伯伯#k的拜托。所以我不能带你进去！");
		cm.dispose();
	}
}