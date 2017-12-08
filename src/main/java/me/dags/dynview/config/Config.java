package me.dags.dynview.config;

import java.util.LinkedHashMap;

/**
 * @author dags <dags@dags.me>
 */
public class Config extends LinkedHashMap<String, WorldConfig> {

    private final Config $self = this;

    public WorldConfig getWorldConfig(String world) {
        return getOrDefault(world, getGlobalConfig());
    }

    public WorldConfig getGlobalConfig() {
        return computeIfAbsent("global", k -> WorldConfig.defaultGlobal());
    }

    public static Config defaultConfig() {
        Config config = new Config();
        config.getGlobalConfig();
        return config;
    }
}
