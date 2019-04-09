package me.dags.dynview.mixin;

import me.dags.dynview.DynPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author dags <dags@dags.me>
 */
@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP implements DynPlayer {

    private int dynViewDistance = -1;

    @Shadow
    public abstract WorldServer getServerWorld();

    @Override
    public int getDynViewDistance() {
        return this.dynViewDistance < 0 ? getWorldViewDistance() : this.dynViewDistance;
    }

    @Override
    public void setDynViewDistance(int distance) {
        if (distance < 0) {
            this.dynViewDistance = -1;
        } else {
            this.dynViewDistance = Math.min(DynPlayer.MAX_DISTANCE, Math.max(DynPlayer.MIN_DISTANCE, distance));
        }
        getServerWorld().getPlayerChunkMap().setPlayerViewRadius(getDynViewDistance());
    }
}
