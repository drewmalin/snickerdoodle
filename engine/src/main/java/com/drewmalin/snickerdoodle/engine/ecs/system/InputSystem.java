package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.window.Window;

public interface InputSystem extends System {

    void update(Engine engine, Window window, double dt);

}
