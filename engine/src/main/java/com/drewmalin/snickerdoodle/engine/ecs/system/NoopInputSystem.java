package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.window.Window;

public class NoopInputSystem
    implements InputSystem {

    public static final InputSystem INSTANCE = new NoopInputSystem();

    @Override
    public void update(final Engine engine, final Window window, final double dt) {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }
}
