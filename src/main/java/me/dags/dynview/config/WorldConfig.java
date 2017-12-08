package me.dags.dynview.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class WorldConfig extends LinkedHashMap<Integer, Threshold> {

    private final WorldConfig $self = this;

    public Threshold getThreshold(int onlineCount) {
        int highest = -1;
        Threshold result = Threshold.DEFAULT;
        for (Map.Entry<Integer, Threshold> entry : entrySet()) {
            int threshold = entry.getKey();
            if (onlineCount >= threshold && threshold > highest) {
                highest = threshold;
                result = entry.getValue();
            }
        }
        return result;
    }

    static WorldConfig defaultGlobal() {
        Threshold threshold = new Threshold();
        threshold.put("default", 10);
        threshold.put("vip", 32);

        WorldConfig config = new WorldConfig();
        config.put(20, threshold);
        return config;
    }
}
