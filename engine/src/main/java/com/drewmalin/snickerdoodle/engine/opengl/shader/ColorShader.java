package com.drewmalin.snickerdoodle.engine.opengl.shader;

import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.Entity;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.light.LightManager;
import com.drewmalin.snickerdoodle.engine.light.PositionalLight;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlUtils;
import com.drewmalin.snickerdoodle.engine.utils.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/*
 * Created as an enum to act as a thread-safe singleton
 */
public class ColorShader
    extends OpenGlShader {

    private static final Logger LOGGER = LogManager.getLogger(ColorShader.class);

    private static final String VERTEX_SHADER_FILEPATH = "/shaders/rgba_vertex.vs";
    private static final String FRAGMENT_SHADER_FILEPATH = "/shaders/rgba_fragment.fs";

    private static final String UNIFORM_FRUSTUM_TRANSFORMATION = "frustumTransformation";
    private static final String UNIFORM_ENTITY_TRANSFORMATION = "entityTransformation";
    private static final String UNIFORM_SPECULAR_POWER_TRANSFORMATION = "specularPower";
    private static final String UNIFORM_AMBIENT_LIGHT_TRANSFORMATION = "ambientLight";
    private static final String MATERIAL_AMBIENT = "material.ambient";
    private static final String MATERIAL_DIFFUSE = "material.diffuse";
    private static final String MATERIAL_SPECULAR = "material.specular";
    private static final String MATERIAL_REFLECTANCE = "material.reflectance";
    private static final String POSITIONAL_LIGHT_COLOR = "positionalLight.color";
    private static final String POSITIONAL_LIGHT_POSITION = "positionalLight.position";
    private static final String POSITIONAL_LIGHT_INTENSITY = "positionalLight.intensity";
    private static final String POSITIONAL_LIGHT_ATT_CONSTANT = "positionalLight.att.constant";
    private static final String POSITIONAL_LIGHT_ATT_LINEAR = "positionalLight.att.linear";
    private static final String POSITIONAL_LIGHT_ATT_EXPONENT = "positionalLight.att.exponent";

    private final Map<Entity, OpenGlShaderMetadata> cachedMetadata;

    ColorShader() {
        super(Files.loadResource(VERTEX_SHADER_FILEPATH), Files.loadResource(FRAGMENT_SHADER_FILEPATH));
        this.cachedMetadata = new HashMap<>();
    }

    public static ColorShader get() {
        final var cachedShader = getCachedShader(ColorShader.class);
        if (cachedShader.isPresent()) {
            return (ColorShader) cachedShader.get();
        }

        final var shader = new ColorShader();
        cacheShader(ColorShader.class, shader);
        return shader;
    }

    @Override
    void onLink() {
        /*
         * Prepare the shaders for use by binding input names to OpenGL uniform variables.
         */
        prepareFrustumTransformation();
        prepareEntityTransformation();
        prepareSpecularPowerTransformation();
        prepareAmbientLightTransformation();
        prepareMaterialTransformation();
        preparePositionalLightTransformation();
    }

    @Override
    public void onUpdate(final Entity entity,
                         final EntityManager entityManager,
                         final LightManager lightManager,
                         final Matrix4f frustumTransformation,
                         final Matrix4f cameraTransformation) {

        final var metadata = getOrCreateRenderMetadata(entity, entityManager);

        /*
         * Step 1: pass the various inputs into the shader arguments.
         */
        setFrustumTransformation(frustumTransformation);
        setEntityTransformation(OpenGlUtils.getEntityTransformation(metadata.transform(), cameraTransformation));
        setMaterialTransformation(metadata.material());
        setSpecularPowerTransformation(lightManager.getSpecularPower());
        setAmbientLightTransformation(lightManager.getAmbientLight());

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
            setPositionalLightTransformation(lightCopy);
        }

        /*
         * Step 3: draw!
         */
        GL30.glBindVertexArray(metadata.vaoID());
        GL11.glDrawElements(GL11.GL_TRIANGLES, metadata.mesh().getVertexRenderOrder().length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    @Override
    void onDestroy() {
        for (final var metadata : this.cachedMetadata.values()) {
            metadata.destroy();
        }
    }

    private OpenGlShaderMetadata getOrCreateRenderMetadata(final Entity entity, final EntityManager entityManager) {
        /*
         * Check to see if we have already generated the render metadata for this entity.
         */
        if (this.cachedMetadata.containsKey(entity)) {
            return this.cachedMetadata.get(entity);
        }

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
         * Retrieve the Color from the entity. It is not required to have a color, so in the case that one is not
         * found, default to an opaque gray.
         */
        final var color = entityManager.getComponent(entity, Color.class).orElse(
            Color.builder().rgba(0.4f, 0.4f, 0.4f, 1.0f).build()
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
            final var colors = color.getColorsForVertices(vertices);
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
        link();

        /*
         * Unbind the VAO
         */
        GL30.glBindVertexArray(0);

        final var metadata = new OpenGlShaderMetadata(vaoID, positionVboID, colorVboID, normalVboID, indexVboID, mesh, color, this, transform);
        this.cachedMetadata.put(entity, metadata);
        return metadata;
    }

    private void prepareFrustumTransformation() {
        prepareUniform(UNIFORM_FRUSTUM_TRANSFORMATION);
    }

    private void prepareEntityTransformation() {
        prepareUniform(UNIFORM_ENTITY_TRANSFORMATION);
    }

    private void prepareSpecularPowerTransformation() {
        prepareUniform(UNIFORM_SPECULAR_POWER_TRANSFORMATION);
    }

    private void prepareAmbientLightTransformation() {
        prepareUniform(UNIFORM_AMBIENT_LIGHT_TRANSFORMATION);
    }

    private void prepareMaterialTransformation() {
        prepareUniform(MATERIAL_AMBIENT);
        prepareUniform(MATERIAL_DIFFUSE);
        prepareUniform(MATERIAL_SPECULAR);
        prepareUniform(MATERIAL_REFLECTANCE);
    }

    private void preparePositionalLightTransformation() {
        prepareUniform(POSITIONAL_LIGHT_COLOR);
        prepareUniform(POSITIONAL_LIGHT_POSITION);
        prepareUniform(POSITIONAL_LIGHT_INTENSITY);
        prepareUniform(POSITIONAL_LIGHT_ATT_CONSTANT);
        prepareUniform(POSITIONAL_LIGHT_ATT_LINEAR);
        prepareUniform(POSITIONAL_LIGHT_ATT_EXPONENT);
    }

    private void setFrustumTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_FRUSTUM_TRANSFORMATION, value);
    }

    private void setEntityTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_ENTITY_TRANSFORMATION, value);
    }

    private void setSpecularPowerTransformation(final float value) {
        setUniformValue(UNIFORM_SPECULAR_POWER_TRANSFORMATION, value);
    }

    private void setAmbientLightTransformation(final Vector3f value) {
        setUniformValue(UNIFORM_AMBIENT_LIGHT_TRANSFORMATION, value);
    }

    private void setMaterialTransformation(final Material material) {
        setUniformValue(MATERIAL_AMBIENT, material.getAmbient());
        setUniformValue(MATERIAL_DIFFUSE, material.getDiffuse());
        setUniformValue(MATERIAL_SPECULAR, material.getSpecular());
        setUniformValue(MATERIAL_REFLECTANCE, material.getReflectance());
    }

    private void setPositionalLightTransformation(final PositionalLight light) {
        setUniformValue(POSITIONAL_LIGHT_COLOR, light.color());
        setUniformValue(POSITIONAL_LIGHT_POSITION, light.getPosition());
        setUniformValue(POSITIONAL_LIGHT_INTENSITY, light.getIntensity());
        setUniformValue(POSITIONAL_LIGHT_ATT_CONSTANT, light.getAttenuation().constant());
        setUniformValue(POSITIONAL_LIGHT_ATT_LINEAR, light.getAttenuation().linear());
        setUniformValue(POSITIONAL_LIGHT_ATT_EXPONENT, light.getAttenuation().exponent());
    }

    private record OpenGlShaderMetadata(
        int vaoID,
        int vertexVboID,
        int colorVboID,
        int normalVboID,
        int indexVboID,
        Mesh mesh,
        Material material,
        OpenGlShader shaderProgram,
        Transform transform) {

        public void destroy() {
            GL15.glDeleteBuffers(this.vertexVboID);
            GL15.glDeleteBuffers(this.colorVboID);
            GL15.glDeleteBuffers(this.indexVboID);
            GL15.glDeleteBuffers(this.normalVboID);
            GL30.glDeleteVertexArrays(this.vaoID);
        }
    }
}
