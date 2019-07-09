/* Brittany
	Henesys Random Hair/Hair Color Change.
*/
var status = -1;
var beauty = 0;
var hair_Colo_new;

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
	return;
    } else {
	status++;
    }

    if (status == 0) {
	cm.sendSimple("你好,我是美发店的助手!如果你有#b#t5150010##k或#b#t5151000##k,你就放心的把发型交给我,我会让你满意的.那么你要做什么?请选择吧!\r\n#L0#改变发型: #i5150000##t5150000##l\r\n#L1#染色: #i5151000##t5151000##l");
    } else if (status == 1) {
	if (selection == 0) {
	    var hair = cm.getPlayerStat("HAIR");
	    hair_Colo_new = [];
	    beauty = 1;

	    if (cm.getPlayerStat("GENDER") == 0) {
		hair_Colo_new = [30310, 30330, 30060, 30150, 30410, 30210, 30140, 30120, 30200, 30560, 30510, 30610, 30470];
	    } else {
		hair_Colo_new = [31150, 31310, 31300, 31160, 31100, 31410, 31030, 31080, 31070, 31610, 31350, 31510, 31740];
	    }
	    for (var i = 0; i < hair_Colo_new.length; i++) {
		hair_Colo_new[i] = hair_Colo_new[i] + (hair % 10);
	    }
	    cm.sendYesNo("如果你有#b#t5150000##k,那么我将帮你随机改变一种发型,你确定要改变发型吗?");

	} else if (selection == 1) {
	    var currenthaircolo = Math.floor((cm.getPlayerStat("HAIR") / 10)) * 10;
	    hair_Colo_new = [];
	    beauty = 2;

	    for (var i = 0; i < 8; i++) {
		hair_Colo_new[i] = currenthaircolo + i;
	    }
	    cm.sendYesNo("如果你有#b#t5151000##k,那么我将帮你随机改变一种发色,你确定要改变发色吗?");
	}
    } else if (status == 2){
	if (beauty == 1){
	    if (cm.setRandomAvatar(5150000, hair_Colo_new) == 1) {
		cm.sendOk("好了,让朋友们赞叹你的新发型吧!");
	    } else {
		cm.sendOk("看起来你并没有我们的会员卡，我恐怕不能给你理发，我很抱歉。请你先购买吧。");
	    }
	} else {
	    if (cm.setRandomAvatar(5151000, hair_Colo_new) == 1) {
		cm.sendOk("Enjoy your new and improved haircolor!");
	    } else {
		cm.sendOk("看起来你并没有我们的会员卡，我恐怕不能给你理发，我很抱歉。请你先购买吧。");
	    }
	}
	cm.safeDispose();
    }
}
