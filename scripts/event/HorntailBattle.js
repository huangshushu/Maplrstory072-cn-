
function init() {
    // 0 = Not started, 1 = started, 2 = first head defeated, 3 = second head defeated
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}

function setup(level, leaderid) {
    em.setProperty("state", "1");
    em.setProperty("preheadCheck", "1");
    em.setProperty("leader", "true");
    var map = parseInt(240060000);
    var eim = em.newInstance("HorntailBattle");
    for (var i = 0; i < 3; i++) {
        eim.setInstanceMap(map + (i * 100)).resetFully();
    }
    eim.startEventTimer(4500000); //now changed to 1 hour 15 mins
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory().getMap(240060000);
    player.changeMap(map, map.getPortal(0));
}

function changedMap(eim, player, mapid) {
    switch (mapid) {
        case 240060000:
            if (em.getProperty("state").equals("1")&&em.getProperty("preheadCheck").equals("1")) {
                mob = em.getMonster(8810024);
                eim.registerMonster(mob);
		em.setProperty("preheadCheck", "2");
		//eim.spawnMobOnMap(8810000,1,940,230,240060000);
                eim.setInstanceMap(240060000).spawnMonsterOnGroundBelow(mob, new java.awt.Point(940, 230));
            }
            return;
        case 240060100:
            if (em.getProperty("state").equals("2")&&em.getProperty("preheadCheck").equals("2")) {
                mob = em.getMonster(8810025);
                eim.registerMonster(mob);
		em.setProperty("preheadCheck", "3");
		//eim.spawnMobOnMap(8810001,1,940,230,240060100);
                eim.setInstanceMap(240060100).spawnMonsterOnGroundBelow(mob, new java.awt.Point(-400, 230));
            }
            return;
        case 240060200:
            return;
    }
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
        em.setProperty("state", "0");
        em.setProperty("leader", "true");
    }
}

function playerDisconnected(eim, player) {
    return 0;
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 240050400);
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
        em.setProperty("state", "0");
        em.setProperty("leader", "true");
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

function playerRevive(eim, player) {
    return false;
}

function clearPQ(eim) {
}
function leftParty(eim, player) {
}
function disbandParty(eim) {
}
function playerDead(eim, player) {
}
function cancelSchedule() {
}