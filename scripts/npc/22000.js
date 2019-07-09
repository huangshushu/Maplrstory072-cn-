/* RED 1st impact
    Vasily (Maple Return skill)
    Made by Daenerys
*/
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else 
        if (status == 0) {
		    cm.sendNext("你还有一些事情吗?");
            cm.dispose();
        status--;
    }
    if (status == 0) {
	    cm.sendYesNo("该船将到达明珠港，如果你需要在这里有紧急的事情，你最好先处理好。你准备好出发了吗?");
	} else if (status == 1) {
	    cm.warp(104000000,0);
		cm.dispose();
    }
  }