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
import me.dags.dynview.config.Threshold;
import me.dags.dynview.config.WorldConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "dynview", name = "DynamicViewDistance", version = "1.0", description = "Dynamic per-player view distances", authors = "dags")
public class DynamicViewDistance {

    public static final String DYN_CUSTOM = "dynview.custom";
    public static final String DYN_BYPASS = "dynview.bypass";
    public static final String DYN_ADMIN = "dynview.admin";
    public static final String DYN_GROUP = "dynview.group.";

    private static final Mapper<Config> mapper = Mapper.of(Config.class);

    private final Path dir;
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
        Task.builder().execute(this::refresh).submit(this);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join e) {
        Task.builder().execute(this::refresh).submit(this);
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect e) {
        Task.builder().execute(this::refresh).submit(this);
    }

    private void refresh() {
        Collection<Player> online = Sponge.getServer().getOnlinePlayers();
        int count = online.size();
        for (Player player : online) {
            if (!player.hasPermission(DYN_BYPASS)) {
                WorldConfig world = config.getWorldConfig(player.getWorld().getName());
                Threshold threshold = world.getThreshold(count);
                int distance = threshold.getViewDistance(player);
                ((DynPlayer) player).setDynViewDistance(distance);
            }
        }
    }

    @Command("dynview|dv set <distance>")
    @Permission(DynamicViewDistance.DYN_CUSTOM)
    @Description("Sets your server-side view distance to a custom value")
    public void setDistance(@Src Player player, int distance) {
        DynPlayer dynPlayer = (DynPlayer) player;
        dynPlayer.setDynViewDistance(distance);
        int value = dynPlayer.getDynViewDistance();
        Fmt.get("dynview").info("Set your server-side view distance to: ").stress(value).tell(player);
    }

    @Command("dynview|dv reset")
    @Permission(DynamicViewDistance.DYN_CUSTOM)
    @Description("Resets your server-side view distance to the default")
    public void resetDistance(@Src Player player) {
        DynPlayer dynPlayer = (DynPlayer) player;
        dynPlayer.resetDynViewDistance();
        Fmt.get("dynview").info("Reset your server-side view distance").tell(player);
    }

    @Command("dynview|dv test <target>")
    @Permission(DynamicViewDistance.DYN_ADMIN)
    @Description("Check what server-side view distance the target player has set")
    public void testDistance(@Src CommandSource src, Player target) {
        DynPlayer dynPlayer = (DynPlayer) target;
        int distance = dynPlayer.getDynViewDistance();
        Object value = distance == DynPlayer.DEFAULT_DISTANCE ? "default" : distance;
        Fmt.stress(target.getName()).info("'s view distance is set to").stress(value).tell(src);
    }

    @Command("dynview|dv reload")
    @Permission(DynamicViewDistance.DYN_ADMIN)
    @Description("Reloads the config and refreshes all users")
    public void reload(@Src CommandSource src) {
        Fmt.get("dynview").info("Reloading...").tell(src);
        onReload(null);
    }
}
