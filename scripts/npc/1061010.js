function start() {
    cm.sendYesNo("(你可以回到原来的世界! 你愿意回去吗?)");
}

function action(mode, type, selection) {
    if (mode == 1) {
	var map = cm.getMapId();
	//var tomap = "211040401";

	if (map == 108010101) {
	    tomap = 100000000;
	} else if (map == 108010201) {
	    tomap = 101000000;
	} else if (map == 108010301) {
	    tomap = 102000000;
	} else if (map == 108010401) {
	    tomap = 103000000;
	} else if (map == 108010501) {
	    tomap = 120000000;
	}
	cm.warp(tomap);
    }
    cm.dispose();
}
