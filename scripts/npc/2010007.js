/* guild creation npc */
var status = -1;
var sel;

function start() {
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

    if (status == 0)
	cm.sendSimple("欢迎来到家族公馆，你现在想做什么呢?\r\n#b#L0#创建家族#l\r\n#L1#解散家族#l\r\n#L2#增加家族成员人数上限#l#k");
    else if (status == 1) {
	sel = selection;
	if (selection == 0) {
	    if (cm.getPlayerStat("GID") > 0) {
		cm.sendOk("你已经拥有家族了，不能再创建家族。");
		cm.dispose();
	    } else
		cm.sendYesNo("创建一个新的家族需要#b20,000,000金币#k, 你确定继续创建一个新的家族吗？");
	} else if (selection == 1) {
	    if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
		cm.sendOk("你还没有家族或你不是族长，因此你不能解散该家族。");
		cm.dispose();
	    } else
		cm.sendYesNo("你确定真的要解散你的家族？当解散后你将不能恢复所有家族相关资料以及GP的数值，是否继续？");
	} else if (selection == 2) {
	    if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
		cm.sendOk("你不是族长，因此你将不能增加家族成员的人数上限。");
		cm.dispose();
	    } else
		cm.sendYesNo("家族成员人数每增加#b5#k位需要支付#b5,000,000金币#k, 你确定要增加吗？");
	} 
    } else if (status == 2) {
	if (sel == 0 && cm.getPlayerStat("GID") <= 0) {
	    cm.genericGuildMessage(1);
	    cm.dispose();
	} else if (sel == 1 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
	    cm.disbandGuild();
	    cm.dispose();
	} else if (sel == 2 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
	    cm.increaseGuildCapacity(false);
	    cm.dispose();
	} 
    }
}