
function action(mode, type, selection) {
    cm.getPlayer().removeAll(4001101);
    cm.getPlayer().removeAll(4001095);
    cm.getPlayer().removeAll(4001096);
    cm.getPlayer().removeAll(4001097);
    cm.getPlayer().removeAll(4001098);
    cm.getPlayer().removeAll(4001099);
    cm.getPlayer().removeAll(4001100);
    if (cm.getPlayer().getMapId() == 910010200) {
        cm.warp(100000200);
    } else {
        cm.warp(910010200);
    }
    cm.dispose();
}