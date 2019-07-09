/**
	VIP Cab - Victoria Road : Ellinia (101000000)
**/

var status = -1;
var cost;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status >= 1) {
	    cm.sendNext("在这个村子里还有许多漂亮的景点，如果你想去蚂蚁洞广场，欢迎随时使用我们的出租车服务。");
	    cm.safeDispose();
	    return;
	}
	status--;
    }

    if (status == 0) {
	cm.sendNext("您好~！我们是星级出租车。不同于村落之间来往的一半的中巴我们给您提供更高级的服务。因此车费有点贵…您只要支付#b10,000金币#k，我们就会将您安全迅速的送到#b蚂蚁洞广场#k。但是等级太低进去会很危险是否要进去呢？");
    } else if (status == 1) {
	var job = cm.getJob();
	if (job == 0 || job == 1000 || job == 2000) {
	    cm.sendYesNo("新手的话价格可以#b9折#k优惠。蚂蚁洞广场是位于金银岛中间的迷宫深处。在那里有24小时排挡。你是否要付#b1,000金币#k后去蚂蚁洞广场？");
	    cost = 1000;
	} else {
	    cm.sendYesNo("蚂蚁洞广场是位于金银岛中间的迷宫深处。在那里有24小时排挡。你是否要付#b10,000金币#k后去蚂蚁洞广场？");
	    cost = 10000;
	}
    } else if (status == 2) {
	if (cm.getMeso() < cost) {
	    cm.sendNext("你好象没有足够的金币，这样的话，我不能为你服务。");
	    cm.safeDispose();
	} else {
	    cm.gainMeso(-cost);
	    cm.warp(105070001, 0);
	    cm.dispose();
	}
    }
}