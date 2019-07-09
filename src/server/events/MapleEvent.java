package server.events;

import client.MapleCharacter;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import server.MapleInventoryManipulator;
import server.RandomRewards;
import server.Timer.EventTimer;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.FileoutputUtil;
import tools.packet.MaplePacketCreator;
import tools.Randomizer;
import tools.StringUtil;

public abstract class MapleEvent {
    public abstract void finished(MapleCharacter chr); //most dont do shit here
    public abstract void startEvent();
    protected MapleEventType type;
    protected int channel, playerCount = 0;
    protected boolean isRunning = false;

    public MapleEvent(final int channel, final MapleEventType type) {
        this.channel = channel;
        this.type = type;
    }

    public void incrementPlayerCount() {
	playerCount++;
	if (playerCount == 250) {
	    setEvent(ChannelServer.getInstance(channel), true);
	}
    }

    public MapleEventType getType() {
	return type;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public MapleMap getMap(final int i) {
        return getChannelServer().getMapFactory().getMap(type.mapids[i]);
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    public void broadcast(final byte[] packet) {
        for (int i = 0; i < type.mapids.length; i++) {
            getMap(i).broadcastMessage(packet);
        }
    }

    public static void givePrize(final MapleCharacter chr) {
        final int reward = RandomRewards.getEventReward();
        if (reward == 0) {
            chr.gainMeso(66666, true, false);
            chr.dropMessage(5, "你獲得 66666 楓幣");
        } else if (reward == 1) {
            chr.gainMeso(399999, true, false);
            chr.dropMessage(5, "你獲得 399999 楓幣");
        } else if (reward == 2) {
            chr.gainMeso(666666, true, false);
            chr.dropMessage(5, "你獲得 666666 楓幣");
        } else if (reward == 3) {
            chr.addFame(10);
            chr.dropMessage(5, "你獲得 10 名聲");
        } else {
            int max_quantity = 1;
            switch (reward) {
                case 5062000:
                    max_quantity = 3;
                    break;
                case 5220000:
                    max_quantity = 25;
                    break;
                case 4031307:
                case 5050000:
                    max_quantity = 5;
                    break;
                case 2022121:
                    max_quantity = 10;
                    break;
            }
            final int quantity = (max_quantity > 1 ? Randomizer.nextInt(max_quantity) : 0) + 1;
            if (MapleInventoryManipulator.checkSpace(chr.getClient(), reward, quantity, "")) {
                MapleInventoryManipulator.addById(chr.getClient(), reward, (short) quantity, "Event prize on " + FileoutputUtil.CurrentReadable_Date());
            } else {
                givePrize(chr); //do again until they get
            }
        }
    }

    public void onMapLoad(MapleCharacter chr) { //most dont do shit here
	//if (GameConstants.isEventMap(chr.getMapId()) && FieldLimitType.Event.check(chr.getMap().getFieldLimit()) && FieldLimitType.Event2.check(chr.getMap().getFieldLimit())) {
	//    chr.getClient().getSession().write(MaplePacketCreator.showEventInstructions());
	//}
    }

    public void warpBack(MapleCharacter chr) {
        int map = chr.getSavedLocation(SavedLocationType.EVENT);
        if (map <= -1) {
            map = 104000000;
        }
        final MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(map);
        chr.changeMap(mapp, mapp.getPortal(0));
    }

    public void reset() {
        isRunning = true;
	playerCount = 0;
    }

    public void unreset() {
        isRunning = false;
	playerCount = 0;
    }

    public static final void setEvent(final ChannelServer cserv, final boolean auto) {
        if (auto && cserv.getEvent() > -1) {
            for (MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = cserv.getEvent(t);
                if (e.isRunning) {
                    for (int i : e.type.mapids) {
                        if (cserv.getEvent() == i) {
                            e.broadcast(MaplePacketCreator.serverNotice(0, "距離活動開始只剩一分鐘!"));
                            e.broadcast(MaplePacketCreator.getClock(60));
                            EventTimer.getInstance().schedule(new Runnable() {

                                public void run() {
                                    e.startEvent();
                                }
                            }, 30000);
                            break;
                        }
                    }
                }
            }
        }
        cserv.setEvent(-1);
    }

    public static final void mapLoad(final MapleCharacter chr, final int channel) {
	if (chr == null) {
	    return;
	} //o_o
        for (MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = ChannelServer.getInstance(channel).getEvent(t);
            if (e.isRunning) {
                if (chr.getMapId() == 109050000) { //finished map
                    e.finished(chr);
                }
                for (int i = 0; i < e.type.mapids.length; i++) {
                    if (chr.getMapId() == e.type.mapids[i]) {
                        e.onMapLoad(chr);
			//if (i == 0) { //first map
			    //e.incrementPlayerCount();
			//}
                    }
                }
            }
        }
    }

    public static final void onStartEvent(final MapleCharacter chr) {
        for (MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = chr.getClient().getChannelServer().getEvent(t);
            if (e.isRunning) {
                for (int i : e.type.mapids) {
                    if (chr.getMapId() == i) {
                        e.startEvent();
			setEvent(chr.getClient().getChannelServer(), false);
                        chr.dropMessage(5, String.valueOf(t) + " 活動開始.");
                    }
                }
            }
        }
    }

    public static final String scheduleEvent(final MapleEventType event, final ChannelServer cserv) {
        if (cserv.getEvent() != -1 || cserv.getEvent(event) == null) {
            return "該活動已經被禁止安排了.";
        }
        for (int i : cserv.getEvent(event).type.mapids) {
            if (cserv.getMapFactory().getMap(i).getCharactersSize() > 0) {
                return "該活動已經在執行中.";
            }
        }
        cserv.setEvent(cserv.getEvent(event).type.mapids[0]);
        cserv.getEvent(event).reset();
        //World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活動 " + cserv.getServerName() + "! Let's play a " + StringUtil.makeEnumHumanReadable(event.name()) + " event in channel " + cserv.getChannel() + "! Change to channel " + cserv.getChannel() + " and use @event command!"));
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活動 " + String.valueOf(event) + " 即將在頻道 " + cserv.getChannel() + " 舉行 , 參加指令@event 要參加的玩家請到頻道 " + cserv.getChannel()));
        return "";
    }
}
