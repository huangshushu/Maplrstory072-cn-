var debug = false;
var status = -1;
var gain;
var pig = 4032226;
var req;
var msg;
var sels;
var amount;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (debug && !cm.getPlayer().isAdmin()) {
        msg = "暫停運作";
        cm.sendNext(msg);
            cm.dispose();
            return;
        }
    if (mode == 1) {
            status++;
    } else if (mode == 0 && status == 0) {
            status--;
    } else {
          cm.dispose();
          return;
        } 
            
    if (status == 0) {
        msg = "#b歡迎玩家 #r#h ##k\r\n" +
                " 兌換#r綿羊單人床#i3010054#\r\n" +
                "#L10##r黃金豬#i" + pig + "##bx500#r換#b綿羊單人床 #i3010054#\r\n" +
                "#L102##r黃金豬#i" + pig + "##bx5#r換#b加持道具(加攻擊)#i2022483#x5\r\n" +
                "#L103##r黃金豬#i" + pig + "##bx1#r換#b加持道具(加移速)#i2022484#x1\r\n" +
                "#L104##r黃金豬#i" + pig + "##bx1#r換#b加持道具(加跳躍)#i2022485#x1\r\n" +
                "#L105##r黃金豬#i" + pig + "##bx1#r換#b加持道具(加迴避)#i2022486#x1\r\n" +
                "#L106##r黃金豬#i" + pig + "##bx1#r換#b加持道具(加命中)#i2022487#x1";
        cm.sendSimple(msg);
    } else if (status == 1) {
        sels = selection;
        msg = "請問需要用多少個#i" + pig + "#換加持道具??";
        cm.sendGetNumber(msg, 1, 1, 100);
    } else if (status == 2) {
        amount = selection;
		req = amount;
        if (sels == 10) {
            req = (amount * 500);
            gain = 3010054;
        } else if (sels == 102) {
            req = (amount * 5);
            gain = 2022483;
        } else if (sels == 103) {
            gain = 2022484;
        } else if (sels == 104) {
            gain = 2022485;
        } else if (sels == 105) {
            gain = 2022486;
        } else if (sels == 106) {
            gain = 2022487;
                } else {
                    cm.dispose();
            return;
				}
        msg = "您確定要使用#i" + pig + "#x" + req + "兌換加持道具(#i" + gain + "#)x" + amount + "嗎？？";
        cm.sendYesNo(msg);
    } else if (status == 3) {
        if (!cm.canHold(3010054) || !cm.canHold(2022483)) {
            msg = "您的背包空格不足";
            cm.sendNext(msg);
                    cm.dispose();
            return;
				}
        if (!cm.haveItem(pig, req)) {
            msg = "您身上沒有足夠的#i" + pig + "#,請再次確認";
            cm.sendNext(msg);
                    cm.dispose();
            return;
				}
        msg = "請問再次確定是否要購買加持道具(#i" + gain + "#)x" + amount + "，價格為: " + pig + "x" + req;
        cm.sendYesNo(msg)
    } else if (status == 4) {
        cm.gainItem(pig, -req);
        cm.gainItem(gain, amount);
        msg = "獲得#i" + gain + "#x" + amount;
        cm.sendNext(msg);
                    cm.dispose();
        return;
                } else {
                    cm.dispose();
        return;
				}
			 }
	