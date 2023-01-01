package com.drewmalin.snickerdoodle.engine.timer;

import org.lwjgl.glfw.GLFW;

public class OpenGlTimer implements Timer {

    private double lastCheckedTime;
    private double updatesPerSecond;

    @Override
    public void update() {
        final var currentTime = now();
        this.updatesPerSecond = 1 / (currentTime - this.lastCheckedTime);
        this.lastCheckedTime = currentTime;
    }

    @Override
    public double getDeltaTime() {
        return now() - lastCheckedTime;
    }

    @Override
    public double getUpdatesPerSecond() {
        return this.updatesPerSecond;
    }

    private double now() {
        return GLFW.glfwGetTime();
    }
}
