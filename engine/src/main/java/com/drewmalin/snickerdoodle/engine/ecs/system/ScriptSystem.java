package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.scene.Scene;

public interface ScriptSystem extends System{

    void update(Engine engine, Scene scene, double dt);
}
