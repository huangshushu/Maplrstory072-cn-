var status = -1;

function start() {

    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode != 1) {
		if (status == 0 && mode == 0) {
			cm.sendNext("请重新考虑一下，然后再和我说话。");
		}
        cm.dispose();
        return;
    }
    status++;
    if (status == 0) {
        if (cm.getMapId() == 260020000 || cm.getMapId() == 260000000)
            cm.sendYesNo("你想乘坐#b骆驼中巴#k，到炼金术的村庄#b玛加提亚#k去吗？价格是#b1500金币#k。");
        else if (cm.getMapId() == 260020700 || cm.getMapId() == 261000000)
            cm.sendYesNo("你想乘坐#b骆驼中巴#k，到绿洲里的村庄#b阿里安特#k去吗？价格是#b1500金币#k。");
    } else if (status == 1) {
		if(cm.getMeso() >= 1500){
			cm.gainMeso(-1500);
			cm.warp((cm.getMapId() == 260020000 || cm.getMapId() == 260000000) ? 261000000 : (cm.getMapId() == 260020700 || cm.getMapId()) == 261000000 ? 260000000 : 260000000);
		} else {
			cm.sendNext("你好像没带够钱啊。");
		}
		cm.dispose();
    }
}