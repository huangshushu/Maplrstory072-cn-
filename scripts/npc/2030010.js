/* Amon
 * Last Mission : Zakum's Altar (280030000)
 */

function start() {
    cm.sendYesNo("你想要离开这里到外面去吗？");
}

function action(mode, type, selection) {
    if (mode == 1) {
	cm.warp(211042300);
    }
    cm.dispose();
}