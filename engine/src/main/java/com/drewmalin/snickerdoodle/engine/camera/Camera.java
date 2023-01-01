package com.drewmalin.snickerdoodle.engine.camera;

import org.joml.Vector3f;

public interface Camera {

    /**
     * Pans the camera forward along its local x-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panForward(float delta);

    /**
     * Pans the camera backward along its local z-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panBackward(float delta);

    /**
     * Pans the camera to the right along its local x-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panRight(float delta);

    /**
     * Pans the camera to the left along its local x-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panLeft(float delta);

    /**
     * Pans the camera upward along its local y-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panUp(float delta);

    /**
     * Pans the camera downward along its local y-axis.
     *
     * @param delta the amount to pan the camera by
     */
    void panDown(float delta);

    /**
     * Adjusts the pitch of the camera, rotating about its local x-axis.
     *
     * @param delta the amount to adjust the pitch by
     */
    void adjustPitch(float delta);

    /**
     * Adjusts the yaw of the camera, rotating about its local y-axis.
     *
     * @param delta the amount to adjust the yaw by
     */
    void adjustYaw(float delta);

    /**
     * Translates the camera.
     *
     * @param deltaX the amount to translate the x-axis by
     * @param deltaY the amount to translate the y-axis by
     * @param deltaZ the amount to translate the z-axis by
     */
    void translate(float deltaX, float deltaY, float deltaZ);

    /**
     * Returns the position of the camera.
     *
     * @return the {@link Vector3f} position of this camera
     */
    Vector3f getPosition();

    /**
     * Returns the target of the camera.
     *
     * @return the {@link Vector3f} target of this camera
     */
    Vector3f getTarget();
}
