package com.drewmalin.snickerdoodle.engine.model;

import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;

public interface Model {

    Mesh getMesh();

    Material getMaterial();
}
