/* Natalie
	Henesys VIP Hair/Hair Color Change.
*/
var status = -1;
var beauty = 0;
var hair_Colo_new;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
	return;
    } else {
	status++;
    }

    if (status == 0) {
	cm.sendSimple("你好,我是射手村美发店的店长娜塔丽!如果你有#b#t5150001##k或#b#t5151001##k,你就放心的把发型交给我,我会让你满意的.那么你要做什么?请选择吧!\r\n#L0#改变发型: #i5150001##t5150001##l\r\n#L1#染色: #i5151001##t5151001##l");
    } else if (status == 1) {
	if (selection == 0) {
	    var hair = cm.getPlayerStat("HAIR");
	    hair_Colo_new = [];
	    beauty = 1;

	    if (cm.getPlayerStat("GENDER") == 0) {
		hair_Colo_new = [30030, 30020, 30000, 30310, 30330, 30060, 30150, 30410, 30210, 30140, 30120, 30200];
	    } else {
		hair_Colo_new = [31050, 31040, 31000, 31150, 31310, 31300, 31160, 31100, 31410, 31030, 31080, 31070];
	    }
	    for (var i = 0; i < hair_Colo_new.length; i++) {
		hair_Colo_new[i] = hair_Colo_new[i] + (hair % 10);
	    }
	    cm.askAvatar("我可以改变你的发型,让它比现在看起来漂亮。你为什么不试着改变它下? 如果你有#b#t5150001##k,我将会帮你改变你的发型,那么选择一个你想要的新发型吧!", hair_Colo_new);
	} else if (selection == 1) {
	    var currenthaircolo = Math.floor((cm.getPlayerStat("HAIR") / 10)) * 10;
	    hair_Colo_new = [];
	    beauty = 2;

	    for (var i = 0; i < 8; i++) {
		hair_Colo_new[i] = currenthaircolo + i;
	    }
	    cm.askAvatar("我可以改变你的发色,让它比现在看起来漂亮. 你为什么不试着改变它下? 如果你有#b#t51051001##k,我将会帮你改变你的发色,那么选择一个你想要的新发色吧!", hair_Colo_new);
	}
    } else if (status == 2){
	if (beauty == 1){
	    if (cm.setAvatar(5150001, hair_Colo_new[selection]) == 1) {
		cm.sendOk("好了,让朋友们赞叹你的新发型吧!");
	    } else {
		cm.sendOk("看起来你并没有我们的高级会员卡,我恐怕不能给你染发,我很抱歉.请你先购买吧.");
	    }
	} else {
	    if (cm.setAvatar(5151001, hair_Colo_new[selection]) == 1) {
		cm.sendOk("好了,让朋友们赞叹你的新发型吧!");
	    } else {
		cm.sendOk("看起来你并没有我们的高级会员卡,我恐怕不能给你染发,我很抱歉.请你先购买吧.");
	    }
	}
	cm.dispose();
    }
}
