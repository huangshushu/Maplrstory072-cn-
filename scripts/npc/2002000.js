/* RED 1st impact
    Rupi
    Made by Daenerys
*/

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else 
        if (status == 0) {
		    cm.sendNext("你还有一些未完成的事，对吗？在走出去之前，在这里放松一下疲惫的心情，这是一个不错的主意.");
            cm.dispose();
        status--;
    }
    if (status == 0) {
	    cm.sendYesNo("你没有别的事要做，嗯？你想回去吗？如果是的话，我可以把你送回去。您是怎么想的？你想回去吗?");
    } else if (status == 1) {
	    cm.warp(101000000);
		cm.dispose();
    }
}