var status = 0;
var cost = 50000;
function start() {
    cm.sendYesNo("怎么样？上海外滩风光确实不错吧，如果你有 #b50000 金币#k. 我就可以带你回 #b勇士部落#k 怎么样？要回去吗？");
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
        cm.sendOk("看来你真的被美丽的外滩风景所吸引了。");
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
		cm.warp(102000000, 0);
        cm.dispose();
    }
}
}