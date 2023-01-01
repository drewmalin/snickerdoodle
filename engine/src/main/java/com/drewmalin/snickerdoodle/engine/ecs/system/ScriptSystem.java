package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.scene.Scene;

public interface ScriptSystem extends System{

    void update(Scene scene);
}
