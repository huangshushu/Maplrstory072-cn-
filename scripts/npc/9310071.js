function start() {
    var Editing = false //false 開始
    if (Editing) {
        cm.sendOk("維修中");
        cm.dispose();
        return;
    }
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else if (mode == 0) {
        status--;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendSimple("你好，我这里是#r每日奖励#k#l的NPC，#b我这里每个30级以上的玩家都可以领取500豆豆（每日一次）。你是否要领取？#k#l\r\n " +
                "#L0##r领取500豆豆（每日一次）#k#l\r\n");

    } else if (status == 1) {
        if (selection == 0) {
            var level = cm.getPlayer().getLevel();
            if (level < 30) {
                cm.sendOk("等级不足30级。");
                cm.dispose();
                return;
            }
            var marr = cm.getQuestRecord(160108);
            var data = marr.getCustomData();
            if (data == null) {
                marr.setCustomData("0");
                data = "0";
            }
            var dat = parseInt(marr.getCustomData());
            if (dat + 86400000 > cm.getCurrentTime()) {
                cm.sendOk("已经领取过了。");
                cm.dispose();
                return;
            } else {
                marr.setCustomData("" + cm.getCurrentTime());
                cm.gainBeans(500);
                cm.sendOk("领取成功。");
                cm.dispose();
                return;
            }
        }
    }
}
