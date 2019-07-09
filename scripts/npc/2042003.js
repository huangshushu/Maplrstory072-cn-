var status = 0;
function start() {
    cm.sendYesNo("請問你想要離開？？");
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
        cm.sendOk("改變主意再來找我。");
        cm.dispose();
        return;
    }
    status++;
    if (status == 1) {
		cm.warp(980000000,0);
        cm.dispose();
    }
}