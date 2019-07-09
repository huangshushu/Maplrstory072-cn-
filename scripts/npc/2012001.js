/*
	NPC Name: 		Rini
	Map(s): 		Orbis: Station<To Ellinia> (200000111)
	Description: 		Orbis Ticketing Usher
*/
var status = 0;

function start() {
    status = -1;
    boat = cm.getEventManager("Boats");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if(mode == 0) {
	cm.sendNext("你还有什么事情再这里没有完成吗？");
	cm.dispose();
	return;
    }
    if (status == 0) {
	if(boat == null) {
	    cm.sendNext("发生未知错误。");
	    cm.dispose();
	} else if(boat.getProperty("entry").equals("true")) {
	    cm.sendYesNo("非常好，船上还有足够的位置，请准备好你的船票，我们将进入漫长的旅行，你是不是想登船？");
	} else if(boat.getProperty("entry").equals("false") && boat.getProperty("docked").equals("true")) {
	    cm.sendNext("很抱歉本航班的船已经开走,乘坐时间表可以通过售票展台查看");
	    cm.dispose();
	} else {
	    cm.sendNext("飞船起飞前5分钟内停止检票，请注意时间。");
	    cm.dispose();
	}
    } else if(status == 1) {
        if(!cm.haveItem(4031047)) {
	    cm.sendNext("不!你没有#b#t4031045##k所以我不能让你上船!.");
	} else {
        cm.gainItem(4031047, -1);
	cm.warp(200000112, 0);
        }
	cm.dispose();
    }
}
