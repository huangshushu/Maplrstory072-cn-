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
        cm.sendSimple("此处兑换 #b- 枫叶 -#k#l #r1比1兑换。#k#l\r\n " +
                "#r枫叶#k#l#i4001126##r获得方式:#k#l\r\n " +
                "#r打任何怪物有几率掉落#k#l\r\n " +
                "\r\n " +
                "#b当前点卷:#k#l#r" + cm.getPlayer().getCSPoints(1) + "#k#l\r\n " +
                "#b当前枫叶:#k#l#r" + cm.haveItemQuantity(4001126) + "#k#l\r\n " +
                "#L0##b用枫叶兑换点卷#k#r比例1：1#k#l\r\n");

    } else if (status == 1) {
        if (selection == 0) {
            cm.sendGetNumber("你想用枫叶兑换多少点卷", 1, 1, 1000);
        }
    } else if (status == 2) {
        qty = selection;
        if (qty <= 0) {
            cm.sendOk("数量错误！");
            cm.dispose();
            return;
        }
        if (qty > 1000) {
            cm.sendOk("数量错误！");
            cm.dispose();
            return;
        }
        if (!cm.haveItem(4001126, qty)) {
            cm.sendOk("数量不足！");
            cm.dispose();
            return;
        }
        cm.gainItem(4001126, -qty);
        cm.getPlayer().modifyCSPoints(1, qty);
        cm.sendOk("兑换" + qty + "点卷成功");
        cm.dispose();
        return;
    }
}
