var itemid = new Array(4031036, 4031037, 4031038);
var mapid = new Array(103000900, 103000903, 103000906);
var menu;
var status;

function start() {
    status = 0;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendNext("You must have some business to take care of here, right?");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        if (status == 1) {
            menu = "这里是检票口，你必须提前购买车票！没有票是不能进入。\r\n";
            for (i = 0; i < itemid.length; i++) {
                if (cm.haveItem(itemid[i])) {
                    menu += "#L" + i + "##b#m" + mapid[i] + "##k#l\r\n";
                }
            }
            cm.sendSimple(menu);
        }
        if (status == 2) {
            section = selection;
            if (section == 0) {
                cm.gainItem(4031036, -1);
                cm.warp(103000900);
                cm.dispose();
            } else if (section == 1) {
                cm.gainItem(4031037, -1);
                cm.warp(103000903);
                cm.dispose();
            } else if (section == 2) {
                cm.gainItem(4031038, -1);
                cm.warp(103000906);
                cm.dispose();
            }
        }
    }
}