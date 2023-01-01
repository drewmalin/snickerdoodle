package com.drewmalin.snickerdoodle.engine.light;

import org.joml.Vector3f;

public interface PositionalLight extends Light {

    Vector3f getPosition();

    float getIntensity();

    Attenuation getAttenuation();

    PositionalLight copy();
}
