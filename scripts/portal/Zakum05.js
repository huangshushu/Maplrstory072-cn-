/*
    Zakum Entrance
*/

function enter(pi) {
    if (pi.getQuestStatus(100200) != 2) {
	pi.playerMessage(5, "任务完成之前，你可能无法面对扎昆！");
	return false;

    } else if (!pi.haveItem(4001017)) {
	pi.playerMessage(5, "里面已经开始了对抗扎昆的战斗，你无法进去！");
	return false;
    }
    
    pi.playPortalSE();
    pi.warp(pi.getPlayer().getMapId() + 100, "west00");
    return true;
}