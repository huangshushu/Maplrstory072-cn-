function enter(pi) {
    if (pi.getPlayer().getEventInstance() != null) {
    	pi.gainExp_PQ(200, 1.5);
	if (pi.canHold(4001530, 1) ) {
	    pi.gainItem(4001530, 1);
	}
    }
    pi.warp(211000002,0);
}