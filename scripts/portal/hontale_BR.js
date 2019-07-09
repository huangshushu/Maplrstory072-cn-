function enter(pi) {

    var map = pi.getMapId();
    if (map == 240060000) {
        var em = pi.getEventManager("HorntailBattle");
        if (pi.getMap().getAllMonstersThreadsafe().size() == 0) {
            em.setProperty("state", "2");
        }
        if (em != null && em.getProperty("state").equals("2")) {
            em.warpAllPlayer(240060000, 240060100);
        } else {
            pi.playerMessage("传送口尚未启动.");
        }
    } else if (map == 240060100) {
        var em = pi.getEventManager("HorntailBattle");
        if (pi.getMap().getAllMonstersThreadsafe().size() == 0) {
            em.setProperty("state", "3");
        }
        if (em != null && em.getProperty("state").equals("3")) {
            em.warpAllPlayer(240060100, 240060200);
        } else {
            pi.playerMessage("传送口尚未启动.");
        }
    }
}