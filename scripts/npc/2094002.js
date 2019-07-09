var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (cm.getPlayer().getMapId() == 925100700) {
	cm.removeAll(4001117);
	cm.removeAll(4001120);
	cm.removeAll(4001121);
	cm.removeAll(4001122);
	cm.warp(251010404,0);
	cm.dispose();
	return;
    }
    var em = cm.getEventManager("Pirate");
    if (em == null) {
	cm.sendNext("发生未知错误，请稍后再试。");
	cm.dispose();
	return;
    }
    if (!cm.isLeader()) {
	cm.sendNext("请叫队长与我对话。");
	cm.dispose();
	return;
    }
    switch(cm.getPlayer().getMapId()) {
	case 925100000:
	   cm.sendNext("我们正在进入海盗内部，请把所有怪物消灭。");
	   cm.dispose();
	   break;
	case 925100100:
	   var emp = em.getProperty("stage2");
	   if (emp == null) {
		em.setProperty("stage2", "0");
		emp = "0";
	   }
	   if (emp.equals("0")) {
		if (cm.haveItem(4001120,20)) {
		    cm.sendNext("优秀! 接下来我要#t4001121# #b20个#k");
		    cm.gainItem(4001120,-20);
		    em.setProperty("stage2", "1");
		} else {
	   	    cm.sendNext("请给我20个#t4001120#。");
		}
	   } else if (emp.equals("1")) {
		if (cm.haveItem(4001121,20)) {
		    cm.sendNext("优秀！ 接下来我要#t4001122# #b20个#k");
		    cm.gainItem(4001121,-20);
		    em.setProperty("stage2", "2");
		} else {
	   	    cm.sendNext("请给我20个#t4001121#。");
		}
	   } else if (emp.equals("2")) {
		if (cm.haveItem(4001122,20)) {
		    cm.sendNext("完成，下一阶段已开启。");
		    cm.gainItem(4001122,-20);
		    em.setProperty("stage2", "3");
		} else {
	   	    cm.sendNext("请给我20个#t4001122#。");
		}
	   } else {
		cm.sendNext("完成，下一阶段已开启。");
	   }
	   cm.dispose();
	   break;
	case 925100200:
	   cm.sendNext("突击海盜船，我们必须先消灭守卫。");
	   cm.dispose();
	   break;
	case 925100300:
	   cm.sendNext("突击海盜船，我们必须先消灭守卫。");
	   cm.dispose();
	   break;
	case 925100201:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.sendNext("Excellent.");
		if (em.getProperty("stage2a").equals("0")) {
		    cm.getMap().setReactorState();
		    em.setProperty("stage2a", "1");
		}
	   } else {
	   	cm.sendNext("他们是在躲藏。我们一定要解放他们。");
	   }
	   cm.dispose();
	   break;
	case 925100301:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.sendNext("优秀。");
		if (em.getProperty("stage3a").equals("0")) {
		    cm.getMap().setReactorState();
		    em.setProperty("stage3a", "1");
		}
	   } else {
	   	cm.sendNext("他们是在躲藏。我们一定要解放他们。");
	   }
	   cm.dispose();
	   break;
	case 925100202:
	case 925100302:
	   cm.sendNext("他们是在躲藏。我们一定要解放他们。");
	   cm.dispose();
	   break;
	case 925100400:
	   cm.sendNext("打倒这些海盗，然后把他们的钥匙放到门口就行了。");
	   cm.dispose();
	   break;
	case 925100500:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.warpParty(925100600);
	   } else {
	   	cm.sendNext("请打败地图上的所有怪物！");
	   }
	   cm.dispose();
	   break;
    }
}