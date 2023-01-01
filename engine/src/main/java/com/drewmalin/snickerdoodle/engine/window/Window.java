package com.drewmalin.snickerdoodle.engine.window;

import org.joml.Vector2d;

public interface Window {

    /**
     * Returns true if the window is closed, false otherwise. A closed window will not react
     * to calls to Window::update.
     */
    boolean isClosed();

    /**
     * Returns true if the given key has been pressed in the context of this window.
     */
    boolean isKeyPressed(int keyCode);

    /**
     * Returns true if the given mouse button has been pressed in the context of this window.
     */
    boolean isMouseButtonPressed(int buttonCode);

    Vector2d getMousePosition();

    /**
     * Update the window.
     */
    void update(Runnable onUpdate);

    /**
     * Destroy the window.
     */
    void destroy();

    /**
     * Returns true if the window should synchronize updates to a specific frame rate.
     */
    boolean isVerticalSyncEnabled();

    /**
     * Get the current width in pixels.
     */
    int getWidth();

    /**
     * Get the current height in pixels.
     */
    int getHeight();
}
