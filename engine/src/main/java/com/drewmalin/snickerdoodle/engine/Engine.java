package com.drewmalin.snickerdoodle.engine;

import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.window.Window;

public interface Engine {

    /**
     * Sets the active {@link Window}.
     */
    void setWindow(Window window);

    /**
     * Returns this {@link Engine}'s {@link Window}.
     */
    Window getWindow();

    /**
     * Run the main loop.
     */
    void run();

    /**
     * Returns the number of frames rendered per second by the engine. This can be used as an "FPS" measure suitable
     * for display to users.
     */
    double getFramesPerSecond();

    /**
     * Returns the number of update ticks per second by the engine. Depending on how the engine has been configured,
     * this value may differ from the above FPS count.
     */
    double getUpdatesPerSecond();

    /**
     * Sets the {@link State} of this engine.
     */
    void setState(State state);

    /**
     * Returns the {@link State} of this engine.
     */
    State getState();

    /**
     * Sets the active {@link Scene} of this engine.
     */
    void setScene(Scene scene);

    /**
     * Returns the active {@link Scene} of this engine.
     */
    Scene getScene();

    /**
     * Close this {@link Engine}.
     */
    void close();

    /**
     * Engine state.
     */
    enum State {
        RUNNING,
        PAUSED,
    }
}
