/*
	NPC Name: 		Adobis
	Map(s): 		El Nath : Entrance to Zakum Altar
	Description: 		Zakum battle starter
*/
var status = 0;

function action(mode, type, selection) {
	if (cm.getPlayer().getMapId() == 211042200) {
		if (selection < 100) {
			cm.sendSimple("#r#L100#Zakum#l\r\n#L101#Chaos Zakum#l");
		} else {
			if (selection == 100) {
				cm.warp(211042300,0);
			} else if (selection == 101) {
				cm.warp(211042301,0);
			}
			cm.dispose();
		}
		return;
	} else if (cm.getPlayer().getMapId() == 211042401) {
    switch (status) {
	case 0:
		if (cm.getPlayer().getLevel() < 100) {
			cm.sendOk("你的等级必须达到100级以上.");
			cm.dispose();
			return;
		}
		if (cm.getPlayer().getClient().getChannel() != 1 && cm.getPlayer().getClient().getChannel() != 2) {
			cm.sendOk("Chaos Zakum is not available till Magic is fixed.");
			cm.dispose();
			return;
		}
	    var em = cm.getEventManager("ChaosZakum");

	    if (em == null) {
		cm.sendOk("The event isn't started, please contact a GM.");
		cm.safeDispose();
		return;
	    }
	var prop = em.getProperty("state");
	    var marr = cm.getQuestRecord(160102);
	    var data = marr.getCustomData();
	    if (data == null) {
		marr.setCustomData("0");
	        data = "0";
	    }
	    var time = parseInt(data);
	if (prop == null || prop.equals("0")) {
	    var squadAvailability = cm.getSquadAvailability("ChaosZak");
	    if (squadAvailability == -1) {
		status = 1;
	    if (time + (12 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Chaos Zakum in the past 12 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (12 * 3600000)));
		cm.dispose();
		return;
	    }
		cm.sendYesNo("你有兴趣成为远征队的队长吗？");

	    } else if (squadAvailability == 1) {
	    if (time + (12 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Chaos Zakum in the past 12 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (12 * 3600000)));
		cm.dispose();
		return;
	    }
		// -1 = Cancelled, 0 = not, 1 = true
		var type = cm.isSquadLeader("ChaosZak");
		if (type == -1) {
		    cm.sendOk("远征队已经结束，请重新登记。");
		    cm.safeDispose();
		} else if (type == 0) {
		    var memberType = cm.isSquadMember("ChaosZak");
		    if (memberType == 2) {
			cm.sendOk("你被禁止参加远征队。");
			cm.safeDispose();
		    } else if (memberType == 1) {
			status = 5;
			cm.sendSimple("你想做什么? \r\n#b#L0#查看远征队#l \r\n#b#L1#加入远征队#l \r\n#b#L2#退出远征队#l");
		    } else if (memberType == -1) {
			cm.sendOk("远征队已经结束，请重新登记。");
			cm.safeDispose();
		    } else {
			status = 5;
			cm.sendSimple("你想做什么?\r\n#b#L0#查看远征队#l \r\n#b#L1#加入远征队#l \r\n#b#L2#退出远征队#l");
		    }
		} else { // Is leader
		    status = 10;
		    cm.sendSimple("你想做什么? \r\n#b#L0#查看远征队#l \r\n#b#L1#移除远征队员#l \r\n#b#L2#编辑限制列表#l \r\n#r#L3#开始挑战扎昆#l");
		// TODO viewing!
		}
	    } else {
			var eim = cm.getDisconnected("ChaosZakum");
			if (eim == null) {
				var squd = cm.getSquad("ChaosZak");
				if (squd != null) {
	    if (time + (12 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Chaos Zakum in the past 12 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (12 * 3600000)));
		cm.dispose();
		return;
	    }
					cm.sendYesNo("对不起,里面已经开始了挑战扎昆的战斗!\r\n" + squd.getNextPlayer());
					status = 3;
				} else {
					cm.sendOk("对不起,里面已经开始了挑战扎昆的战斗!");
					cm.safeDispose();
				}
			} else {
				cm.sendYesNo("你愿意加入你的远征队的战斗中吗?");
				status = 2;
			}
	    }
	} else {
			var eim = cm.getDisconnected("ChaosZakum");
			if (eim == null) {
				var squd = cm.getSquad("ChaosZak");
				if (squd != null) {
	    if (time + (12 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Chaos Zakum in the past 12 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (12 * 3600000)));
		cm.dispose();
		return;
	    }
					cm.sendYesNo("对不起,里面已经开始了挑战扎昆的战斗!\r\n" + squd.getNextPlayer());
					status = 3;
				} else {
					cm.sendOk("对不起,里面已经开始了挑战扎昆的战斗!");
					cm.safeDispose();
				}
			} else {
				cm.sendYesNo("你愿意加入你的远征队的战斗中吗?");
				status = 2;
			}
	}
	    break;
	case 1:
	    	if (mode == 1) {
			if (cm.registerSquad("ChaosZak", 5, " has been named the Leader of the squad (Chaos). If you would you like to join please register for the Expedition Squad within the time period.")) {
				cm.sendOk("You have been named the Leader of the Squad. For the next 5 minutes, you can add the members of the Expedition Squad.");
			} else {
				cm.warp(280030000,1);
			}
	    	} else {
			cm.sendOk("如果你想成为远征队队长的话，请跟我谈谈.")
	    	}
	    cm.safeDispose();
	    break;
	case 2:
		if (!cm.reAdd("ChaosZakum", "ChaosZak")) {
			cm.sendOk("错误... 请再试一次.");
		}
		cm.dispose();
		break;
	case 3:
		if (mode == 1) {
			var squd = cm.getSquad("ChaosZak");
			if (squd != null && !squd.getAllNextPlayer().contains(cm.getPlayer().getName())) {
				squd.setNextPlayer(cm.getPlayer().getName());
				cm.sendOk("你已经保留了现场.");
			}
		}
		cm.dispose();
		break;
	case 5:
	    if (selection == 0) {
		if (!cm.getSquadList("ChaosZak", 0)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		    cm.safeDispose();
		} else {
		    cm.dispose();
		}
	    } else if (selection == 1) { // join
		var ba = cm.addMember("ChaosZak", true);
		if (ba == 2) {
		    cm.sendOk("队伍已满，请稍后再试。");
		    cm.safeDispose();
		} else if (ba == 1) {
		    cm.sendOk("你已成功加入队伍。");
		    cm.safeDispose();
		} else {
		    cm.sendOk("你已加入队伍。");
		    cm.safeDispose();
		}
	    } else {// withdraw
		var baa = cm.addMember("ChaosZak", false);
		if (baa == 1) {
		    cm.sendOk("你已成功退出队伍。");
		    cm.safeDispose();
		} else {
		    cm.sendOk("你不是队伍中的一员。");
		    cm.safeDispose();
		}
	    }
	    break;
	case 10:
	    if (selection == 0) {
		if (!cm.getSquadList("ChaosZak", 0)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		}
		cm.safeDispose();
	    } else if (selection == 1) {
		status = 11;
		if (!cm.getSquadList("ChaosZak", 1)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		cm.safeDispose();
		}

	    } else if (selection == 2) {
		status = 12;
		if (!cm.getSquadList("ChaosZak", 2)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		cm.safeDispose();
		}

	    } else if (selection == 3) { // get insode
		if (cm.getSquad("ChaosZak") != null) {
		    var dd = cm.getEventManager("ChaosZakum");
		    dd.startInstance(cm.getSquad("ChaosZak"), cm.getMap(), 160102);
		    cm.dispose();
		} else {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		    cm.safeDispose();
		}
	    }
	    break;
	case 11:
	    cm.banMember("ChaosZak", selection);
	    cm.dispose();
	    break;
	case 12:
	    if (selection != -1) {
		cm.acceptMember("ChaosZak", selection);
	    }
	    cm.dispose();
	    break;
    }
	} else {
    switch (status) {
	case 0:
		if (cm.getPlayer().getLevel() < 50) {
			cm.sendOk("等级必须大于50级才能挑战扎昆");
			cm.dispose();
			return;
		}
		if (cm.getPlayer().getClient().getChannel() != 1 && cm.getPlayer().getClient().getChannel() != 2 && cm.getPlayer().getClient().getChannel() != 4) {
			cm.sendOk("挑战扎昆只能在1，2，4频道。");
			cm.dispose();
			return;
		}
	    var em = cm.getEventManager("ZakumBattle");

	    if (em == null) {
		cm.sendOk("发生未知错误，请稍后再试。");
		cm.safeDispose();
		return;
	    }
	var prop = em.getProperty("state");
	    var marr = cm.getQuestRecord(160101);
	    var data = marr.getCustomData();
	    if (data == null) {
		marr.setCustomData("0");
	        data = "0";
	    }
	    var time = parseInt(data);
	if (prop == null || prop.equals("0")) {
	    var squadAvailability = cm.getSquadAvailability("ZAK");
	    if (squadAvailability == -1) {
		status = 1;
	    if (time + (1 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Zakum in the past 6 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (6 * 360000)));
		cm.dispose();
		return;
	    }
		cm.sendYesNo("你有兴趣成为远征队的队长吗？");

	    } else if (squadAvailability == 1) {
	    if (time + (1 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Zakum in the past 6 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (6 * 360000)));
		cm.dispose();
		return;
	    }
		// -1 = Cancelled, 0 = not, 1 = true
		var type = cm.isSquadLeader("ZAK");
		if (type == -1) {
		    cm.sendOk("远征队已结束，请重新登记。");
		    cm.safeDispose();
		} else if (type == 0) {
		    var memberType = cm.isSquadMember("ZAK");
		    if (memberType == 2) {
			cm.sendOk("你被禁止参加远征队。");
			cm.safeDispose();
		    } else if (memberType == 1) {
			status = 5;
			cm.sendSimple("你想做什么?\r\n#b#L0#查看远征队#l \r\n#b#L1#加入远征队#l \r\n#b#L2#退出远征队#l");
		    } else if (memberType == -1) {
			cm.sendOk("远征队已结束，请重新登记。.");
			cm.safeDispose();
		    } else {
			status = 5;
			cm.sendSimple("你想做什么? \r\n#b#L0#查看远征队#l \r\n#b#L1#加入远征队#l \r\n#b#L2#退出远征队#l");
		    }
		} else { // Is leader
		    status = 10;
		    cm.sendSimple("你想做什么?\r\n#b#L0#查看远征队#l \r\n#b#L1#移除远征队成员#l \r\n#b#L2#编辑限制列表#l \r\n#r#L3#开始挑战扎昆#l");
		// TODO viewing!
		}
	    } else {
			var eim = cm.getDisconnected("ZakumBattle");
			if (eim == null) {
				var squd = cm.getSquad("ZAK");
				if (squd != null) {
	    if (time + (1 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Zakum in the past 6 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (6 * 360000)));
		cm.dispose();
		return;
	    }
					cm.sendYesNo("对不起,里面已经开始了挑战扎昆的战斗!\r\n" + squd.getNextPlayer());
					status = 3;
				} else {
					cm.sendOk("对不起,里面已经开始了挑战扎昆的战斗!");
					cm.safeDispose();
				}
			} else {
				cm.sendYesNo("你愿意加入你的远征队的战斗中吗?");
				status = 1;
			}
	    }
	} else {
			var eim = cm.getDisconnected("ZakumBattle");
			if (eim == null) {
				var squd = cm.getSquad("ZAK");
				if (squd != null) {
	    if (time + (1 * 3600000) >= cm.getCurrentTime() && !cm.getPlayer().isGM()) {
		cm.sendOk("You have already went to Zakum in the past 6 hours. Time left: " + cm.getReadableMillis(cm.getCurrentTime(), time + (6 * 360000)));
		cm.dispose();
		return;
	    }
					cm.sendYesNo("对不起,里面已经开始了挑战扎昆的战斗!\r\n" + squd.getNextPlayer());
					status = 3;
				} else {
					cm.sendOk("对不起,里面已经开始了挑战扎昆的战斗!");
					cm.safeDispose();
				}
			} else {
				cm.sendYesNo("你愿意加入你的远征队的战斗中吗?");
				status = 1;
			}
	}
	    break;
	case 1:
	    	if (mode == 1) {
			if (cm.registerSquad("ZAK", 5, " 已被任命为远征队队长（定期）。如果你想加入请在时间段内登记的远征队.")) {
				cm.sendOk("你已经被任命为远征队队长。在接下来的5分钟，你可以管理远征队的成员。");
			} else {
				cm.sendOk("管理远征队时，发生一个错误。");
			}
	    	} else {
			cm.sendOk("如果你想成为远征队队长的话，来跟我谈谈.")
	    	}
	    cm.safeDispose();
	    break;
	case 2:
		if (!cm.reAdd("ZakumBattle", "ZAK")) {
			cm.sendOk("错误... 请稍后再试。");
		}
		cm.safeDispose();
		break;
	case 3:
		if (mode == 1) {
			var squd = cm.getSquad("ZAK");
			if (squd != null && !squd.getAllNextPlayer().contains(cm.getPlayer().getName())) {
				squd.setNextPlayer(cm.getPlayer().getName());
				cm.sendOk("你已经保留了现场.");
			}
		}
		cm.dispose();
		break;
	case 5:
	    if (selection == 0) {
		if (!cm.getSquadList("ZAK", 0)) {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		    cm.safeDispose();
		} else {
		    cm.dispose();
		}
	    } else if (selection == 1) { // join
		var ba = cm.addMember("ZAK", true);
		if (ba == 2) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		    cm.safeDispose();
		} else if (ba == 1) {
		    cm.sendOk("你已成功加入队伍。");
		    cm.safeDispose();
		} else {
		    cm.sendOk("你已是队伍的一部分。");
		    cm.safeDispose();
		}
	    } else {// withdraw
		var baa = cm.addMember("ZAK", false);
		if (baa == 1) {
		    cm.sendOk("你已成功的退出了队伍。");
		    cm.safeDispose();
		} else {
		    cm.sendOk("你不是队伍中的一员。");
		    cm.safeDispose();
		}
	    }
	    break;
	case 10:
	    if (selection == 0) {
		if (!cm.getSquadList("ZAK", 0)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		}
		cm.safeDispose();
	    } else if (selection == 1) {
		status = 11;
		if (!cm.getSquadList("ZAK", 1)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		cm.safeDispose();
		}

	    } else if (selection == 2) {
		status = 12;
		if (!cm.getSquadList("ZAK", 2)) {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		cm.safeDispose();
		}

	    } else if (selection == 3) { // get insode
		if (cm.getSquad("ZAK") != null) {
		    var dd = cm.getEventManager("ZakumBattle");
		    dd.startInstance(cm.getSquad("ZAK"), cm.getMap(), 160101);
		    cm.dispose();
		} else {
		    cm.sendOk("由于未知的错误，对队伍的要求被拒绝了.");
		    cm.safeDispose();
		}
	    }
	    break;
	case 11:
	    cm.banMember("ZAK", selection);
	    cm.dispose();
	    break;
	case 12:
	    if (selection != -1) {
		cm.acceptMember("ZAK", selection);
	    }
	    cm.dispose();
	    break;
    }
	}
}