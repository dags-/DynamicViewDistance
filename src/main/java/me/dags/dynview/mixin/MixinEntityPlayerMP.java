package me.dags.dynview.mixin;

import me.dags.dynview.DynChunkMap;
import me.dags.dynview.DynPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author dags <dags@dags.me>
 */
@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP implements DynPlayer {

    private int dynViewDistance = DynPlayer.DEFAULT_DISTANCE;

    @Shadow
    public abstract WorldServer getServerWorld();

    @Override
    public int getDynViewDistance() {
        return this.dynViewDistance;
    }

    @Override
    public void setDynViewDistance(int distance) {
        DynChunkMap chunkMap = (DynChunkMap) getServerWorld().getPlayerChunkMap();
        int currentDistance = getDynViewDistance() == DEFAULT_DISTANCE ? chunkMap.getWorldViewDistance() : getDynViewDistance();
        int newDistance = distance >= 0 ? DynPlayer.clampViewDistance(distance) : chunkMap.getWorldViewDistance();
        this.dynViewDistance = newDistance;
        chunkMap.setViewDistance(this, currentDistance, newDistance);
    }
}
