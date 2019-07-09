function enter(pi) {
	pi.warp(610030800, 0);
    if (pi.getPlayer().getEventInstance() != null && pi.getPlayer().getEventInstance().getName().startsWith("CWKPQ")) {
	pi.getPlayer().finishAchievement(37);
    }
}