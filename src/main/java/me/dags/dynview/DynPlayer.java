package me.dags.dynview;

/**
 * @author dags <dags@dags.me>
 */
public interface DynPlayer {

    int MIN_DISTANCE = 3;
    int MAX_DISTANCE = 64;

    int getDynViewDistance();

    void setDynViewDistance(int distance);

    default void resetDynViewDistance() {
        setDynViewDistance(-1);
    }
}
