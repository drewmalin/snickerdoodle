package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.window.Window;

public class NoopRenderSystem
    implements RenderSystem {

    public static final RenderSystem INSTANCE = new NoopRenderSystem();

    @Override
    public void update(final Engine engine, final Window window, final Scene scene) {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }
}
