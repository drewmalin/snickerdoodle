package com.drewmalin.snickerdoodle.engine.light;

import org.joml.Vector3f;

import java.util.Set;

public interface LightManager {
    Set<PositionalLight> getPositionalLights();

    void addPositionalLight(PositionalLight light);

    float getSpecularPower();

    void setSpecularPower(float specularPower);

    Vector3f getAmbientLight();

    void setAmbient(Light.Ambient ambient);
}
