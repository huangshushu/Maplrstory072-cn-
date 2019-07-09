var status = 0;
var cost = 50000;
function start() {
    cm.sendYesNo("嗨！我是洪姓驾驶员，我负责驾驶飞往上海的飞机。经过长年的飞行，我的驾驶技术已经很了不得。如果你有 #b50000 金币#k. 我就可以带你去美丽的 #b上海外滩#k 怎么样？要去吗？");
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
        cm.sendOk("美丽的上海外滩，难道你不想去看看吗！真遗憾。");
        cm.dispose();
        return;
    }
    status++;
    if (status == 1) {
		if(cm.getMeso() < cost) {
		cm.sendOk("你确定你有 #b50000 金币#k？ 如果没有，我可不能免费送你去。");
		cm.dispose();
		} else {
		cm.gainMeso(-cost);
		cm.warp(701000100, 0);
        cm.dispose();
    }
}
}