package com.drewmalin.snickerdoodle.engine.opengl.shader;

import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;
import com.drewmalin.snickerdoodle.engine.ecs.component.Texture14;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.Entity;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.light.LightManager;
import com.drewmalin.snickerdoodle.engine.utils.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.drewmalin.snickerdoodle.engine.opengl.shader.Utils.resourceToByteBuffer;

public class TextureShader
    extends OpenGlShader {

    private static final Logger LOGGER = LogManager.getLogger(TextureShader.class);

    private static final String VERTEX_SHADER_FILEPATH = "/shaders/texture_vertex.vs";
    private static final String FRAGMENT_SHADER_FILEPATH = "/shaders/texture_fragment.fs";

    private static final String UNIFORM_TEXTURE_SAMPLER = "texture_sampler";
    private static final String UNIFORM_FRUSTUM_TRANSFORMATION = "frustumTransformation";
    private static final String UNIFORM_ENTITY_TRANSFORMATION = "entityTransformation";

    private final Map<Entity, OpenGlShaderMetadata> cachedMetadata;

    TextureShader() {
        super(Files.loadResource(VERTEX_SHADER_FILEPATH), Files.loadResource(FRAGMENT_SHADER_FILEPATH));
        this.cachedMetadata = new HashMap<>();
    }

    public static TextureShader get() {
        final var cachedShader = getCachedShader(TextureShader.class);
        if (cachedShader.isPresent()) {
            return (TextureShader) cachedShader.get();
        }

        final var shader = new TextureShader();
        cacheShader(TextureShader.class, shader);
        return shader;
    }

    @Override
    void onLink() {
        /*
         * Prepare the shaders for use by binding input names to OpenGL uniform variables.
         */
        prepareTextureSampler();
        prepareFrustumTransformation();
        prepareEntityTransformation();
    }

    @Override
    public void onUpdate(final Entity entity,
                         final EntityManager entityManager,
                         final LightManager lightManager,
                         final Matrix4f frustumTransformation,
                         final Matrix4f cameraTransformation) {
        final var metadata = getOrCreateRenderMetadata(entity, entityManager);

        setTextureSampler(0);
        setFrustumTransformation(frustumTransformation);
        setEntityTransformation(com.drewmalin.snickerdoodle.engine.opengl.Utils.getEntityTransformation(metadata.transform(), cameraTransformation));

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, metadata.textureID);

        GL30.glBindVertexArray(metadata.vaoID());
        GL11.glDrawElements(GL11.GL_TRIANGLES, metadata.mesh().getVertexRenderOrder().length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    private OpenGlShaderMetadata getOrCreateRenderMetadata(final Entity entity, final EntityManager entityManager) {
        if (this.cachedMetadata.containsKey(entity)) {
            return this.cachedMetadata.get(entity);
        }

        LOGGER.debug("Initializing metadata for entity {}", entity);


        /*
         * These IDs will ultimately act as handles into the OpenGL VAO content. These are all that need to be stored
         * on the final RenderMetadata object.
         */
        final int vaoID;
        final int textureID;
        final int positionVboID;
        final int textureVboID;
        final int indexVboID;

        /*
         * Retrieve the Mesh from the entity, to be referenced when retrieving vertex data. If no Mesh is found,
         * something wrong must have occurred (as the render system should have used a call like the following:
         * entityManager.getEntitiesWithComponent(Mesh.class)).
         */
        final Mesh mesh = entityManager.getComponent(entity, Mesh.class).orElseThrow();

        /*
         * Retrieve the Texture from the entity. It is not required to have a texture.
         */
        final var texture = entityManager.getComponent(entity, Texture14.class).orElseThrow();

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
         * layout (location =1) in vec2 texCoord;
         *
         * so, the below will create these VBOs of the specified vector type in the specified order.
         */
        FloatBuffer positionVBOBuffer = null;
        FloatBuffer textureVBOBuffer = null;
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

            System.out.println("loading");

            /*
             * Load the texture into memory.
             */
            try (final MemoryStack stack = MemoryStack.stackPush()) {
                final var widthBuffer = stack.mallocInt(1);
                final var heightBuffer = stack.mallocInt(1);
                final var channelsBuffer = stack.mallocInt(1);

                final var textureImageBuffer = resourceToByteBuffer(texture.getFilePath(), 8 * 1024);
                final var buffer = STBImage.stbi_load_from_memory(textureImageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
                if (buffer == null) {
                    throw new RuntimeException("Failed to load texture file at location " + texture.getFilePath());
                }

                final var width = widthBuffer.get();
                final var height = heightBuffer.get();

                textureID = GL30.glGenTextures();
                GL30.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
                GL30.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                GL30.glGenerateMipmap(textureID);
            }
            catch (final IOException e) {
                throw new RuntimeException("Failed to load texture", e);
            }

            /*
             * Prepare the buffer for the "position" shader input.
             */
            final var vertices = mesh.getVertices();
            positionVBOBuffer = MemoryUtil.memAllocFloat(vertices.length);
            positionVBOBuffer.put(vertices).flip();

            /*
             * Prepare the buffer for the "texCoord" shader input.
             */
            final var coords = texture.getCoordinates();
            textureVBOBuffer = MemoryUtil.memAllocFloat(coords.length);
            textureVBOBuffer.put(coords).flip();

            /*
             * Create VBOs within the context of this VAO, ensuring that the input index corresponds to the "location"
             * specified in the shader program.
             */
            positionVboID = createAndLoadVBO(0, positionVBOBuffer, 3);
            textureVboID = createAndLoadVBO(1, textureVBOBuffer, 2);

            /*
             * At the end of the VAO, add a final VBO that contains the render order for each mesh. This will be used
             * to construct polygons out of the individual vertices during the Geometry Processing phase of the graphics
             * pipeline. Note that the shader will not reference this data as an input -- we will use it in the update
             * loop directly.
             */
            final var indices = mesh.getVertexRenderOrder();
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            indexVboID = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexVboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }
        finally {
            /*
             * Now that the memory has been loaded from the Java heap into VRAM, we can clear the unneeded Java buffers
             */
            if (idxBuffer != null) {
                MemoryUtil.memFree(idxBuffer);
            }
            if (textureVBOBuffer != null) {
                MemoryUtil.memFree(textureVBOBuffer);
            }
            if (positionVBOBuffer != null) {
                MemoryUtil.memFree(positionVBOBuffer);
            }
        }

        /*
         * Link the mesh's shader program into this VAO
         */
        link();

        /*
         * Unbind the VAO
         */
        GL30.glBindVertexArray(0);

        final var metadata = new OpenGlShaderMetadata(vaoID, textureID, positionVboID, textureVboID, indexVboID, mesh, texture, this, transform);
        this.cachedMetadata.put(entity, metadata);
        return metadata;
    }

    private void prepareTextureSampler() {
        prepareUniform(UNIFORM_TEXTURE_SAMPLER);
    }

    private void prepareFrustumTransformation() {
        prepareUniform(UNIFORM_FRUSTUM_TRANSFORMATION);
    }

    private void prepareEntityTransformation() {
        prepareUniform(UNIFORM_ENTITY_TRANSFORMATION);
    }

    private void setTextureSampler(final int value) {
        setUniformValue(UNIFORM_TEXTURE_SAMPLER, value);
    }

    private void setFrustumTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_FRUSTUM_TRANSFORMATION, value);
    }

    private void setEntityTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_ENTITY_TRANSFORMATION, value);
    }

    @Override
    public void onDestroy() {
        for (final var metadata : this.cachedMetadata.values()) {
            metadata.destroy();
        }
    }

    private record OpenGlShaderMetadata(
        int vaoID,
        int textureID,
        int vertexVboID,
        int textCoordVboID,
        int indexVboID,
        Mesh mesh,
        Material material,
        OpenGlShader shaderProgram,
        Transform transform) {

        public void destroy() {
            GL15.glDeleteBuffers(this.vertexVboID);
            GL30.glDeleteTextures(this.textureID);
            GL15.glDeleteBuffers(this.textCoordVboID);
            GL15.glDeleteBuffers(this.indexVboID);
            GL30.glDeleteVertexArrays(this.vaoID);
        }
    }
}
