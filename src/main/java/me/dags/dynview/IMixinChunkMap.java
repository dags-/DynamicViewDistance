package me.dags.dynview;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author dags <dags@dags.me>
 */
public interface IMixinChunkMap {

    int getWorldViewDistance();

    void updatePlayerViewDistance(EntityPlayerMP player, int oldDistance, int newDistance);

    interface EntryVisitor {

        void visit(int x, int z, EntityPlayerMP player);
    }
}
