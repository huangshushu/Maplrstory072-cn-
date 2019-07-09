var status = 0;
var request;

function start() {
    status = -1;
    action(1, 0, 0);
}


function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        status = 0;
    if (status == 0) {
        request = cm.getNextCarnivalRequest();
        if (request != null) {
            cm.sendYesNo(request.getChallengeInfo() + "\r\n是否想跟他們挑戰??");
        } else {
            cm.dispose();
        }
    } else if (status == 1) {
        var pt = cm.getPlayer().getParty();
        if (checkLevelsAndMap(30, 50) == 1) {
            cm.sendOk("隊伍裡有人等級不符合。");
            cm.dispose();
        } else if (checkLevelsAndMap(30, 50) == 2) {
            cm.sendOk("在地圖上找不到您的隊友。");
            cm.dispose();
        } else if (pt.getMembers().size() < 1) {
            cm.sendOk("需要 2 人以上才可以擂台！！");
            cm.dispose();
        } else if (request.getChallenger().getParty().getMembers().size() != pt.getMembers().size()) {
            cm.sendOk("對方人數不符！！");
            cm.dispose();
        } else {
            try {
                cm.getChar().getEventInstance().registerCarnivalParty(request.getChallenger(), request.getChallenger().getMap(), 1);
                cm.dispose();
            } catch (e) {
                cm.sendOk("目前挑戰不再是有效的。");
            }
            status = -1;
        }
    }
}

function checkLevelsAndMap(lowestlevel, highestlevel) {
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var channel = cm.getChannelNumber();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) {
        var cPlayer = it.next();
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
            valid = 1;
        }
        if (cPlayer.getMapid() != mapId && (cPlayer.getChannel() != channel) && !cPlayer.isOnline()) {
            valid = 2;
        }
    }
    return valid;
}