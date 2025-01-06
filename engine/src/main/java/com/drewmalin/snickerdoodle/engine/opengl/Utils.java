package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Utils {

    private Utils() {
    }


    public static Matrix4f getCameraTransformation(final Camera camera) {
        final var cameraPosition = camera.getPosition();
        final var cameraTarget = camera.getTarget();

        return new Matrix4f()
            .identity()
            .lookAt(cameraPosition, cameraPosition.add(cameraTarget, new Vector3f()), Vectors.up())
            .translate(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());
    }

    public static Matrix4f getEntityTransformation(final Transform transform, final Matrix4f cameraTransformation) {
        final var positionMatrix = new Matrix4f()
            .identity()
            .translate(transform.getPosition())
            .rotateX((float) Math.toRadians(transform.getRotation().x()))
            .rotateY((float) Math.toRadians(transform.getRotation().y()))
            .rotateZ((float) Math.toRadians(transform.getRotation().z()))
            .scale(transform.getScale());
        final var cameraCurrent = new Matrix4f(cameraTransformation);
        return cameraCurrent.mul(positionMatrix);
    }
}
