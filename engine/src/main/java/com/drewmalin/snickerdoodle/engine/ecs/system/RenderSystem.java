package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.window.Window;

/**
 * Renders entities which have at least:
 * - Mesh
 * - Position (offsets the given Mesh points)
 * Specialized rendering will occur in priority order:
 * - Shader
 * - Material
 * - Color
 */
public interface RenderSystem extends System {

    void update(Engine engine, Window window, Scene scene);
}
