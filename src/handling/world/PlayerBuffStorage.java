package handling.world;

import client.MapleCoolDownValueHolder;
import client.MapleDiseaseValueHolder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBuffStorage implements Serializable {
    private static final Map<Integer, List<PlayerBuffValueHolder>> buffs = new ConcurrentHashMap<Integer, List<PlayerBuffValueHolder>>();
    private static final Map<Integer, List<MapleCoolDownValueHolder>> coolDowns = new ConcurrentHashMap<Integer, List<MapleCoolDownValueHolder>>();
    private static final Map<Integer, List<MapleDiseaseValueHolder>> diseases = new ConcurrentHashMap<Integer, List<MapleDiseaseValueHolder>>();

    public static final void addBuffsToStorage(final int chrid, final List<PlayerBuffValueHolder> toStore) {
        buffs.put(chrid, toStore);
    }

    public static final void addCooldownsToStorage(final int chrid, final List<MapleCoolDownValueHolder> toStore) {
        coolDowns.put(chrid, toStore);
    }

    public static final void addDiseaseToStorage(final int chrid, final List<MapleDiseaseValueHolder> toStore) {
        diseases.put(chrid, toStore);
    }

    public static final List<PlayerBuffValueHolder> getBuffsFromStorage(final int chrid) {
        return buffs.remove(chrid);
    }

    public static final List<MapleCoolDownValueHolder> getCooldownsFromStorage(final int chrid) {
        return coolDowns.remove(chrid);
    }

    public static final List<MapleDiseaseValueHolder> getDiseaseFromStorage(final int chrid) {
        return diseases.remove(chrid);
    }
}
