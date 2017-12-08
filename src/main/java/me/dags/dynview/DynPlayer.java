package me.dags.dynview;

/**
 * @author dags <dags@dags.me>
 */
public interface DynPlayer {

    int MIN_DISTANCE = 3;
    int MAX_DISTANCE = 64;
    int DEFAULT_DISTANCE = -1;

    int getDynViewDistance();

    void setDynViewDistance(int distance);

    default void resetDynViewDistance() {
        setDynViewDistance(DEFAULT_DISTANCE);
    }

    static int clampViewDistance(int distance) {
        return Math.min(MAX_DISTANCE, Math.max(MIN_DISTANCE, distance));
    }
}
