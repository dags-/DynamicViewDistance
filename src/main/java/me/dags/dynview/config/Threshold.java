package me.dags.dynview.config;

import me.dags.dynview.DynamicViewDistance;
import me.dags.dynview.DynPlayer;
import org.spongepowered.api.service.permission.Subject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Threshold extends LinkedHashMap<String, Integer> {

    private final Threshold $self = this;

    private Threshold(int size) {
        super(size);
    }

    public Threshold() {
        this(5);
    }

    public int getViewDistance(Subject subject) {
        int distance = getOrDefault("default", DynPlayer.DEFAULT_DISTANCE);
        for (Map.Entry<String, Integer> entry : entrySet()) {
            if (subject.hasPermission(DynamicViewDistance.DYN_GROUP + entry.getKey())) {
                distance = Math.max(distance, entry.getValue());
            }
        }
        return distance;
    }

    static final Threshold DEFAULT = new Threshold(0) {
        @Override
        public int getViewDistance(Subject subject) {
            return DynPlayer.DEFAULT_DISTANCE;
        }
    };
}
