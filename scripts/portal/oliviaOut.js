function enter(pi) {
    if (pi.getPlayer().getEventInstance() != null && pi.getPlayer().getEventInstance().getProperty("stage").equals("1") && pi.getPlayer().getMap().getAllMonstersThreadsafe().size() == 0) {
		var s = parseInt(pi.getPlayer().getEventInstance().getProperty("mode"));
		pi.gainExp((s == 0 ? 1500 : (s == 1 ? 5500 : 16000)));
		pi.warp(682000000,0);
    } else {
		pi.playerMessage(5, "The boss is not summoned or defeated yet.");
	}
    
}