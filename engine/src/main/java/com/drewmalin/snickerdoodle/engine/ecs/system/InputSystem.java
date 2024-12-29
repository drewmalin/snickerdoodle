package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.window.Window;

public interface InputSystem extends System {

    void update(Window window, double dt);

}
