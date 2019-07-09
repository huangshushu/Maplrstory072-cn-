var status = -1;
//高级快乐百宝劵
var itemId = 5220040;


function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (0 == status) {
            cm.sendNext("欢迎使用高级快乐百宝箱，我这里道具应有尽有哦！\n使用前请确认您的背包是否有足够的空间！");
        } else if (1 == status) {
            cm.sendYesNo("确定使用#v " + itemId + "#进行一次游戏吗？");
        } else if (2 == status) { //抽奖
            if (!cm.haveItem(itemId)) {
                cm.sendOk("对不起！你没有#v " + itemId + "#，请检查。");
                cm.dispose();
                return;
            }
            var result = cm.seniorBox();

            //大奖;全服广播
            //cm.info(typeof (result.get(2).intValue()));
            if (cm.seniorBoxlevel() == 3 || cm.seniorBoxlevel() == 4) {
                //cm.info("yes");
                cm.gainItem(5220040, -1);
                cm.gainGachaponItema(cm.seniorBoxitemId(), 1, "高级快乐百宝箱");
                cm.sendOk("恭喜你获得#v " + cm.seniorBoxitemId() + "#！你的手气太好了,继续努力！");
            } else {
                //cm.info("no");
                cm.gainItem(5220040, -1);
                cm.gainItem(cm.seniorBoxitemId(), 1);
                cm.sendOk("恭喜你获得#v " + cm.seniorBoxitemId() + "#！不要放弃下个大奖等着你！");
            }

            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}
