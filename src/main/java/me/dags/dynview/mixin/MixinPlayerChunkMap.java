package me.dags.dynview.mixin;

import me.dags.dynview.DynPlayer;
import me.dags.dynview.IMixinChunkMap;
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
public abstract class MixinPlayerChunkMap implements IMixinChunkMap {

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
        int viewDistance = ((DynPlayer) player).getDynViewDistance();
        if (viewDistance == getWorldViewDistance()) {
            return;
        }

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

    @Override
    public void updatePlayerViewDistance(EntityPlayerMP player, int oldDistance, int newDistance) {
        if (oldDistance == newDistance) {
            return;
        }

        int chunkX = (int) player.posX >> 4;
        int chunkZ = (int) player.posZ >> 4;
        int min = Math.min(oldDistance, newDistance);
        int max = Math.max(oldDistance, newDistance);
        EntryVisitor visitor = newDistance > oldDistance ? expand : contract;

        for (int dx = 0; dx <= max; dx++) {
            for (int dz = 0; dz <= max; dz++) {
                if (dx > min || dz > min) {
                    visitor.visit(chunkX - dx, chunkZ - dz, player);
                    if (dz != 0) {
                        visitor.visit(chunkX - dx, chunkZ + dz, player);
                    }
                    if (dx != 0) {
                        visitor.visit(chunkX + dx, chunkZ - dz, player);
                    }
                    if (dx != 0 && dz != 0) {
                        visitor.visit(chunkX + dx, chunkZ + dz, player);
                    }
                }
            }
        }

        markSortPending();
    }

    @Override
    public int getWorldViewDistance() {
        return playerViewRadius;
    }

    private final EntryVisitor expand = (x, z, p) -> getOrCreateEntry(x, z).addPlayer(p);

    private final EntryVisitor contract = (x, z, p) -> {
        PlayerChunkMapEntry entry = getEntry(x, z);
        if (entry != null) {
            entry.removePlayer(p);
        }
    };
}
