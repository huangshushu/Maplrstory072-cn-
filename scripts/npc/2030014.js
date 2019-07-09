var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	}
	status--;
    }
    if (status == 0) {
	cm.sendYesNo("你想做什么，别来打扰我！");
    } else if (status == 1) {
        cm.gainItem(2280011,1);
	cm.warp(211040200);
	cm.dispose();
    }
}