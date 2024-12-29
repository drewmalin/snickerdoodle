package com.drewmalin.snickerdoodle.engine.timer;

public interface Timer {

    /**
     * Update the timer.
     */
    void update();

    /**
     * Returns the amount of time in seconds since the previous call to Timer::update.
     */
    double getDeltaTimeInSeconds();

    /**
     * Returns the total number of updates which have occurred per second.
     */
    double getUpdatesPerSecond();

    void newLap(String lapName);

    double getDeltaTimeForLap(String lapName);

    void updateLap(String lapName);

    void accumulateFrameTime(double deltaTimeInSeconds);

    double getAccumulatedFrameTime();

    void decrementAccumulatedFrameTime(double deltaTimeInSeconds);
}
