/* 
	NPC Name: 		Sunny
	Map(s): 		Orbis: Station<To Ludibrium> (200000121)
	Description: 		Orbis Ticketing Usher
*/
var status = 0;

function start() {
    status = -1;
    train = cm.getEventManager("Trains");
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
	if(train == null) {
	    cm.sendNext("发生未知错误。");
	    cm.dispose();
	} else if (train.getProperty("entry").equals("true")) {
	    cm.sendYesNo("非常好，船上还有足够的位置，请准备好你的船票，我们将进入漫长的旅行，你是不是想上船？");
	} else if (train.getProperty("entry").equals("false") && train.getProperty("docked").equals("true")) {
	    cm.sendNext("这班飞船已经出发，请等待下一次航班。");
	    cm.dispose();
	} else {
	    cm.sendNext("飞船出发前5分钟内停止检票，请注意时间。");
	    cm.dispose();
	}
    } else if(status == 1) {
     if(!cm.haveItem(4031074)) {
	    cm.sendNext("不! 你没有#b#t4031074##k所以我不能让你上船!");
	} else {
           cm.gainItem(4031074, -1); 
           cm.warp(200000122, 0);
        }
	cm.dispose();
    }
}