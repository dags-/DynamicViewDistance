package me.dags.dynview;

import me.dags.dynview.config.Config;
import me.dags.dynview.config.Threshold;
import me.dags.dynview.config.WorldConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public abstract class UpdateTask implements Consumer<Task> {

    private final Iterator<Player> iterator;
    private final Config config;
    private final int count;

    private UpdateTask(Collection<Player> players, Config config) {
        this.iterator = players.iterator();
        this.config = config;
        this.count = players.size();
    }

    @Override
    public void accept(Task task) {
        if (!iterator.hasNext()) {
            task.cancel();
            return;
        }

        // make
        Player next = iterator.next();
        if (!next.isOnline()) {
            return;
        }

        apply(next);
    }

    public abstract void apply(Player player);

    static class Refresh extends UpdateTask {

        Refresh(Collection<Player> players, Config config) {
            super(players, config);
        }

        @Override
        public void apply(Player player) {
            if (player.hasPermission(DynamicViewDistance.DYN_BYPASS)) {
                return;
            }

            DynPlayer dynPlayer = (DynPlayer) player;
            WorldConfig world = super.config.getWorldConfig(player.getWorld().getName());
            Threshold threshold = world.getThreshold(super.count);

            int distance = threshold.getViewDistance(player);
            dynPlayer.setDynViewDistance(distance);
        }
    }

    static class Reset extends UpdateTask {

        Reset(Collection<Player> players, Config config) {
            super(players, config);
        }

        @Override
        public void apply(Player player) {
            if (player.hasPermission(DynamicViewDistance.DYN_BYPASS)) {
                return;
            }
            ((DynPlayer) player).resetDynViewDistance();
        }
    }
}
