/**
	Pison - Florina Beach(110000000)
**/
var status = -1;
var returnmap = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	cm.sendNext("看来你在这里有些事还没有办完嘛？身心疲惫的时候到这黄金海滩休息放松一下也不错。");
	cm.safeDispose();
	return;
    }
    if (status == 0) {
	returnmap = cm.getSavedLocation("FLORINA");
	cm.sendNext("你在#b#m110000000##k已没别的事情了吗?如果有需要的话，我送你去#b#m"+returnmap+"##k吧！");
    } else if (status == 1) {
	cm.sendYesNo("想回#b#m"+returnmap+"##k吗？好~那现在准备出航吧。那…现在马上去#m"+returnmap+"#吗？")
    } else if (status == 2) {
	if (returnmap < 0) {
		returnmap = 104000000;
	}
	cm.warp(returnmap);
	cm.clearSavedLocation("FLORINA");
	cm.dispose();
    }
}
