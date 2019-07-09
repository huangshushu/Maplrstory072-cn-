package handling.world.guild;

import client.MapleCharacter;

public class MapleGuildCharacter implements java.io.Serializable { // alias for a character
    public static final long serialVersionUID = 2058609046116597760L;
    private byte channel = -1, guildrank, allianceRank;
    private short level;
    private int id, jobid, guildid, guildContribution;
    private boolean online;
    private String name;

    // either read from active character...
    // if it's online
    public MapleGuildCharacter(final MapleCharacter c) {
        name = c.getName();
        level = (short) c.getLevel();
        id = c.getId();
        channel = (byte) c.getClient().getChannel();
        jobid = c.getJob();
        guildrank = c.getGuildRank();
        guildid = c.getGuildId();
	guildContribution = c.getGuildContribution();
        allianceRank = c.getAllianceRank();
        online = true;
    }

    // or we could just read from the database
    public MapleGuildCharacter(final int id, final short lv, final String name, final byte channel, final int job, final byte rank, final int guildContribution, final byte allianceRank, final int gid, final boolean on) {
        this.level = lv;
        this.id = id;
        this.name = name;
        if (on) {
            this.channel = channel;
        }
        this.jobid = job;
        this.online = on;
        this.guildrank = rank;
        this.allianceRank = allianceRank;
	this.guildContribution = guildContribution;
        this.guildid = gid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(short l) {
        level = l;
    }

    public int getId() {
        return id;
    }

    public void setChannel(byte ch) {
        channel = ch;
    }

    public int getChannel() {
        return channel;
    }

    public int getJobId() {
        return jobid;
    }

    public void setJobId(int job) {
        jobid = job;
    }

    public int getGuildId() {
        return guildid;
    }

    public void setGuildId(int gid) {
        guildid = gid;
    }

    public void setGuildRank(byte rank) {
        guildrank = rank;
    }

    public byte getGuildRank() {
        return guildrank;
    }

    public void setGuildContribution(int c) {
	this.guildContribution = c;
    }

    public int getGuildContribution() {
	return guildContribution;
    }

    public boolean isOnline() {
        return online;
    }

    public String getName() {
        return name;
    }

    public void setOnline(boolean f) {
        online = f;
    }

    public void setAllianceRank(byte rank) {
        allianceRank = rank;
    }

    public byte getAllianceRank() {
        return allianceRank;
    }
}
