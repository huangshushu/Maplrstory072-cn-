package handling.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import server.maps.AnimatedMapleMapObject;
import server.movement.*;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;

public class MovementParse {//1 = player, 2 = mob, 3 = pet, 4 = summon, 5 = dragon

    public static List<LifeMovementFragment> parseMovement(final LittleEndianAccessor lea, final int kind, Point Pos) {
        final List<LifeMovementFragment> res = new ArrayList<>();
        final byte numCommands = lea.readByte();

        for (byte i = 0; i < numCommands; i++) {
            final byte command = lea.readByte();
            switch (command) {
                case 0:
                case 5:
                case 0xF:
                case 0x11: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    short fh = 0;
                    if (command == 0xF) {
                        fh = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setFh(fh);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 6:
                case 0xC:
                case 0xD:
                case 0x10:
                case 0x12: {
                    final short xmod = lea.readShort();
                    final short ymod = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(Pos.x, Pos.y), duration, newstate);
                    rlm.setPixelsPerSecond(new Point(xmod, ymod));
                    res.add(rlm);
                    break;
                }
                case 3:
                case 4:
                case 7:
                case 8:
                case 9:
                case 0xB: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), duration, newstate);
                    tm.setFh(fh);

                    res.add(tm);
                    break;
                }
                case 0x0E: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final ChairMovement cm = new ChairMovement(command, new Point(Pos.x, Pos.y), duration, newstate);
                    cm.setPixelsPerSecond(new Point(xpos, ypos));
                    cm.setUnk(unk);
                    res.add(cm);
                    break;
                }
                case 0xA: { // Update Equip or Dash
                    res.add(new ChangeEquipSpecialAwesome(command, lea.readByte()));
                    break;
                }
                default:
                    System.out.println("Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    FileoutputUtil.log(FileoutputUtil.Movement_Log, "Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    return null;
            }
        }
        double skip = lea.readByte();
        skip = Math.ceil(skip / 2.0D);
        lea.skip((int) skip);
        if (numCommands != res.size()) {
            System.out.println("循环次数[" + numCommands + "]和实际上获取的循环次数[" + res.size() + "]不符");
            FileoutputUtil.log("logs/移动封包出错.txt", "循环次数[" + numCommands + "]和实际上获取的循环次数[" + res.size() + "]不符 " + "移动封包 剩余次数: " + (numCommands - res.size()) + "  封包: " + lea.toString(true));
            return null; // Probably hack
        }
        return res;
    }

    public static void updatePosition(final List<LifeMovementFragment> movement, final AnimatedMapleMapObject target, final int yoffset) {
        if (movement == null) {
            return;
        }
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    final Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
