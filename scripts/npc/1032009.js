/*
	Purin - Before Takeoff To Orbis(101000301)
*/

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if(mode == 0) {
	cm.sendOk("旅途还很漫长...");
	cm.dispose();
	return;
    }
    if(status == 0) {
	cm.sendYesNo("你想下船吗？离开飞船后会回到原来的地方！");
    } else if(status == 1) {
	cm.warp(101000300, 0);
	cm.dispose();
    }
}
