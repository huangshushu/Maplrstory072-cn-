/* 
 *  Witch Tower
 *  Golden Key
 */

function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
	var keys = eim.getProperty("goldkey");
	keys++
	eim.setProperty("goldkey", keys);
	rm. witchTowerKey(keys);
	rm.playerMessage("Acquired key "+keys+".");
	//rm.getPlayer().getMap().startShortMapEffect("Acquired Key number" + keys + ".", 5120030, 6000);
    }
}
