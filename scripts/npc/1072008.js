/**
	Author: xQuasar
	NPC: Kyrin - Pirate Job Advancer
	Inside Test Room
**/

var status;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode,type,selection) {
    if (status == -1) {
	if (cm.getMapId() == 108000500) {
	    if (!(cm.haveItem(4031857,15))) {
		cm.sendNext("快去收集 15个 #b强大的风力结晶#k 给我.");
		cm.dispose();
	    } else {
		status = 2;
		cm.sendNext("恭喜通过这次考验 你已经是个强大的海盗了,请把我给你的英雄证书交给#b凯琳#k!");
	    }
	} else if (cm.getMapId() == 108000502) {
	    if (!(cm.haveItem(4031856,15))) {
		cm.sendNext("快去收集15个 #b强大力量结晶#k 给我.");
		cm.dispose();
	    } else {
		status = 2;
		cm.sendNext("恭喜通过这次考验 你已经是个强大的海盗了,请把我给你的英雄证书交给#b凯琳#k!");
	    }
	} else {
	    cm.sendNext("错误请再尝试一次.");
	    cm.dispose();
	}
    } else if (status == 2) {
	cm.gainItem(4031012, 1);
	cm.warp(120000101,0);
	cm.dispose();
    }
}