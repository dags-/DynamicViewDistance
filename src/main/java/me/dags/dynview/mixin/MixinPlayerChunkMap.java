package me.dags.dynview.mixin;

import me.dags.dynview.DynChunkMap;
import me.dags.dynview.DynPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author dags <dags@dags.me>
 */
@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap implements DynChunkMap {

    @Shadow
    private int playerViewRadius;

    @Shadow
    protected abstract boolean overlaps(int i, int j, int l, int m, int n);

    @Shadow
    protected abstract PlayerChunkMapEntry getOrCreateEntry(int x, int z);

    @Shadow
    public abstract PlayerChunkMapEntry getEntry(int x, int z);

    @Shadow
    protected abstract void markSortPending();

    @Inject(method = "updateMountedMovingPlayer", at = @At("HEAD"), cancellable = true)
    private void updateMountedMovingPlayer(EntityPlayerMP player, CallbackInfo ci) {
        DynPlayer dynPlayer = (DynPlayer) player;
        int viewDistance = dynPlayer.getDynViewDistance();

        // user the custom view distance if set & is different to world view distance
        if (viewDistance != DynPlayer.DEFAULT_DISTANCE && viewDistance != getWorldViewDistance()) {
            ci.cancel();

            int px = (int) player.posX >> 4;
            int pz = (int) player.posZ >> 4;
            double d0 = player.managedPosX - player.posX;
            double d1 = player.managedPosZ - player.posZ;
            double d2 = d0 * d0 + d1 * d1;

            if (d2 >= 64.0D) {
                int mx = (int) player.managedPosX >> 4;
                int mz = (int) player.managedPosZ >> 4;
                int dx = px - mx;
                int dz = pz - mz;

                if (dx != 0 || dz != 0) {
                    for (int x = px - viewDistance; x <= px + viewDistance; ++x) {
                        for (int z = pz - viewDistance; z <= pz + viewDistance; ++z) {
                            if (!this.overlaps(x, z, mx, mz, viewDistance)) {
                                this.getOrCreateEntry(x, z).addPlayer(player);
                            }

                            if (!this.overlaps(x - dx, z - dz, px, pz, viewDistance)) {
                                PlayerChunkMapEntry playerchunkmapentry = this.getEntry(x - dx, z - dz);

                                if (playerchunkmapentry != null) {
                                    playerchunkmapentry.removePlayer(player);
                                }
                            }
                        }
                    }

                    player.managedPosX = player.posX;
                    player.managedPosZ = player.posZ;
                    this.markSortPending();
                }
            }
        }
    }

    @Override
    public void setViewDistance(DynPlayer dynPlayer, int currentDistance, int newDistance) {
        if (currentDistance == newDistance) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) dynPlayer;
        boolean expand = newDistance > currentDistance;
        int px = (int) player.posX >> 4;
        int pz = (int) player.posZ >> 4;
        int min = Math.min(currentDistance, newDistance);
        int max = Math.max(currentDistance, newDistance);

        /*
         * |xz|xz|xz|
         * |  |  |  |
         * |xz|xz|xz| // iterates this area and mirrors ^
         */
        for (int dx = -max; dx <= max; dx++) {
            for (int dz = -max; dz <= -min; dz++) {
                if (expand) {
                    getOrCreateEntry(px + dx, pz + dz).addPlayer(player);
                    if (dx != 0 || dz != 0) { // avoid duplicates at origin
                        getOrCreateEntry(px - dx, pz - dz).addPlayer(player);
                    }
                } else {
                    removePlayerFromEntry(px + dx, pz + dz, player);
                    if (dx != 0 || dz != 0) {
                        removePlayerFromEntry(px - dx, pz - dz, player);
                    }
                }
            }
        }

        /*
         * |..|..|..|
         * |xz|  |xz| // iterates left-hand area and mirrors >
         * |..|..|..|
         */
        for (int dx = -max; dx <= -min; dx++) {
            for (int dz = -min; dz <= min; dz++) {
                if (expand) {
                    getOrCreateEntry(px + dx, pz + dz).addPlayer(player);
                    if (dx != 0 || dz != 0) {
                        getOrCreateEntry(px - dx, pz - dz).addPlayer(player);
                    }
                } else {
                    removePlayerFromEntry(px + dx, pz + dz, player);
                    if (dx != 0 || dz != 0) {
                        removePlayerFromEntry(px - dx, pz - dz, player);
                    }
                }
            }
        }

        this.markSortPending();
    }

    @Override
    public int getWorldViewDistance() {
        return playerViewRadius;
    }

    private void removePlayerFromEntry(int x, int z, EntityPlayerMP player) {
        PlayerChunkMapEntry entry = getEntry(x, z);
        if (entry != null) {
            entry.removePlayer(player);
        }
    }
}
