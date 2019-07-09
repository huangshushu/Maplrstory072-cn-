function enter(pi) {
try {
    if (pi.haveItem(4031346)) {
	if (pi.getMapId() == 240010100) {
	    pi.playPortalSE();
	    pi.warp(101010000, "minar00");
	} else {
	    pi.playPortalSE();
	    pi.warp(240010100, "elli00");
	}
	pi.gainItem(4031346, -1);
	pi.playerMessage("因为魔法种子的力量，被传送到未知区域。");
	return true;
    } else {
	pi.playerMessage("没有魔法种子来启动传送阵。");
	return false;
    }
} catch (e) {
    pi.playerMessage("Error: " + e);
}
}
