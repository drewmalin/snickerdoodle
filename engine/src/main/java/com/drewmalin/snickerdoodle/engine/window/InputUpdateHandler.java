package com.drewmalin.snickerdoodle.engine.window;

import org.joml.Vector2f;

@FunctionalInterface
public interface InputUpdateHandler {
    void invoke(Window window, Vector2f mouseCursorPositionDelta, double dt);
}