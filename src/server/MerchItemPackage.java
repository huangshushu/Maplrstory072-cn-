package server;

import client.inventory.Item;
import java.util.ArrayList;
import java.util.List;

public class MerchItemPackage {
    private long sentTime;
    private int mesos = 0, packageid;
    private List<Item> items = new ArrayList<Item>();

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public long getSentTime() {
        return sentTime;
    }

    public int getMesos() {
        return mesos;
    }

    public void setMesos(int set) {
        mesos = set;
    }

    public int getPackageid() {
        return packageid;
    }

    public void setPackageid(int packageid) {
        this.packageid = packageid;
    }
}
