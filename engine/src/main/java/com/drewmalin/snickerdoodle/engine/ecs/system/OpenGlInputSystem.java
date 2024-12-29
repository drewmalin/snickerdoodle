package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.window.InputUpdateHandler;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;

public class OpenGlInputSystem
    implements InputSystem {

    private final Vector2d mousePosition;
    private final InputUpdateHandler handler;

    public OpenGlInputSystem(final InputUpdateHandler handler) {
        this.mousePosition = new Vector2d();
        this.handler = handler;
    }

    @Override
    public void update(final Window window, final double dt) {
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

        this.handler.invoke(window, mouseCursorPositionDelta, dt);
    }

    @Override
    public void destroy() {

    }

}
