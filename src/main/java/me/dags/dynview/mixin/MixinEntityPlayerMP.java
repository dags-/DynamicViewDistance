package me.dags.dynview.mixin;

import me.dags.dynview.DynPlayer;
import me.dags.dynview.IMixinChunkMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
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

    private IMixinChunkMap getChunkMap() {
        return (IMixinChunkMap) getServerWorld().getPlayerChunkMap();
    }

    @Override
    public int getDynViewDistance() {
        return this.dynViewDistance < 0 ? getChunkMap().getWorldViewDistance() : this.dynViewDistance;
    }

    @Override
    public void setDynViewDistance(int distance) {
        int currentDistance = getDynViewDistance();
        this.dynViewDistance = getSafeViewDistance(distance);
        int newDistance = getDynViewDistance();
        getChunkMap().updatePlayerViewDistance((EntityPlayerMP) (Object) this, currentDistance, newDistance);
    }

    private int getSafeViewDistance(int distance) {
        return distance < 0 ? -1 : Math.min(DynPlayer.MAX_DISTANCE, Math.max(DynPlayer.MIN_DISTANCE, distance));
    }
}
