package server;

import client.MapleCharacter;
import constants.GameConstants;
import handling.world.World;
import tools.packet.MaplePacketCreator;

public class MapleAchievement {
    private String name;
    private int reward;
    private boolean notice;

    public MapleAchievement(String name, int reward) {
        this.name = name;
        this.reward = reward;
        this.notice = true;
    }

    public MapleAchievement(String name, int reward, boolean notice) {
        this.name = name;
        this.reward = reward;
        this.notice = notice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public boolean getNotice() {
        return notice;
    }

    public void finishAchievement(MapleCharacter chr) {
        chr.modifyCSPoints(1, reward, false);
        chr.setAchievementFinished(MapleAchievements.getInstance().getByMapleAchievement(this));
        if (notice && !chr.isGM()) {
            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[Achievement] Congratulations to " + chr.getName() + " on " + name + " and rewarded with " + (GameConstants.GMS ? (reward/2) : reward) + " cash!"));
        } else {
            chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[Achievement] You've gained " + (GameConstants.GMS ? (reward/2) : reward) + " Cash as you " + name + "."));
        }
    }
}

