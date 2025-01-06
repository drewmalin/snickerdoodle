package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;
import com.drewmalin.snickerdoodle.engine.ecs.component.Texture14;
import com.drewmalin.snickerdoodle.engine.ecs.system.RenderSystem;
import com.drewmalin.snickerdoodle.engine.opengl.shader.ColorShader;
import com.drewmalin.snickerdoodle.engine.opengl.shader.OpenGlShader;
import com.drewmalin.snickerdoodle.engine.opengl.shader.TextureShader;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class OpenGlRenderSystem
    implements RenderSystem {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlRenderSystem.class);

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000f;

    private final Frustum frustum;

    public OpenGlRenderSystem() {
        this.frustum = new Frustum();
    }

    @Override
    public void update(final Engine engine, final Window window, final Scene scene) {
        final var entityManager = scene.getEntityManager();
        final var lightManager = scene.getLightManager();

        final var frustumTransformation = this.frustum.toMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        final var cameraTransformation = Utils.getCameraTransformation(window.getCamera());

        for (final var entity : entityManager.getEntitiesWithComponent(Mesh.class)) {

            /*
             * Fetch the appropriate shader for the mesh. If none is found, use a default color.
             */
            final OpenGlShader shader;
            if (entityManager.getComponent(entity, Texture14.class).isPresent()) {
                shader = TextureShader.get();
            }
            else {
                shader = ColorShader.get();
            }

            /*
             * Rendering is ultimately done by the shader itself, so the below sets the new vertex positions, normals,
             * colors, lighting, and other inputs before invoking a call to "draw". This is done within an implicit
             * bind/unbind call to the shader program, which is handled below by the call to runInShader.
             */
            shader.update(entity, entityManager, lightManager, frustumTransformation, cameraTransformation);
        }
    }



    @Override
    public void destroy() {
        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // TODO: this could be better, would be nice to have one call
        TextureShader.get().destroy();
        ColorShader.get().destroy();

        LOGGER.debug("OpenGL render system destroyed");
    }

    /**
     * A helper class used to generate and cache the view frustum for the render system. As the frustum only changes
     * when the dimensions of the window change (or when other less mutable values change, such as the Z values or the
     * field of view) the Frustum matrix (used as the starting place for calculating mesh vertex positions) can easily
     * be cached.
     */
    private static class Frustum {

        private float cachedMatrixWidth;
        private float cachedMatrixHeight;
        private Matrix4f cachedMatrix;

        public Matrix4f toMatrix(final float fov,
                                 final float width,
                                 final float height,
                                 final float zNear,
                                 final float zFar) {
            if (this.cachedMatrix != null && this.cachedMatrixWidth == width && this.cachedMatrixHeight == height) {
                return this.cachedMatrix;
            }

            final var aspectRatio = width / height;

            this.cachedMatrix = new Matrix4f()
                .identity()
                .perspective(fov, aspectRatio, zNear, zFar);
            this.cachedMatrixWidth = width;
            this.cachedMatrixHeight = height;

            return this.cachedMatrix;
        }
    }
}
