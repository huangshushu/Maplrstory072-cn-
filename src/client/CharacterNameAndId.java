package client;

public class CharacterNameAndId {
    private int id;
    private String name, group;

    public CharacterNameAndId(int id, String name, String group) {
        super();
        this.id = id;
        this.name = name;
	this.group = group;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }
}
