package com.drewmalin.snickerdoodle.engine.window;

import com.drewmalin.snickerdoodle.engine.Engine;
import org.joml.Vector2f;

@FunctionalInterface
public interface InputUpdateHandler {
    void invoke(Engine engine, Window window, Vector2f mouseCursorPositionDelta, double dt);
}