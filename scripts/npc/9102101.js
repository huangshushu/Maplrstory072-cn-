/*
	? - Victoria Road: Pet-Walking Road (100000202)
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
	cm.sendNext("#b(我沒有想太多，所以我沒有去碰它。)");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendYesNo("#b(我能看到的东西在草丛中。我应该把它找出来？)");
    } else if (status == 1) {
	cm.sendNext("#b(呸...这是宠物的便便!)");
	cm.gainItem(4031922, 1);
	cm.dispose();
    }
}