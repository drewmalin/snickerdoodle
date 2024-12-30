package com.drewmalin.snickerdoodle.engine.light;

import org.joml.Vector3f;

public class PointLight implements PositionalLight {

    private final Vector3f color;
    private final Vector3f position;
    private final float intensity;
    private final Attenuation attenuation;

    private PointLight(final Vector3f color, final Vector3f position, final float intensity, final Attenuation attenuation) {
        this.color = color;
        this.position = position;
        this.intensity = intensity;
        this.attenuation = attenuation;
    }

    private PointLight(final Builder builder) {
        this(builder.color, builder.position, builder.intensity, builder.attenuation);
    }

    @Override
    public Vector3f color() {
        return this.color;
    }

    @Override
    public Vector3f getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(final Vector3f position) {
        this.position.set(position);
    }

    @Override
    public float getIntensity() {
        return this.intensity;
    }

    @Override
    public Attenuation getAttenuation() {
        return this.attenuation;
    }

    @Override
    public PositionalLight copy() {
        return new PointLight(new Vector3f(this.color), new Vector3f(this.position), this.intensity, this.attenuation);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vector3f color;
        private Vector3f position;
        private float intensity;
        private Attenuation attenuation;

        private Builder() {

        }

        public Builder color(final Vector3f color) {
            this.color = color;
            return this;
        }

        public Builder position(final Vector3f position) {
            this.position = position;
            return this;
        }

        public Builder intensity(final float intensity) {
            this.intensity = intensity;
            return this;
        }

        public Builder attenuation(final Attenuation attenuation) {
            this.attenuation = attenuation;
            return this;
        }

        public PointLight build() {
            return new PointLight(this);
        }
    }
}
