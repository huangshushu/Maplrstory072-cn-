var status = 0;
var slc;

var chairPrice = 2000000; //椅子价格
var baitPrice = 300000; //鱼饵价格
var baitNumber = 10; //鱼饵数量

//兑换配置
function ExchangeConfig(exchangeItemId, exchangeNumber, prizesItemId, prizesPeriod, prizesNumber) {
    this.exchangeItemId = exchangeItemId; //兑换道具的id
    this.exchangeNumber = exchangeNumber; //兑换数量
    this.prizesItemId = prizesItemId; //奖品id
    this.prizesPeriod = prizesPeriod; //奖品有效天数
    this.prizesNumber = prizesNumber; //奖品数量
}

var exchangeArray = new Array();
//鲤鱼 100cm
exchangeArray.push(new ExchangeConfig(4031639, 300, 1142004, 0, 1)); //小鱼戒指
exchangeArray.push(new ExchangeConfig(4031639, 250, 2340000, 0, 1)); //祝福卷轴 *1
exchangeArray.push(new ExchangeConfig(4031639, 500, 2340000, 0, 2)); //祝福卷轴 *2
exchangeArray.push(new ExchangeConfig(4031639, 300, 1142000, 0, 1)); //猫咪纸屋

//旗鱼 140cm
exchangeArray.push(new ExchangeConfig(4031643, 40, 2100009, 0, 1)); //蝙蝠魔召唤包
exchangeArray.push(new ExchangeConfig(4031643, 40, 2049100, 0, 1))  //混沌卷轴60%
exchangeArray.push(new ExchangeConfig(4031643, 30, 2040917, 0, 1))  //盾牌攻击诅咒卷轴30%
exchangeArray.push(new ExchangeConfig(4031643, 30, 2040815, 0, 1))  //手套魔力30%卷轴
exchangeArray.push(new ExchangeConfig(4031643, 20, 2040816, 0, 1))  //手套魔力10%卷轴

//鲑鱼 150cm
exchangeArray.push(new ExchangeConfig(4031631, 20, 2049100, 0, 1))  //混沌卷轴60%
exchangeArray.push(new ExchangeConfig(4031631, 30, 2040922, 0, 1)); //盾牌魔力诅咒卷轴30%
exchangeArray.push(new ExchangeConfig(4031631, 50, 2101070, 0, 1)); //大金鱼召唤包

//银鱼 10cm
exchangeArray.push(new ExchangeConfig(4031636, 800, 1142005, 0, 1));//大黄风
exchangeArray.push(new ExchangeConfig(4031636, 1500, 1142024, 0, 1));//
exchangeArray.push(new ExchangeConfig(4031636, 300, 1112305, 0, 1));//闪耀新星戒指3克拉
exchangeArray.push(new ExchangeConfig(4031636, 200, 1112908, 0, 1));//极光戒指
exchangeArray.push(new ExchangeConfig(4031636, 150, 1112400, 0, 1));//炼金术士之戒
exchangeArray.push(new ExchangeConfig(4031636, 180, 2022154, 0, 10));//火红玫瑰*10
exchangeArray.push(new ExchangeConfig(4031636, 80, 2000005, 0, 99));//超级药水*99


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else {
            cm.dispose();
            return;
        }
        if (status == 0) {
            var says = "你好，很高兴为您服务。\r\n#b";
            says = "钓鱼需要准备钓鱼椅子,鱼饵,鱼竿。#r鱼竿、鱼饵#k需要在#b商城#k中购买。\r\n";
            says += "#L1#购买30天期限钓鱼椅子(需#b" + chairPrice + "#k金币)#l\r\n";
            says += "#L2#购买鱼饵(" + baitNumber + "个需#b" + baitPrice + "#k金币)#l\r\n";
            says += "#L3#兑换高级鱼饵#b1#k个(需要一个#r高级鱼饵罐头#k)#l\r\n";
            says += "#L4##b进入渔场#l\r\n";
            //活动兑换
            for (var i = 0; i < exchangeArray.length; i++) {
                var exchange = exchangeArray[i];
                says += "#L" + (i + 5) + "#使用" + exchange.exchangeNumber + "个#v" + exchange.exchangeItemId + "#兑换" + exchange.prizesNumber + "个#v" + exchange.prizesItemId + "#\r\n";
            }
            cm.sendSimple(says);
        } else if (status == 1) {
            slc = selection;
            if (slc == 1) { //购买椅子
                if (cm.getPlayer().getMeso() < chairPrice) {
                    cm.sendOk("您的金币不足!");
                } else if (!cm.canHold(3011000, 1)) {
                    cm.sendOk("请您的背包至少保留一个空位!");
                } else {
                    //if (!cm.gainMesoSubtract(-chairPrice, 9330045)) {
                    //    cm.dispose();
                    //     return;
                    //}
                    cm.gainMeso(-chairPrice);
                    cm.gainItem(3011000, 1, 30);
                    cm.sendOk("购买成功!");
                }

            } else if (slc == 2) { //购买鱼饵
                if (cm.getPlayer().getMeso() < baitPrice) {
                    cm.sendOk("您的金币不足!");
                } else if (!cm.canHold(2300000, baitNumber)) {
                    cm.sendOk("请您的背包至少保留一个空位!");
                } else {
                    //if (!cm.gainMesoSubtract(-baitPrice, 9330045)) {
                    //      cm.dispose();
                    //      return;
                    //  }
                    cm.gainMeso(-baitPrice);
                    cm.gainItem(2300000, baitNumber);
                    cm.sendOk("购买成功!");
                }

            } else if (slc == 3) { //兑换罐头
                if (!cm.haveItem(5350000)) {
                    cm.sendOk("你好像没有#v5350000#");
                } else if (!cm.canHold(2300001, 1)) {
                    cm.sendOk("请您的背包至少保留一个空位!");
                } else {
                    cm.gainItem(5350000, -1);
                    cm.gainItem(2300001, 8);
                    cm.sendOk("购买成功!");
                }
            } else if (slc == 4) {
                if (cm.getPlayer().getLevel() >= 30) {
                    cm.saveLocation("FISHING");
                    cm.warp(741000208);
                    cm.dispose();
                } else {
                    cm.sendOk("等级不足30无法进入");
                    cm.dispose();
                }
            } else {
                slc -= 5; //跳过前四个选项
                var exchange = exchangeArray[slc];
                if (null != exchange) {
                    if (cm.canHold(exchange.prizesItemId, exchange.prizesNumber)) {
                        if (cm.haveItem(exchange.exchangeItemId, exchange.exchangeNumber)) {
                            cm.gainItem(exchange.exchangeItemId, -exchange.exchangeNumber); //扣除物品
                            cm.gainItem(exchange.prizesItemId, exchange.prizesNumber, exchange.prizesPeriod); //获得物品
                            cm.sendOk("恭喜你,兑换" + exchange.prizesNumber + "个#v" + exchange.prizesItemId + "#成功!");
                        } else {
                            cm.sendOk("请检查你是否拥有对应的物品!");
                        }
                    } else {
                        cm.sendOk("请检查你的背包是否有空余的位置!");
                    }
                } else {
                    cm.sendOk("好像发生了一点错误哦,请截图给GM修复!");
                }
            }
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}