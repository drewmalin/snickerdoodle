package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.camera.Camera;
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

    void update(Scene scene, Window window, Camera camera);
}
