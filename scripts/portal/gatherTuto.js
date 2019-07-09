function enter(pi) {
    if (pi.haveItem(4001480)) {
	pi.warp(910001005,0);
	pi.gainItem(4001480, -1);
    } else if (pi.haveItem(4001481)) {
	pi.warp(910001006, 0);
	pi.gainItem(4001481, -1);
    } else if (pi.haveItem(4001482)) {
	pi.warp(910001003, 0);
	pi.gainItem(4001482, -1);
    } else if (pi.haveItem(4001483)) {
	pi.warp(910001004, 0);
	pi.gainItem(4001483, -1);
    } else if (pi.haveItem(4001569)) {
	pi.warp(910001007, 0);
	pi.gainItem(4001569, -1);
    } else if (pi.haveItem(4001570)) {
	pi.warp(910001008, 0);
	pi.gainItem(4001570, -1);
    } else if (pi.haveItem(4001571)) {
	pi.warp(910001009, 0);
	pi.gainItem(4001571, -1);
    } else if (pi.haveItem(4001572)) {
	pi.warp(910001010, 0);
	pi.gainItem(4001572, -1);
    } else if (pi.isQuestActive(3197) || pi.isQuestActive(3198)) {
	pi.warp(910001002, 0);
    } else if (pi.isQuestActive(3195) || pi.isQuestActive(3196)) {
	pi.warp(910001001, 0);
    }
}