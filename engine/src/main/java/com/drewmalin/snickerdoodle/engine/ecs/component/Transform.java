package com.drewmalin.snickerdoodle.engine.ecs.component;

import org.joml.Vector3f;

import java.util.Objects;

public class Transform implements Component {

    private final Vector3f position;
    private final Vector3f rotation;
    private final Vector3f scale;

    public Transform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Transform(final Builder builder) {
        this.position = builder.position == null ? new Vector3f(0, 0, 0) : builder.position;
        this.rotation = builder.rotation == null ? new Vector3f(0, 0, 0) : builder.rotation;
        this.scale = builder.scale == null ? new Vector3f(1, 1, 1) : builder.scale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public Vector3f getScale() {
        return this.scale;
    }

    public void setPosition(final float x, final float y, final float z) {
        this.position.setComponent(0, x);
        this.position.setComponent(1, y);
        this.position.setComponent(2, z);
    }

    public void setRotation(final float x, final float y, final float z) {
        this.rotation.setComponent(0, x);
        this.rotation.setComponent(1, y);
        this.rotation.setComponent(2, z);
    }

    public void setScale(final float scale) {
        setScale(scale, scale, scale);
    }

    public void setScale(final float x, final float y, final float z) {
        this.scale.setComponent(0, x);
        this.scale.setComponent(1, y);
        this.scale.setComponent(2, z);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Transform that = (Transform) o;
        return this.position.equals(that.position)
                && this.rotation.equals(that.rotation)
                && this.scale.equals(that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.rotation, this.scale);
    }

    public static class Builder {
        private Vector3f position;
        private Vector3f rotation;
        private Vector3f scale;

        public Builder position(final Vector3f position) {
            this.position = position;
            return this;
        }

        public Builder rotation(final Vector3f rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder scale(final Vector3f scale) {
            this.scale = scale;
            return this;
        }

        public Transform build() {
            return new Transform(this);
        }
    }
}
