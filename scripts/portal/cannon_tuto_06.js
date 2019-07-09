function enter(pi) {
	pi.EnableUI(1);
	pi.DisableUI(false);
	pi.sendDirectionStatus(4, 1096003);
	pi.sendDirectionStatus(3, 4);
	pi.openNpc(1096003);
}