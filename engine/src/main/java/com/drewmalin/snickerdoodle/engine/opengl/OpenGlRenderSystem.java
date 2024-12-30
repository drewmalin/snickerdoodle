package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.Entity;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.system.RenderSystem;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OpenGlRenderSystem
    implements RenderSystem {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlRenderSystem.class);

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000f;

    private final Map<Entity, RenderMetadata> entityRenderMetadata;
    private final Transformation transformation;

    public OpenGlRenderSystem() {
        this.entityRenderMetadata = new HashMap<>();
        this.transformation = new Transformation();
    }

    @Override
    public void update(final Engine engine, final Window window, final Scene scene) {
        final var entityManager = scene.getEntityManager();
        final var lightManager = scene.getLightManager();

        final var frustumTransformation = this.transformation.getFrustumTransformation(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        final var cameraTransformation = this.transformation.getCameraTransformation(window.getCamera());

        final Set<Entity> entitiesWithMesh = entityManager.getEntitiesWithComponent(Mesh.class);
        for (final var entity : entitiesWithMesh) {

            // lazily compute the entity's render metadata
            final RenderMetadata metadata = this.entityRenderMetadata.computeIfAbsent(entity, k ->
                generateEntityRenderMetadata(entity, entityManager)
            );

            metadata.shaderProgram.runInShader((shader) -> {
                // calculate the projection and world matrices relative to this entity, passing them to the shader
                final var entityTransformation = this.transformation.getEntityTransformation(metadata.transform, cameraTransformation);

                shader.setFrustumTransformation(frustumTransformation);
                shader.setEntityTransformation(entityTransformation);
                shader.setMaterialTransformation(metadata.material());
                shader.setSpecularPowerTransformation(lightManager.getSpecularPower());
                shader.setAmbientLightTransformation(lightManager.getAmbientLight());

                for (final var light : lightManager.getPositionalLights()) {
                    final var lightCopy = light.copy();
                    final var lightPos = lightCopy.getPosition();
                    final var aux = new Vector4f(lightPos, 1f);

                    aux.mul(cameraTransformation);
                    lightPos.x = aux.x;
                    lightPos.y = aux.y;
                    lightPos.z = aux.z;
                    shader.setPositionalLightTransformation(lightCopy);
                }

                // render the entity's vertices
                GL30.glBindVertexArray(metadata.vaoID);
                GL11.glDrawElements(GL11.GL_TRIANGLES, metadata.mesh.getVertexRenderOrder().length, GL11.GL_UNSIGNED_INT, 0);
                GL30.glBindVertexArray(0);
            });
        }
    }

    @Override
    public void destroy() {
        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        for (final var metadata : this.entityRenderMetadata.values()) {
            metadata.destroy();
        }
        this.entityRenderMetadata.clear();
    }

    private RenderMetadata generateEntityRenderMetadata(final Entity entity, final EntityManager entityManager) {
        LOGGER.debug("Initializing metadata for entity {}", entity);

        final int vaoID;
        final int vertexVboID;
        final int normalVboID;
        final int indexVboID;
        final int colorVboID;

        // entities must have a mesh
        final Mesh mesh = entityManager.getComponent(entity, Mesh.class).orElseThrow();

        // if a material is not found, use a default
        final var material = entityManager.getComponent(entity, Material.class).orElse(
            new Color(
                new Vector4f(0.4f, 0.4f, 0.4f, 1.0f)
            )
        );

        // if a transform is not found, use a default
        final var transform = entityManager.getComponent(entity, Transform.class).orElse(
            new Transform()
        );

        // initialize VBO data
        FloatBuffer vboBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer idxBuffer = null;
        FloatBuffer normalBuffer = null;
        try {
            // prepare VAO
            vaoID = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoID);

            // prepare VBO (for vertex positions)
            final float[] vertices = mesh.getVertices();
            vboBuffer = MemoryUtil.memAllocFloat(vertices.length);
            vboBuffer.put(vertices).flip();

            vertexVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboID);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboBuffer, GL15.GL_STATIC_DRAW);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

            // prepare VBO (for shader)
            final float[] colors = material.getColorsForVertices(vertices);
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();

            colorVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVboID);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);

            // prepare VBO (for vertex normals)
            final float[] normals = mesh.getVertexNormals();
            normalBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();

            normalVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVboID);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
            GL20.glEnableVertexAttribArray(2);
            GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);

            // prepare VBO (for vertex indices)
            final int[] indices = mesh.getVertexRenderOrder();
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            indexVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexVboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL15.GL_STATIC_DRAW);

            // link shader program into this VBO
            material.getShaderProgram().link();

            // unbind VBO and VAO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);
        }
        finally {
            if (vboBuffer != null) {
                MemoryUtil.memFree(vboBuffer);
            }
            if (idxBuffer != null) {
                MemoryUtil.memFree(idxBuffer);
            }
            if (colorBuffer != null) {
                MemoryUtil.memFree(colorBuffer);
            }
            if (normalBuffer != null) {
                MemoryUtil.memFree(normalBuffer);
            }
        }

        return new RenderMetadata(vaoID, vertexVboID, colorVboID, normalVboID, indexVboID, mesh, material, material.getShaderProgram(), transform);
    }

    private static class Transformation {

        private final Matrix4f frustumTransformation;
        private final Matrix4f positionTransformation;
        private final Matrix4f cameraTransformation;

        public Transformation() {
            this.frustumTransformation = new Matrix4f();
            this.positionTransformation = new Matrix4f();
            this.cameraTransformation = new Matrix4f();
        }

        public Matrix4f getFrustumTransformation(final float fov,
                                                 final float width,
                                                 final float height,
                                                 final float zNear,
                                                 float zFar) {
            float aspectRatio = width / height;
            this.frustumTransformation.identity();
            this.frustumTransformation.perspective(fov, aspectRatio, zNear, zFar);
            return this.frustumTransformation;
        }

        public Matrix4f getCameraTransformation(final Camera camera) {
            final Vector3f cameraPosition = camera.getPosition();
            final Vector3f cameraTarget = camera.getTarget();
            this.cameraTransformation.identity()
                .lookAt(cameraPosition, cameraPosition.add(cameraTarget, new Vector3f()), Vectors.up())
                .translate(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());
            return this.cameraTransformation;
        }

        public Matrix4f getEntityTransformation(final Transform transform, final Matrix4f cameraTransformation) {
            this.positionTransformation.identity().translate(transform.getPosition())
                .rotateX((float) Math.toRadians(transform.getRotation().x()))
                .rotateY((float) Math.toRadians(transform.getRotation().y()))
                .rotateZ((float) Math.toRadians(transform.getRotation().z()))
                .scale(transform.getScale());
            final Matrix4f cameraCurrent = new Matrix4f(cameraTransformation);
            return cameraCurrent.mul(this.positionTransformation);
        }
    }

    /**
     * A simple record to hold onto cacheable render data for a given entity.
     */
    private record RenderMetadata(
        int vaoID,
        int vertexVboID,
        int colorVboID,
        int normalVboID,
        int indexVboID,
        Mesh mesh,
        Material material,
        OpenGlShaderProgram shaderProgram,
        Transform transform) {

        void destroy() {
            GL15.glDeleteBuffers(this.vertexVboID);
            GL15.glDeleteBuffers(this.colorVboID);
            GL15.glDeleteBuffers(this.indexVboID);
            GL15.glDeleteBuffers(this.normalVboID);
            GL30.glDeleteVertexArrays(this.vaoID);
            this.shaderProgram.destroy();
        }
    }
}
