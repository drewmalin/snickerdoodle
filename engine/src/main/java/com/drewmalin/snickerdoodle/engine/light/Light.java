package com.drewmalin.snickerdoodle.engine.light;

import org.joml.Vector3f;

public interface Light {

    Vector3f color();

    record Ambient(Vector3f color) implements Light {
    }

    record Directional(Vector3f color) implements Light {
    }
}
