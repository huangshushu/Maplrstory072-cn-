/* Dr. Feeble
	Henesys Random Eye Change.
*/
var status = 0;
var beauty = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendNext("你好,我是医生!如果你有#b昭和村整形手术普通会员卡#k,你就放心的让我为你进行整形手术吧,我会让你满意的.那么你要做什么？\r\n\#L2##b进行整形手术#k(使用#b昭和村整形手术普通会员卡#k)随机#l");
    } else if (status == 1) {
	cm.sendYesNo("如果你有#b昭和村整形手术普通会员卡#k,那么我将帮你随机改变一种脸型,你确定要改变脸型吗？");
    } else if (status == 2){
	var face = cm.getPlayerStat("FACE");
	var facetype;

	if (cm.getPlayerStat("GENDER") == 0) {
	    facetype = [20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20012, 20014];
	} else {
	    facetype = [21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21012, 21014];
	}
	for (var i = 0; i < facetype.length; i++) {
	    facetype[i] = facetype[i] + face % 1000 - (face % 100);
	}

	if (cm.setRandomAvatar(5152008, facetype) == 1) {
	    cm.sendOk("#e好了,你的朋友们一定认不出你了!");
	} else {
	    cm.sendOk("看起来你并没有我们的会员卡,我恐怕不能给你理发,我很抱歉.请你先购买吧。");
	}
	cm.dispose();
    }
}
