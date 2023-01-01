package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.window.Window;
import org.joml.Vector2f;

public interface InputSystem extends System {

    void update(Window window, Callback callback);

    @FunctionalInterface
    interface Callback {

        void invoke(Window window, Vector2f mouseCursorPositionDelta);
    }
}
