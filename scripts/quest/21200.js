var status = -1;

function start(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
            qm.sendNext("有很緊急的事情。要是拒絕的話，肯定會後悔的哦？#b有關你長矛的事情#k，也就是有關你的過去。誰知道呢？……說不定這個長矛能夠喚醒你的能力？");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.askAcceptDecline("修煉進展得如何？喲，等級升得這麼高了？難怪人們都說濟州島是養馬的天堂，維多利亞港是升級的天堂……對了，現在還不是說閒話的時候。能否麻煩你回島上來一趟？");
    } else if (status == 1) {
        qm.forceStartQuest(21200, "3");
        qm.sendOk("#b保管在#m140000000##k的你的#b#p1201001##k突然出現了奇怪的反應。據說長矛在呼喚自己主人的時候才會發出那樣的反應。#b也許有什麼事情要轉達給你？#k請速回島上一趟吧。");
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 11) {
            qm.sendNext("你這傢伙！好歹也要努力傳奇一下吧？");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
        qm.sendNextS("嗡嗡嗡嗡嗡……", 2);
    } else if (status == 1) {
        qm.sendNextPrevS("#b（#p1201001#在發出嗡鳴聲。奇怪，那邊的少年是誰？）#k", 2);
    } else if (status == 2) {
        qm.sendNextPrevS("#b（以前沒見過他啊？怎麼看起來不太像人類？）#k", 2);
    } else if (status == 3) {
        qm.sendNextPrev("喂！狂狼勇士！還聽不見我的聲音嗎？到底聽不聽得見？唉，煩死了！");
    } else if (status == 4) {
        qm.sendNextPrevS("#b（咦？這是誰的聲音？怎麼聽起來像個凶巴巴的少年……）#k", 2);
    } else if (status == 5) {
        qm.sendNextPrev("唉……哪有這樣的主人啊？丟開武器在冰窟裡睡了幾百年，現在連話都聽不懂了……");
    } else if (status == 6) {
        qm.sendNextPrevS("你是誰啊？", 2);
    } else if (status == 7) {
        qm.sendNextPrev("啊，狂狼勇士？現在聽到我的聲音了？是我啊，不記得我了？我就是武器#b長矛 #p1201002##k啊？");
    } else if (status == 8) {
        qm.sendNextPrevS("#b（……#p1201002#？#p1201001#會說話？）#k", 2);
    } else if (status == 9) {
        qm.sendNextPrev("不至於吧？這麼吃驚？再怎麼失憶，總不能連我都忘了吧？太不夠意思了！");
    } else if (status == 10) {
        qm.sendNextPrevS("不好意思，真的一點都想不起來。", 2);
    } else if (status == 11) {
        qm.sendYesNo("說聲不好意思就能算了？！幾百年來就我一個人孤苦伶仃地，有多寂寞你知道嗎？不管怎樣，你快點給我想起來！");
    } else if (status == 12) {
        qm.sendNextS("#b（一口一個自己是#p1201001#、#p1201002#的，還越說越生氣了。再這麼說下去也不會有啥進展，還是先走到 #p1201000#跟前，好好商量商量。）#k", 2);
        qm.forceCompleteQuest();
    } else if (status == 13) {
        qm.MovieClipIntroUI(true);
        qm.warp(914090200, 0);
        qm.dispose();
    }
}