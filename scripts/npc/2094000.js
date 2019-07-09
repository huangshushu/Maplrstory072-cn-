

function action(mode, type, selection) {
    cm.removeAll(4001117);
    cm.removeAll(4001120);
    cm.removeAll(4001121);
    cm.removeAll(4001122);
    if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
        cm.sendOk("请让队长来找我谈话。");
    } else {
        var party = cm.getPlayer().getParty().getMembers();
        var mapId = cm.getPlayer().getMapId();
        var next = true;
        var size = 0;
        var it = party.iterator();
        while (it.hasNext()) {
            var cPlayer = it.next();
            var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
            if (ccPlayer == null || ccPlayer.getLevel() < 55 || ccPlayer.getLevel() > 200) {
                next = false;
                break;
            }
            size += (ccPlayer.isGM() ? 4 : 1);
        }
        if (next && size >= 3) {
            var em = cm.getEventManager("Pirate");
            if (em == null) {
                cm.sendOk("发生未知错误，请稍后再试。");
            } else {
                var prop = em.getProperty("state");
                if (prop.equals("0") || prop == null) {
                    em.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap(), 120);
                } else {
                    cm.sendOk("目前有队伍在执行组队任务，请更换频道或继续等候。");
                }
            }
        } else {
            cm.sendOk("需要3个人以上,等级必须是55级到100级。");
        }
    }
    cm.dispose();
}