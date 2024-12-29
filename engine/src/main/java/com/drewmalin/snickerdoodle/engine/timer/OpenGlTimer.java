package com.drewmalin.snickerdoodle.engine.timer;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class OpenGlTimer implements Timer {

    private double lastCheckedTimeInSeconds;
    private double updatesPerSecond;
    private double accumulatedFrameTime;

    private final Map<String, Double> lapTimers;

    public OpenGlTimer() {
        this.lapTimers = new HashMap<>();
    }

    @Override
    public void update() {
        final var currentTimeInSeconds = currentTimeInSeconds();
        this.updatesPerSecond = 1 / (currentTimeInSeconds - this.lastCheckedTimeInSeconds);
        this.lastCheckedTimeInSeconds = currentTimeInSeconds;
    }

    @Override
    public double getDeltaTimeInSeconds() {
        return currentTimeInSeconds() - this.lastCheckedTimeInSeconds;
    }

    @Override
    public double getUpdatesPerSecond() {
        return this.updatesPerSecond;
    }

    @Override
    public void newLap(final String lapName) {
        this.lapTimers.put(lapName, currentTimeInSeconds());
    }

    @Override
    public double getDeltaTimeForLap(final String lapName) {
        return currentTimeInSeconds() - this.lapTimers.getOrDefault(lapName, -1D);
    }

    @Override
    public void updateLap(final String lapName) {
        this.lapTimers.put(lapName, currentTimeInSeconds());
    }

    @Override
    public void accumulateFrameTime(final double deltaTimeInSeconds) {
        var delta = 0D;
        if (deltaTimeInSeconds > 0.25) {
            delta = 0.25;
        }
        else {
            delta = deltaTimeInSeconds;
        }
        this.accumulatedFrameTime += delta;
    }

    @Override
    public double getAccumulatedFrameTime() {
        return this.accumulatedFrameTime;
    }

    @Override
    public void decrementAccumulatedFrameTime(final double deltaTimeInSeconds) {
        var delta = deltaTimeInSeconds;
        this.accumulatedFrameTime -= delta;
    }

    private double currentTimeInSeconds() {
        return GLFW.glfwGetTime();
    }
}
