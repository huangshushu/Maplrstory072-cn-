package handling.world.sidekick;

import client.MapleCharacter;
import java.io.Serializable;

public class MapleSidekickCharacter implements Serializable {
    private static final long serialVersionUID = 6215463252132450750L;
    private String name;
    private int id;
    private int level;
    private int jobid;
    private int mapid;

    public MapleSidekickCharacter(MapleCharacter maplechar) {
        update(maplechar);
    }

    public MapleSidekickCharacter(int id, String name, int level, int jobid, int mapid) {
        this.name = name;
	this.id = id;
	this.level = level;
	this.jobid = jobid;
	this.mapid = mapid;
    }
	
    public void update(MapleCharacter maplechar) {
        this.name = maplechar.getName();
        this.level = maplechar.getLevel();
        this.id = maplechar.getId();
        this.jobid = maplechar.getJob();
        this.mapid = maplechar.getMapId();
    }

    public int getLevel() {
        return level;
    }

    public int getMapid() {
        return mapid;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleSidekickCharacter other = (MapleSidekickCharacter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
