package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.window.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;

public class DefaultInputSystem implements InputSystem {

    private final Vector2d mousePosition;

    public DefaultInputSystem() {
        this.mousePosition = new Vector2d();
    }


    @Override
    public void update(final Window window, final Callback callback) {
        final var mouseCursorPositionDelta = new Vector2f();

        final var currentPosition = window.getMousePosition();
        if (this.mousePosition.x > 0 && this.mousePosition.y > 0) {
            final var deltaX = currentPosition.x - this.mousePosition.x;
            final var deltaY = currentPosition.y - this.mousePosition.y;
            if (deltaX != 0) {
                mouseCursorPositionDelta.y = (float) deltaX;
            }
            if (deltaY != 0) {
                mouseCursorPositionDelta.x = (float) deltaY;
            }
        }
        this.mousePosition.x = currentPosition.x;
        this.mousePosition.y = currentPosition.y;

        callback.invoke(window, mouseCursorPositionDelta);
    }


    @Override
    public void destroy() {

    }

}
