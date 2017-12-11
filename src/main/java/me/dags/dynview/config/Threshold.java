package me.dags.dynview.config;

import me.dags.dynview.DynamicViewDistance;
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
        int distance = -1;

        for (Map.Entry<String, Integer> entry : entrySet()) {
            if (entry.getKey().equals("default")) {
                continue;
            }

            if (subject.hasPermission(DynamicViewDistance.DYN_GROUP + entry.getKey())) {
                distance = Math.max(distance, entry.getValue());
            }
        }

        if (distance == -1) {
            distance = getOrDefault("default", -1);
        }

        return distance;
    }

    static final Threshold DEFAULT = new Threshold(0) {
        @Override
        public int getViewDistance(Subject subject) {
            return -1;
        }
    };
}
