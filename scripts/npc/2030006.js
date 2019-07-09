/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
var status = 0;
var qChars = new Array ("Q1: 冒险岛中，从等级1到等级2需要多少经验值？#10#12#15#20#3",
    "Q1: 根据不同职业为了第1次转职所要求的能力，被不正确叙述的是哪一个？#战士 35 力量#飞侠 20 幸运#法师 20 智力#弓箭手 25 敏捷#2",
    "Q1: 被怪物攻击时特别的异常状态没有被正确说明的是哪一个？#虚弱 — 移动速度降低#封印 - 不能使用技能#黑暗 - 命中下降#诅咒 - 减少经验#1",
    "Q1: 根据不同职业的第1次转职必须条件 敏捷25 正确的是哪一个？#战士#弓箭手#法师#飞侠#2");
var qItems = new Array( "Q2: 下列怪物中，哪组怪物与打倒它所能得到的战利品是正确对应关系的？#大幽灵-幽灵头带#蝙蝠 — 蝙蝠翅膀#煤泥 - 粘糊糊的泡泡#猪 - 丝带#2",
    "Q2: 下列怪物中，哪组怪物与打倒它所能得到的战利品是不正确对应关系的？#漂漂猪- 蝴蝶结#僵尸蘑菇 - 道符#绿色蜗牛 - 绿色蜗牛壳#食人花——食人花的叶子#4",
    "Q2: 冒险岛中下列药品中，哪组药品与功效是正确对应关系的？#白色药水 - 回复 250 HP#超级药水 — HP400恢复#红色药水 - 回复 100 HP#披萨 — HP400恢复#4",
    "Q2: 冒险岛中下列药品中，哪组药水可以回复HP50%MP50%？#特殊药水#超级药水#大西瓜#矿泉水#1",
    "Q2: 冒险岛中下列药品中，哪组药品与功效是不正确对应关系的？#蓝色药水 - 恢复 100 MP#活力药水 - 恢复 300 MP#清晨之露 - 恢复3000MP#红色药水 - 恢复 50 HP#3");
var qMobs = new Array(  "Q3: 绿蘑菇、蓝水灵、斧木妖、三眼章鱼，哪个是等级最高的怪物？#绿蘑菇#三眼章鱼#蓝水灵#斧木妖#4",
    "Q3: 明珠港没有哪个怪物？#小石球#蜗牛#蓝蜗牛#蘑菇仔#1",
    "Q3: 金银岛的废弃都市不能见到的NPC是谁？#洪先生#后街吉姆#休咪#鲁克#4",
    "Q3: 冒险岛最初遇见的NPC是谁？#冒险岛运营员#瑞恩#皮奥#希娜#4",
    "Q3: 会飞的怪物是什么？#巫婆#绿水灵#生气的雪人#小白鼠#1",
    "Q3: 能够反复执行的任务是哪一个？#秀兹的兴趣#寻找《上古魔书》#克林的记忆#艾温的玻璃鞋#4",
    "Q3: 为了进行2次转职收集好30个黑珠后转职教官会给你的物品是什么?#转职教官的信#英雄证书#英雄的勋章#介绍信#2");
var qQuests = new Array("Q4:要求级别最高的任务是哪一个？#蜗牛传说#芳博士的化石研究#阿尔卡斯特和黑暗水晶#柰洛的项链#3",
    "Q4: 下面哪个职业不是二转中出现的职业? #炮手#刺客#火枪手#猎手#1",
    "Q4: 在金银岛的明珠港不能看到的NPC是谁？#赛恩#特奥#贝干#约翰#1",
    "Q4: 射手村的玛雅为了治好自己的病让你给她的物品是哪一个?#补药#奇怪的药#克里夫的特殊药水#应急治疗药#2",
    "Q4: 金银岛没有的村落？#林中之城#彩虹村#射手村#废弃都市#2",
    "Q4: 在神秘岛(天空之城)没有出现的怪物是哪一个？#黑鳄鱼#小石球#艾利杰#黑色飞狮#1");
var qTowns = new Array( "Q5:在金银岛的勇士部落不能看到的NPC是谁？ #易得#酋长#蘑菇博士#伊安#1",
    "Q5: 在废弃都市能够见到一个离家的少年阿列克斯，他的父亲是谁？#麦吉#小摊老板#斯坦长老#伊卡路斯#3",
    "Q5: 在金银岛和蚂蚁洞看不到的怪物是哪一个？#石球#刺蘑菇#僵尸蘑菇#蝙蝠#1",
    "Q5: 在天空之城不能看到的NPC是哪一个？#哈尔里#炼金术师#索非亚#舍琵#3",
    "Q5: 在神秘岛冰峰雪域看不见的NPC是谁?#武先生#高登#阿尔卡斯特#保姆#4",
    "Q5: 寻找《上古魔书》任务完成后,找谁换取奖励?#炼金术师#索菲亚#列高罗#阿尔卡斯特#4");
var correctAnswer = 0;

function start() {
	if (cm.haveItem(4031058, 1)) {
		cm.sendOk("#h #,你已经有了 #t4031058#!");
		cm.dispose();
	}
    if (!(cm.haveItem(4031058, 1))) {
        cm.sendNext("我是 #b神圣的石头#k.看来你来到这个阶段非常的不容易啊!");
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("下次再见.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendNextPrev("如果你给我#b黑暗水晶#k.我将会让你试着回答5个问题,若您5个问题都答对您将得到 #v4031058# #b智慧项链#k.");
        else if (status == 2) {
            if (!cm.haveItem(4005004)) {
                cm.sendOk("#h #, 你没有 #b黑暗水晶#k");
                cm.dispose();
            } else {
                cm.gainItem(4005004, -1);
                cm.sendSimple("测验开始 #b接受挑战吧!#k.\r\n\r\n" + getQuestion(qChars[Math.floor(Math.random() * qChars.length)]));
                status = 2;
            }
        } else if (status == 3) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答对了.\n准备答下一题?");
            else {
                cm.sendOk("你答错了的答案!.\r\n很抱歉你必须在给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 4)
            cm.sendSimple("测验开始 #b接受挑战吧!#k.\r\n\r\n" + getQuestion(qItems[Math.floor(Math.random() * qItems.length)]));
        else if (status == 5) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答对了.\n准备答下一题?");
            else {
                cm.sendOk("你答错了的答案!.\r\n很抱歉你必须在给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 6) {
            cm.sendSimple("测验开始 #b接受挑战吧!#k.\r\n\r\n" + getQuestion(qMobs[Math.floor(Math.random() * qMobs.length)]));
            status = 6;
        } else if (status == 7) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答对了.\n准备答下一题??");
            else {
                cm.sendOk("你答错了的答案!.\r\n很抱歉你必须在给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 8)
            cm.sendSimple("测验开始 #b接受挑战吧!#k.\r\n\r\n" + getQuestion(qQuests[Math.floor(Math.random() * qQuests.length)]));
        else if (status == 9) {
            if (selection == correctAnswer) {
                cm.sendOk("#h # 你答对了.\n准备答下一题?");
                status = 9;
            } else {
                cm.sendOk("你答错了的答案!.\r\n很抱歉你必须在给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 10) {
            cm.sendSimple("最后一个问题.\r\n测验开始 #b接受挑战吧!#k.\r\n\r\n" + getQuestion(qTowns[Math.floor(Math.random() * qTowns.length)]));
            status = 10;
        } else if (status == 11) {
            if (selection == correctAnswer) {
                cm.gainItem(4031058, 1);
				//cm.warp(211000001, 0);
                cm.sendOk("拿着这个 #v4031058# 去找你的转职教官吧!");
                cm.dispose();
            } else {
                cm.sendOk("太可惜了,差一题就可以通关了,继续加油!\r\n很抱歉你必须在给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        }
    }
}
function getQuestion(qSet){
    var q = qSet.split("#");
    var qLine = q[0] + "\r\n\r\n#L0#" + q[1] + "#l\r\n#L1#" + q[2] + "#l\r\n#L2#" + q[3] + "#l\r\n#L3#" + q[4] + "#l";
    correctAnswer = parseInt(q[5],10);
    correctAnswer--;
    return qLine;
}