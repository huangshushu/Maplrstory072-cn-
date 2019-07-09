/* Author: Xterminator
	NPC Name: 		Regular Cab
	Map(s): 		Victoria Road : Kerning City (103000000)
	Description: 		Kerning City Cab
*/
var status = 0;
var maps = Array(104000000, 102000000, 101000000, 100000000, 120000000);
var cost = Array(1000, 1000, 1000, 1000, 1000, 1000);
var costBeginner = Array(100, 100, 100, 100, 100, 100);
var show;
var sCost;
var selectedMap = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 1 && mode == 0) {
	cm.dispose();
	return;
    } else if (status >= 2 && mode == 0) {
	cm.sendNext("在这个村子里还有许多漂亮的景点，如果你想去其他地方，欢迎随时使用我们的出租车服务。");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendNext("您好~！我是射手村中巴。你想不想又快捷又安全的到达其他地方去？那么请使用我们的出租车吧。它会马上将你送到你想去的地方，价格很便宜哦！");
    } else if (status == 1) {
	var job = cm.getJob();
	if (job == 0 || job == 1000 || job == 2000) {
	    var selStr = "新手的话价格可以#b9折#k优惠。请选择你的目的地吧。#b";
	    for (var i = 0; i < maps.length; i++) {
		selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " 金币)#l";
	    }
	} else {
	    var selStr = "请选择你的目的地吧。按照目的地不同，车费也有所不同。#b";
	    for (var i = 0; i < maps.length; i++) {
		selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " 金币)#l";
	    }
	}
	cm.sendSimple(selStr);
    } else if (status == 2) {
	var job = cm.getJob();
	if (job == 0 || job == 1000 || job == 2000) {
	    sCost = costBeginner[selection];
	    show = costBeginner[selection];
	} else {
	    sCost = cost[selection];
	    show = cost[selection];
	}
	cm.sendYesNo("看来这里的事情你已经办完了嘛。你确定要去 #b#m" + maps[selection] + "##k吗？ 票价是 #b" + show + " 金币#k.");
	selectedMap = selection;
    } else if (status == 3) {
	if (cm.getMeso() < sCost) {
	    cm.sendNext("你好像没有足够的金币，这样的话，我不能为你服务。");
	} else {
	    cm.gainMeso(-sCost);
	    cm.warp(maps[selectedMap]);
	}
	cm.dispose();
    }
}