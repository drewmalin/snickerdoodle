package com.drewmalin.snickerdoodle.engine.window;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import org.joml.Vector2d;

public interface Window {

    /**
     * Get the current width in pixels.
     */
    int getWidth();

    /**
     * Get the current height in pixels.
     */
    int getHeight();

    /**
     * Gets the camera for this window.
     */
    Camera getCamera();

    /**
     * Returns true if the given key has been pressed in the context of this window.
     */
    boolean isKeyPressed(int keyCode);

    /**
     * Returns true if the given mouse button has been pressed in the context of this window.
     */
    boolean isMouseButtonPressed(int buttonCode);

    /**
     * Returns the {@link Vector2d} position of the mouse.
     */
    Vector2d getMousePosition();

    /**
     * Update the window.
     */
    void update(Engine engine, Scene scene, double dt);

    /**
     * Returns true if the window is closed, false otherwise. A closed window will not react
     * to calls to Window::update.
     */
    boolean isClosed();

    /**
     * Destroy the window.
     */
    void destroy();
}
