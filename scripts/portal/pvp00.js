/*
 Àë¿ª´óÀŞÌ¨
*/

function enter(pi) {
    var returnMap = pi.getSavedLocation("PVP");
    pi.clearSavedLocation("PVP");

    if (returnMap < 0) {
	returnMap = 102000000; // to fix people who entered the fm trough an unconventional way
    }
    var target = pi.getMap(returnMap);
    var portal = target.getPortal("pvp00");


    if (portal == null) {
	portal = target.getPortal(0);
    }
    if (pi.getMapId() != target) {
	pi.getPlayer().changeMap(target, portal);
    }
}