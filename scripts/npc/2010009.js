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

/**
 * Guild Alliance NPC
 */

var status;
var choice;
var guildName;
var partymembers;

function start() {
	//cm.sendOk("The Guild Alliance is currently under development.");
	//cm.dispose();
	partymembers = cm.getPartyMembers();
	status = -1;
	action(1,0,0);
}

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		return;
	}
	if (status == 0) {
		cm.sendSimple("您好!我叫#b蕾那丽#k。\r\n#b#L0#请告诉我家族联盟是什么？#l\r\n#L1#要成立家族联盟的话应该怎么做？#l\r\n#L2#我想成立家族联盟。#l\r\n#L3#我想增加家族联盟的家族数量。#l\r\n#L4#我想解散家族联盟。#l");
	} else if (status == 1) {
		choice = selection;
	    if (selection == 0) {
		    cm.sendOk("家族联盟仅仅只是说。家族的一些行业联盟。联合在一起。形成一个庞大的家族。我负责管理这些联盟家族。");
			cm.dispose();
		} else if (selection == 1) {
			cm.sendOk("如果要创建家族联盟，必须由2个家族的族长组队。组对的组队长将成为新建家族联盟的族长。");
			cm.dispose();
		} else if(selection == 2) {
			if (cm.getPlayer().getParty() == null || partymembers == null || partymembers.size() != 2 || !cm.isLeader()) {
				cm.sendOk("请确认你的组队中只有2名玩家或和要联盟的家族族长组队后再和我说话。"); //Not real text
				cm.dispose();
			} else if (partymembers.get(0).getGuildId() <= 0 || partymembers.get(0).getGuildRank() > 1) {
				cm.sendOk("你不能创建家族联盟。因为你没有家族。");
				cm.dispose();
			} else if (partymembers.get(1).getGuildId() <= 0 || partymembers.get(1).getGuildRank() > 1) {
				cm.sendOk("你的组队中似乎有一位成员没有家族。");
				cm.dispose();
			} else {
				var gs = cm.getGuild(cm.getPlayer().getGuildId());
				var gs2 = cm.getGuild(partymembers.get(1).getGuildId());
				if (gs.getAllianceId() > 0) {
					cm.sendOk("你已经存另一家族联盟中。因此，不能再继续创建。");
					cm.dispose();
				} else if (gs2.getAllianceId() > 0) {
					cm.sendOk("你的组队中的成员已经是另一家族联盟的成员。");
					cm.dispose();
				} else if (cm.partyMembersInMap() < 2) {
					cm.sendOk("请确保你组队中的另一名玩家和你在同一地图。");
					cm.dispose();
				} else
                			cm.sendYesNo("噢~！你有兴趣创建一个家族联盟？");
			}
		} else if (selection == 3) {
			if (cm.getPlayer().getGuildRank() == 1 && cm.getPlayer().getAllianceRank() == 1) {
				cm.sendYesNo("增加联盟家族数量需要支付20,000,000金币. 你确定要继续吗?"); //ExpandGuild Text
			} else {
			    cm.sendOk("只有联盟队长才可以增加联盟家族数量。");
				cm.dispose();
			}
		} else if(selection == 4) {
			if (cm.getPlayer().getGuildRank() == 1 && cm.getPlayer().getAllianceRank() == 1) {
				cm.sendYesNo("你确定要解散你的家族联盟？");
			} else {
				cm.sendOk("只有联盟队长才可以解散家族联盟。");
				cm.dispose();
			}
		}
	} else if(status == 2) {
	    if (choice == 2) {
		    cm.sendGetText("请输入想要创建家族联盟的名称。(最多13个字节)");
		} else if (choice == 3) {
			if (cm.getPlayer().getGuildId() <= 0) {
				cm.sendOk("你不能增加不存在的家族联盟。");
				cm.dispose();
			} else {
				if (cm.addCapacityToAlliance()) {
					cm.sendOk("你成功的增加了联盟家族数量。");
				} else {
					cm.sendOk("很抱歉，由于你的联盟家族数量已满，不能再继续增加。");
				}
				cm.dispose();
			}
		} else if (choice == 4) {
			if (cm.getPlayer().getGuildId() <= 0) {
				cm.sendOk("你不能解散不存在的家族联盟。");
				cm.dispose();
			} else {
				if (cm.disbandAlliance()) {
					cm.sendOk("家族联盟已经被解散。如果需要再次创建，请再和我说话。");
				} else {
					cm.sendOk("解散家族联盟出错，请稍后再试。");
				}
				cm.dispose();
			}
		}
	} else if (status == 3) {
		guildName = cm.getText();
	    cm.sendYesNo("你确定使用#b"+ guildName + "#k做为家族联盟的名称吗？");
	} else if (status == 4) {
			if (!cm.createAlliance(guildName)) {
				cm.sendNext("这个名称不能被使用，请尝试其他名称。"); //Not real text
				status = 1;
				choice = 2;
			} else
				cm.sendOk("成功创建了家族联盟。");
			cm.dispose();
	}
}