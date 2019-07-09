function act(){

    rm.dropItems();

    var eim = rm.getPlayer().getEventInstance();
    if (eim != null) {
	var newp = parseInt(eim.getProperty("stage2")) + 1;
	if (newp <= 10) {
	    eim.setProperty("stage2", "" + newp);
	    rm.getMap().startSimpleMapEffect("你搜集了 " + newp + " 张次元通行证.", 5120018);
	}
    }
}