package com.drewmalin.snickerdoodle.engine;

import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.timer.Timer;
import com.drewmalin.snickerdoodle.engine.window.Window;

public interface Engine {

    /**
     * Returns this {@link Engine}'s {@link Camera}.
     *
     * @return this {@link Engine}'s {@link Camera}
     */
    Camera getCamera();

    /**
     * Returns this {@link Engine}'s {@link Window}.
     *
     * @return this {@link Engine}'s {@link Window}
     */
    Window getWindow();

    /**
     * Returns this {@link Engine}'s {@link Timer}.
     *
     * @return this {@link Engine}'s {@link Timer}
     */
    Timer getTimer();

    /**
     * Run the main loop.
     */
    void run();

    /**
     * Close this {@link Engine}.
     */
    void close();

    void setState(State state);

    /**
     * Engine state.
     */
    enum State {
        RUNNING,
        PAUSED,
    }
}
