var equiplist;
var cashlist;
var str = "";
var isok;
var modea = 0;

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
        cm.sendSimple("你好，我是删除装备栏和特殊栏道具的NPC。\r\n" +
                "#L0#删除装备栏道具#l\r\n" +
                "#L1#删除特殊栏道具#l\r\n");

    } else if (status == 1) {
        if (selection == 0) {
            equiplist = cm.getEquiplist();
            if (equiplist != null) {
                for (var i = 0; i < equiplist.size(); i++) {
                    str += "#L" + i + "##i" + equiplist.get(i).getItemId() + "##t" + equiplist.get(i).getItemId() + "##k\r\n";
                }
            }
            cm.sendSimple("请选择想要丢弃的衣服……丢弃的衣服将无法找回。如果有多件相同的衣服，会最先丢掉背包最前面的东西。\r\n" + str);
            moba = 1;

        }
        if (selection == 1) {
            cashlist = cm.getCashlist();
            if (cashlist != null) {
                for (var i = 0; i < cashlist.size(); i++) {
                    str += "#L" + i + "##i" + cashlist.get(i).getItemId() + "##t" + cashlist.get(i).getItemId() + "##k\r\n";
                }
            }
            cm.sendSimple("请选择想要丢弃的衣服……丢弃的衣服将无法找回。如果有多件相同的衣服，会最先丢掉背包最前面的东西。\r\n" + str);
            moba = 2;

        }

    } else if (status == 2) {
        if (moba == 1) {
            select = selection;
            isok = cm.removeItem(equiplist.get(select).getItemId());
            if (isok) {
                cm.sendOk("已删除该道具！");
            } else {
                cm.sendOk("删除失败，请报告管理员。");
            }
            cm.dispose();

        } else {
            cm.dispose();
        }
        if (moba == 2) {
            select = selection;
            isok = cm.removeItem(cashlist.get(select).getItemId());
            if (isok) {
                cm.sendOk("已删除该道具！");
            } else {
                cm.sendOk("删除失败，请报告管理员。");
            }
            cm.dispose();

        } else {
            cm.dispose();
        }
    }
}
