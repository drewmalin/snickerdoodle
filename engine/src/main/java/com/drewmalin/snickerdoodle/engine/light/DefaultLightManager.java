package com.drewmalin.snickerdoodle.engine.light;

import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class DefaultLightManager implements LightManager {

    private static final float DEFAULT_SPECULAR_POWER = 1f;
    private static final Vector3f DEFAULT_AMBIENT_COLOR = new Vector3f(1f, 1f, 1f);

    private Light.Ambient ambient;
    private float specularPower;
    private final Set<PositionalLight> positionalLights;

    public DefaultLightManager() {
        this.specularPower = DEFAULT_SPECULAR_POWER;
        this.ambient = new Light.Ambient(DEFAULT_AMBIENT_COLOR);
        this.positionalLights = new HashSet<>();
    }

    @Override
    public Set<PositionalLight> getPositionalLights() {
        return this.positionalLights;
    }

    @Override
    public void addPositionalLight(final PositionalLight light) {
        this.positionalLights.add(light);
    }

    @Override
    public float getSpecularPower() {
        return this.specularPower;
    }

    @Override
    public void setSpecularPower(final float specularPower) {
        this.specularPower = specularPower;
    }

    @Override
    public Vector3f getAmbientLight() {
        return this.ambient.color();
    }

    @Override
    public void setAmbient(final Light.Ambient ambient) {
        this.ambient = ambient;
    }
}
