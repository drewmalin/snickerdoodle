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

public class OpenGlRenderSystem
    implements RenderSystem {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlRenderSystem.class);

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000f;

    private final Frustum frustum;
    private final Map<Entity, RenderMetadata> entityRenderMetadata;

    public OpenGlRenderSystem() {
        this.frustum = new Frustum();
        this.entityRenderMetadata = new HashMap<>();
    }

    @Override
    public void update(final Engine engine, final Window window, final Scene scene) {
        final var entityManager = scene.getEntityManager();
        final var lightManager = scene.getLightManager();

        final var frustumTransformation = this.frustum.toMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        final var cameraTransformation = getCameraTransformation(window.getCamera());

        for (final var entity : entityManager.getEntitiesWithComponent(Mesh.class)) {

            /*
             * Fetch (or generate) the render metadata for this entity. Render metadata essentially a bag of pointers
             * to objects like the entity's Mesh, Transform, and Shader, but it also includes the one-time setup of
             * OpenGL vertex buffer object (VBO) which contains the inputs to graphics pipeline that will be used
             * downstream by the shader. In this model, the original fixed vertex positions, normals, colors, and
             * indices are created once per entity and cached in the entityRenderMetadata map. When it comes time to
             * render a particular entity, that entity's new position and color information is passed into its shader.
             */
            final var metadata = this.entityRenderMetadata.computeIfAbsent(entity, k ->

                /*
                 * This step is done lazily as entities may be added over time after the render system has been started.
                 * If this is done, the above computeIfAbsent check will miss, and a new metadata object will be
                 * created.
                 */
                generateEntityRenderMetadata(entity, entityManager)
            );

            /*
             * Rendering is ultimately done by the shader itself, so the below sets the new vertex positions, normals,
             * colors, lighting, and other inputs before invoking a call to "draw". This is done within an implicit
             * bind/unbind call to the shader program, which is handled below by the call to runInShader.
             */
            metadata.shaderProgram.runInShader((shader) -> {

                /*
                 * Step 1: pass the various inputs into the shader arguments.
                 */
                shader.setFrustumTransformation(frustumTransformation);
                shader.setEntityTransformation(getEntityTransformation(metadata.transform, cameraTransformation));
                shader.setMaterialTransformation(metadata.material());
                shader.setSpecularPowerTransformation(lightManager.getSpecularPower());
                shader.setAmbientLightTransformation(lightManager.getAmbientLight());

                /*
                 * Step 2: as a special case, set the locations of the (possibly-mobile!) positional lights.
                 */
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

                /*
                 * Step 3: invoke glDrawElements to initiate a call to the GPU. Note that this call will be done within
                 * the context of the shader due to the enclosing runInShader call.
                 */
                GL30.glBindVertexArray(metadata.vaoID);
                GL11.glDrawElements(GL11.GL_TRIANGLES, metadata.mesh.getVertexRenderOrder().length, GL11.GL_UNSIGNED_INT, 0);
                GL30.glBindVertexArray(0);
            });
        }
    }

    public Matrix4f getCameraTransformation(final Camera camera) {
        final var cameraPosition = camera.getPosition();
        final var cameraTarget = camera.getTarget();

        return new Matrix4f()
            .identity()
            .lookAt(cameraPosition, cameraPosition.add(cameraTarget, new Vector3f()), Vectors.up())
            .translate(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());
    }

    public Matrix4f getEntityTransformation(final Transform transform, final Matrix4f cameraTransformation) {
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

        /*
         * These IDs will ultimately act as handles into the OpenGL VAO content. These are all that need to be stored
         * on the final RenderMetadata object.
         */
        final int vaoID;
        final int positionVboID;
        final int normalVboID;
        final int colorVboID;
        final int indexVboID;


        /*
         * Retrieve the Mesh from the entity, to be referenced when retrieving vertex data. If no Mesh is found,
         * something wrong must have occurred (as the render system should have used a call like the following:
         * entityManager.getEntitiesWithComponent(Mesh.class)).
         */
        final Mesh mesh = entityManager.getComponent(entity, Mesh.class).orElseThrow();

        /*
         * Retrieve the Material from the entity. It is not required to have a material, so in the case that one is not
         * found, default to an opaque gray.
         */
        final var material = entityManager.getComponent(entity, Material.class).orElse(
            new Color(
                new Vector4f(0.4f, 0.4f, 0.4f, 1.0f)
            )
        );

        /*
         * Retrieve the Transform from the entity. It is not required to have a transform, so in the case that one is
         * not found, default to a 0-ed one (no scaling, no rotation, no position).
         */
        final var transform = entityManager.getComponent(entity, Transform.class).orElse(
            new Transform()
        );


        /*
         * Initialize Java buffers to be used as the sources of data for the below OpenGL VBOs. In this case each VBO
         * represents the data expected by the shader program, and must be declared in the appropriate order. To start,
         * the shaders used by this engine have the following block for data:
         *
         * layout (location =0) in vec3 position;
         * layout (location =1) in vec4 inColor;
         * layout (location =2) in vec3 vertexNormal;
         *
         * so, the below will create these VBOs of the specified vector type in the specified order.
         */
        FloatBuffer positionVBOBuffer = null;
        FloatBuffer colorBuffer = null;
        FloatBuffer normalBuffer = null;
        IntBuffer idxBuffer = null;

        /*
         * Prepare and set in context (bind) the VAO for this entity.
         */
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        /*
         * Create and bind the VBOs to this VAO
         */
        try {
            /*
             * Prepare the buffer for the "position" shader input.
             */
            final var vertices = mesh.getVertices();
            positionVBOBuffer = MemoryUtil.memAllocFloat(vertices.length);
            positionVBOBuffer.put(vertices).flip();

            /*
             * Prepare the buffer for the "inColor" shader input.
             */
            final var colors = material.getColorsForVertices(vertices);
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();

            /*
             * Prepare the buffer for the "vertexNormals" shader input.
             */
            final var normals = mesh.getVertexNormals();
            normalBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();

            /*
             * Create VBOs within the context of this VAO, ensuring that the input index corresponds to the "location"
             * specified in the shader program.
             */
            positionVboID = createAndLoadVBO(0, positionVBOBuffer, 3);
            colorVboID = createAndLoadVBO(1, colorBuffer, 4);
            normalVboID = createAndLoadVBO(2, normalBuffer, 3);

            /*
             * At the end of the VAO, add a final VBO that contains the render order for each mesh. This will be used
             * to construct polygons out of the individual vertices during the Geometry Processing phase of the graphics
             * pipeline.
             */
            final var indices = mesh.getVertexRenderOrder();
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            indexVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexVboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL15.GL_STATIC_DRAW);
        }
        finally {
            /*
             * Now that the memory has been loaded from the Java heap into VRAM, we can clear the unneeded Java buffers
             */
            if (idxBuffer != null) {
                MemoryUtil.memFree(idxBuffer);
            }
            if (normalBuffer != null) {
                MemoryUtil.memFree(normalBuffer);
            }
            if (colorBuffer != null) {
                MemoryUtil.memFree(colorBuffer);
            }
            if (positionVBOBuffer != null) {
                MemoryUtil.memFree(positionVBOBuffer);
            }
        }

        /*
         * Link the mesh's shader program into this VAO
         */
        material.getShaderProgram().link();

        /*
         * Unbind any VBO
         */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        /*
         * Unbind the VAO
         */
        GL30.glBindVertexArray(0);

        return new RenderMetadata(vaoID, positionVboID, colorVboID, normalVboID, indexVboID, mesh, material, material.getShaderProgram(), transform);
    }

    private int createAndLoadVBO(final int inputAttributeIndex, final FloatBuffer buffer, final int elementsPerVertex) {
        final int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(inputAttributeIndex);
        GL20.glVertexAttribPointer(inputAttributeIndex, elementsPerVertex, GL11.GL_FLOAT, false, 0, 0);
        return vboID;
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
