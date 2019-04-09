package me.dags.dynview;

import com.google.inject.Inject;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
import me.dags.config.Mapper;
import me.dags.dynview.config.Config;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "dynview", name = "DynamicViewDistance", version = "0.2.0", description = "Dynamic per-player view distances", authors = "dags")
public class DynamicViewDistance {

    public static final String DYN_CUSTOM = "dynview.custom";
    public static final String DYN_BYPASS = "dynview.bypass";
    public static final String DYN_ADMIN = "dynview.admin";
    public static final String DYN_GROUP = "dynview.group.";

    private static final Mapper<Config> mapper = Mapper.of(Config.class);

    private final Path dir;
    private boolean paused = false;
    private Config config;

    @Inject
    public DynamicViewDistance(@ConfigDir(sharedRoot = false) Path dir) {
        this.dir = dir;
    }

    @Listener
    public void onInit(GameInitializationEvent e) {
        CommandBus.create(this).register(this).submit();
        Format format = Format.builder().info(TextColors.GREEN).stress(TextColors.DARK_AQUA).build();
        Fmt.get("dynview", format);
        onReload(null);
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        config = mapper.must(dir.resolve("config.conf"), Config::defaultConfig);
        refresh();
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join e) {
        refresh();
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect e) {
        refresh();
    }

    @Listener(order = Order.POST)
    public void onSpawn(SpawnEntityEvent e) {
        for (Entity entity : e.getEntities()) {
            if (entity instanceof Player) {
                refreshPlayer((Player) entity);
            }
        }
    }

    @Listener(order = Order.POST)
    public void onTeleport(MoveEntityEvent.Teleport e) {
        Entity entity = e.getTargetEntity();
        if (entity instanceof Player && e.getToTransform().getExtent() != e.getFromTransform().getExtent()) {
            refreshPlayer((Player) entity);
        }
    }

    @Command("dynview set <distance>")
    @Permission(DynamicViewDistance.DYN_CUSTOM)
    @Description("Sets your server-side view distance to a custom value")
    public void setDistance(@Src Player player, int distance) {
        DynPlayer dynPlayer = (DynPlayer) player;
        dynPlayer.setDynViewDistance(distance);
        int value = dynPlayer.getDynViewDistance();
        Fmt.get("dynview").info("Set your server-side view distance to: ").stress(value).tell(player);
    }

    @Command("dynview reset")
    @Permission(DynamicViewDistance.DYN_CUSTOM)
    @Description("Resets your server-side view distance to the default")
    public void resetDistance(@Src Player player) {
        DynPlayer dynPlayer = (DynPlayer) player;
        dynPlayer.resetDynViewDistance();
        Fmt.get("dynview").info("Reset your server-side view distance").tell(player);
    }

    @Command("dynview test <target>")
    @Permission(DynamicViewDistance.DYN_ADMIN)
    @Description("Check what server-side view distance the target player has set")
    public void testDistance(@Src CommandSource src, Player target) {
        DynPlayer dynPlayer = (DynPlayer) target;
        int distance = dynPlayer.getDynViewDistance();

        Fmt.get("dynview")
                .info("Player: ").stress(target.getName())
                .info(", World: ").stress(target.getWorld().getName())
                .info(", Distance: ").stress(distance)
                .tell(src);
    }

    @Command("dynview reload")
    @Permission(DynamicViewDistance.DYN_ADMIN)
    @Description("Reloads the config and refreshes all users")
    public void reload(@Src CommandSource src) {
        Fmt.get("dynview").info("Reloading...").tell(src);
        onReload(null);
    }

    @Command("dynview pause")
    @Permission(DynamicViewDistance.DYN_ADMIN)
    @Description("Toggles on/off dynamic view distances for all players")
    public void pause(@Src CommandSource src) {
        if (paused = !paused) {
            Fmt.get("dynview").info("Pausing dynamic view distances...").tell(src);
            reset();
        } else {
            Fmt.get("dynview").info("Un-pausing dynamic view distances...").tell(src);
            refresh();
        }
    }

    private void refreshPlayer(Player player) {
        if (paused) {
            return;
        }
        Collection<Player> singleton = Collections.singletonList(player);
        UpdateTask.Refresh refresh = new UpdateTask.Refresh(singleton, config);
        Task.builder().execute(refresh).delayTicks(1L).submit(this);
    }

    private void refresh() {
        if (paused) {
            return;
        }
        Collection<Player> online = Sponge.getServer().getOnlinePlayers();
        UpdateTask.Refresh refresh = new UpdateTask.Refresh(online, config);
        Task.builder().execute(refresh).intervalTicks(1).submit(this);
    }

    private void reset() {
        Collection<Player> online = Sponge.getServer().getOnlinePlayers();
        UpdateTask.Reset reset = new UpdateTask.Reset(online, config);
        Task.builder().execute(reset).intervalTicks(1).submit(this);
    }
}
