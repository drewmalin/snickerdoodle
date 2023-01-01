package com.drewmalin.snickerdoodle.engine.camera;

import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import org.joml.Vector3f;

public class DefaultCamera implements Camera {

    private static final float ROTATION_DAMPENING = 0.005f;
    private static final float TRANSLATION_DAMPENING = 0.05f;

    private final Vector3f position;
    private final Vector3f target;

    public DefaultCamera(final Vector3f position) {
        this.position = position;
        this.target = new Vector3f(0, 0, -1);
    }

    public DefaultCamera() {
        this(new Vector3f());
    }


    @Override
    public void panForward(final float delta) {
        this.position.x += this.target.x * delta * TRANSLATION_DAMPENING;
        this.position.y += 0;
        this.position.z += this.target.z * delta * TRANSLATION_DAMPENING;
    }

    @Override
    public void panBackward(final float delta) {
        panForward(-delta);
    }

    @Override
    public void panRight(final float delta) {
        final var right = this.target
                .cross(Vectors.up(), new Vector3f())
                .normalize();
        this.position.x += right.x * delta * TRANSLATION_DAMPENING;
        this.position.y += 0;
        this.position.z += right.z * delta * TRANSLATION_DAMPENING;
    }

    @Override
    public void panLeft(final float delta) {
        final var left = Vectors.up()
                .cross(this.target, new Vector3f())
                .normalize();
        this.position.x += left.x * delta * TRANSLATION_DAMPENING;
        this.position.y += 0;
        this.position.z += left.z * delta * TRANSLATION_DAMPENING;
    }

    @Override
    public void panUp(final float delta) {
        this.position.y += delta * TRANSLATION_DAMPENING;
    }

    @Override
    public void panDown(final float delta) {
        panUp(-delta);
    }

    @Override
    public void adjustPitch(final float delta) {
        this.target.rotateX(delta * ROTATION_DAMPENING);
    }

    @Override
    public void adjustYaw(final float delta) {
        this.target.rotateY(delta * ROTATION_DAMPENING);
    }

    @Override
    public void translate(final float deltaX, final float deltaY, final float deltaZ) {
        this.position.x += this.target.x * deltaX * TRANSLATION_DAMPENING;
        this.position.y += this.target.y * deltaY * TRANSLATION_DAMPENING;
        this.position.z += this.target.z * deltaZ * TRANSLATION_DAMPENING;
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(this.position);
    }

    @Override
    public Vector3f getTarget() {
        return new Vector3f(this.target);
    }
}
