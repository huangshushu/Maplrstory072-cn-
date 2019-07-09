var status = 0;
var jobId;
var jobName;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 2) {
        cm.sendOk("请重试.");
        cm.dispose();
        return;
    }
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0) {
        if (cm.getJob() == 0) {
            if (cm.getPlayer().getLevel() >= 8) {
                cm.sendNext("你想成为一位#r魔法师#k ?");
            } else {
                cm.sendOk("你还没有具备成为#r魔法师#k的资格。")
                cm.dispose();
            }
        } else {
            if (cm.getPlayer().getLevel() >= 30 && cm.getJob() == 200) { // 法师
                if (cm.haveItem(4031012, 1)) {
                    if (cm.haveItem(4031012, 1)) {
                        status = 20;
                        cm.sendNext("我知道你已经完成了转职测试!");
                    } else {
                        if (!cm.haveItem(4031009)) {
                            cm.gainItem(4031009, 1);
                        }
                        cm.sendOk("请去找 #r法师转职教官#k.")
                        cm.dispose();
                    }
                } else {
                    status = 10;
                    cm.sendNext("你或许可以做转职的准备.让我看看...");
                }
            } else if (cm.getPlayer().getLevel() >= 70 && cm.getJob() == 210 || cm.getJob() == 220 || cm.getJob() == 230) {
				if(cm.haveItem(4031057, 1)){
			    cm.sendOk("你完成了一个考验，现在去找 #b鲁碧#k.位于冰封雪域#b长老公馆#k!");
               } if (cm.haveItem(4031059, 1)) {
                    cm.gainItem(4031057, 1);
                    cm.gainItem(4031059, -1);
                   //cm.warp(211000001, 0);
                    cm.sendOk("你完成了一个考验,现在去找#b鲁碧#k,位于冰封雪域#b长老公馆#k");
                } else {
                    cm.sendOk("嗨,#b#h0##k!我需要一个#b黑符#k.快去找异次元空间拿给我.");
                }
                cm.dispose();
            } else {
                cm.sendOk("魔法师是体质弱.但是他的力量很强大...");
                cm.dispose();
            }
        }
    } else if (status == 1) {
        cm.sendNextPrev("一旦转职了就不能反悔。");
    } else if (status == 2) {
        cm.sendYesNo("你真的要成为一位 #r法师#k ?");
    } else if (status == 3) {
        if (cm.getJob() == 0) {
            cm.changeJob(200); // 法师
			cm.resetStats(4, 4, 4, 4);
        }
        cm.gainItem(1372005, 1);
        cm.sendOk("转职成功 !");
        cm.dispose();
    } else if (status == 11) {
        cm.sendNextPrev("你可以选择你要转职成为一位#r法师(火,毒)#k,#r法师(冰,雷)#k 或#r牧师#k.");
    } else if (status == 12) {
        cm.askAcceptDecline("但是我必须先测试你,你准备好了吗 ?");
    } else if (status == 13) {
        cm.gainItem(4031009, 1);
        //cm.warp(101020000);
        cm.sendOk("请去找#b法师二转教官#k他在#b魔法密林北部#k的最上方,他会帮助你的!");
        cm.dispose();
    } else if (status == 21) {
        cm.sendSimple("你想要成为什么?#b\r\n#L0#法师(火,毒)#l\r\n#L1#法师(冰,雷)#l\r\n#L2#牧师#l#k");
    } else if (status == 22) {
        if (selection == 0) {
            jobName = "法师(火,毒)";
            jobId = 210; // FP
        } else if (selection == 1) {
            jobName = "法师(冰,雷)";
            jobId = 220; // IL
        } else {
            jobName = "牧师";
            jobId = 230; // CLERIC
        }
        cm.sendYesNo("你真的要成为一位 #r" + jobName + "#k?");
    } else if (status == 23) {
        cm.changeJob(jobId);
        cm.gainItem(4031012, -1);
        cm.sendOk("转职成功 !");
        cm.dispose();
    }
}