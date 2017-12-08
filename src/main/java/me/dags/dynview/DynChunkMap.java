package me.dags.dynview;

/**
 * @author dags <dags@dags.me>
 */
public interface DynChunkMap {

    int getWorldViewDistance();

    void setViewDistance(DynPlayer player, int currentDistance, int newDistance);
}
