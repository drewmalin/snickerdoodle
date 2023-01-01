package com.drewmalin.snickerdoodle.engine.timer;

public interface Timer {

    /**
     * Update the timer.
     */
    void update();

    /**
     * Returns the amount of time in seconds since the previous call to Timer::update.
     */
    double getDeltaTime();

    /**
     * Returns the total number of updates which have occurred per second.
     */
    double getUpdatesPerSecond();

}
