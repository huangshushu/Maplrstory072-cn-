/*
	Red Sign - 101st Floor Eos Tower (221024500)
*/

var status = -1;
var minLevel = 30; // 35
var maxLevel = 200; // 65

var minPartySize = 5; //CHANGE after BB
var maxPartySize = 6;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	    return;
	}
	status--;
    }
	cm.removeAll(4001022);
	cm.removeAll(4001023);
	if (cm.getParty() == null) { // No Party
	    cm.sendOk("进入需要: #r\r\n" + minPartySize + " 位组员.最低 " + minLevel + " 级,最高 " + maxLevel + "级.");
	} else if (!cm.isLeader()) { // Not Party Leader
	    cm.sendOk("如果你想做任务，请让#b队长#k跟我谈.#b");
	} else {
	    // Check if all party members are within PQ levels
	    var party = cm.getParty().getMembers();
	    var mapId = cm.getMapId();
	    var next = true;
	    var levelValid = 0;
	    var inMap = 0;
	    var it = party.iterator();

	    while (it.hasNext()) {
		var cPlayer = it.next();
		if ((cPlayer.getLevel() >= minLevel) && (cPlayer.getLevel() <= maxLevel)) {
		    levelValid += 1;
		} else {
		    next = false;
		}
		if (cPlayer.getMapid() == mapId) {
		    inMap += (cPlayer.getJobId() == 900 ? 6 : 1);
		}
	    }
	    if (party.size() > maxPartySize || inMap < minPartySize) {
		next = false;
	    }
	    if (next) {
		var em = cm.getEventManager("LudiPQ");
		if (em == null) {
		    cm.sendOk("发生未知错误，请稍后再试。");
		} else {
		    var prop = em.getProperty("state");
		    if (prop.equals("0") || prop == null) {
			em.startInstance(cm.getParty(), cm.getMap(), 70);
			cm.removeAll(4001022);
			cm.removeAll(4001023);
			cm.dispose();
			return;
		    } else {
			cm.sendOk("其他队伍已经在里面做#r组队任务了#k请尝试换频道或者等其他队伍完成。");
		    }
		}
	    } else {
		cm.sendOk("你好，你想挑战这个组队任务吗？进入受到了限制，请确定你的组队时这样完整的。进入需要: #r\r\n" + minPartySize + " 位组员.最低 " + minLevel + " 级,最高 " + maxLevel + "级.");
	    }
	}
	cm.dispose();
}