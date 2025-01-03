package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.light.PositionalLight;
import com.drewmalin.snickerdoodle.engine.utils.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A representation of a compiled and linked program corresponding to a vertex and fragment shader. This program may be
 * "bound" into context, at which time various parameters may be set (these parameters link to those defined in the
 * original shader source code).
 */

// TODO: introduce interface
public class OpenGlShaderProgram {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlShaderProgram.class);

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

    private final int vertexShaderID;
    private final int fragmentShaderID;
    private final Map<String, Integer> uniforms;

    private int programID;

    private OpenGlShaderProgram(final String vertexShaderSourceFile, final String fragmentShaderSourceFile) {
        this.vertexShaderID = compile(vertexShaderSourceFile, GL20.GL_VERTEX_SHADER);
        this.fragmentShaderID = compile(fragmentShaderSourceFile, GL20.GL_FRAGMENT_SHADER);
        this.uniforms = new HashMap<>();
    }

    private int compile(final String shaderProgramPath, int shaderType) {
        /*
         * Create a new shader of the specified shader type. A failure to create this program results in a hard error.
         */
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        /*
         * Specify the source code for the shader and compile it.
         */
        GL20.glShaderSource(shaderId, shaderProgramPath);
        GL20.glCompileShader(shaderId);

        /*
         * Get the results of code compilation. If the status is 0, then an error occurred.
         */
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
        }

        return shaderId;
    }

    /**
     * Prepare this shader for use by OpenGL.
     */
    public void link() {
        /*
         * Create a new program object.
         */
        this.programID = GL20.glCreateProgram();

        /*
         * Link the shader to the parent program.
         */
        if (vertexShaderID != 0) {
            GL20.glAttachShader(this.programID, vertexShaderID);
        }
        if (fragmentShaderID != 0) {
            GL20.glAttachShader(this.programID, fragmentShaderID);
        }

        /*
         * Link the shader programs to the parent program object.
         */
        GL20.glLinkProgram(this.programID);
        if (GL20.glGetProgrami(this.programID, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + GL20.glGetProgramInfoLog(this.programID, 1024));
        }

        /*
         * Now that the shaders have been linked to this program, the memory used for the compiled shaders can be freed.
         */
        if (vertexShaderID != 0) {
            GL20.glDetachShader(this.programID, vertexShaderID);
        }
        if (fragmentShaderID != 0) {
            GL20.glDetachShader(this.programID, fragmentShaderID);
        }

        /*
         * Perform a validation of the resultant program.
         */
        GL20.glValidateProgram(this.programID);
        if (GL20.glGetProgrami(this.programID, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(this.programID, 1024));
        }

        /*
         * Finally, prepare the shaders for use by binding input names to OpenGL uniform variables.
         */
        prepareFrustumTransformation();
        prepareEntityTransformation();
        prepareSpecularPowerTransformation();
        prepareAmbientLightTransformation();
        prepareMaterialTransformation();
        preparePositionalLightTransformation();
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

    /**
     * Runs the provided {@link Consumer} within the context of this shader program.
     */
    public void runInShader(final Consumer<OpenGlShaderProgram> consumer) {
        bind();
        consumer.accept(this);
        unbind();
    }

    public void setFrustumTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_FRUSTUM_TRANSFORMATION, value);
    }

    public void setEntityTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_ENTITY_TRANSFORMATION, value);
    }

    public void setSpecularPowerTransformation(final float value) {
        setUniformValue(UNIFORM_SPECULAR_POWER_TRANSFORMATION, value);
    }

    public void setAmbientLightTransformation(final Vector3f value) {
        setUniformValue(UNIFORM_AMBIENT_LIGHT_TRANSFORMATION, value);
    }

    public void setMaterialTransformation(final Material material) {
        setUniformValue(MATERIAL_AMBIENT, material.getAmbient());
        setUniformValue(MATERIAL_DIFFUSE, material.getDiffuse());
        setUniformValue(MATERIAL_SPECULAR, material.getSpecular());
        setUniformValue(MATERIAL_REFLECTANCE, material.getReflectance());
    }

    public void setPositionalLightTransformation(final PositionalLight light) {
        setUniformValue(POSITIONAL_LIGHT_COLOR, light.color());
        setUniformValue(POSITIONAL_LIGHT_POSITION, light.getPosition());
        setUniformValue(POSITIONAL_LIGHT_INTENSITY, light.getIntensity());
        setUniformValue(POSITIONAL_LIGHT_ATT_CONSTANT, light.getAttenuation().constant());
        setUniformValue(POSITIONAL_LIGHT_ATT_LINEAR, light.getAttenuation().linear());
        setUniformValue(POSITIONAL_LIGHT_ATT_EXPONENT, light.getAttenuation().exponent());
    }

    private void prepareUniform(final String uniformName) {
        final var uniformLocation = GL20.glGetUniformLocation(this.programID, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("No uniform found for name: " + uniformName);
        }
        this.uniforms.put(uniformName, uniformLocation);
    }

    private void setUniformValue(final String uniformName, final Matrix4f value) {
        try (final var stack = MemoryStack.stackPush()) {
            final var fb = stack.mallocFloat(4 * 4);
            value.get(fb);
            GL20.glUniformMatrix4fv(this.uniforms.get(uniformName), false, fb);
        }
    }

    private void setUniformValue(final String uniformName, final int value) {
        GL20.glUniform1i(this.uniforms.get(uniformName), value);
    }

    private void setUniformValue(final String uniformName, final float value) {
        GL20.glUniform1f(this.uniforms.get(uniformName), value);
    }

    private void setUniformValue(final String uniformName, final Vector3f value) {
        GL20.glUniform3f(this.uniforms.get(uniformName), value.x, value.y, value.z);
    }

    private void setUniformValue(final String uniformName, final Vector4f value) {
        GL20.glUniform4f(this.uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    private void bind() {
        GL20.glUseProgram(this.programID);
    }

    private void unbind() {
        GL20.glUseProgram(0);
    }

    public void destroy() {
        unbind();
        if (this.programID != 0) {
            GL20.glDeleteProgram(this.programID);
        }
    }

    @Override
    public String toString() {
        return "Shader["
            + "programID=" + this.programID
            + ']';
    }

    /**
     * The name of this enum and the enclosing class ({@link OpenGlShaderProgram}) are slightly off, but intentional. This
     * enum is intended to fundamentally trigger the compilation of all shaders exactly once (by virtue of this enum
     * being evaluated at class-load time, with each value invoking the {@link OpenGlShaderProgram} constructor).
     */
    private enum Shader {
        TEXTURE(
            "/shaders/texture_vertex.vs",
            "/shaders/texture_fragment.fs"),
        RGBA(
            "/shaders/rgba_vertex.vs",
            "/shaders/rgba_fragment.fs"),
        ;

        private final OpenGlShaderProgram shaderProgram;

        Shader(final String vertexFilePath, final String fragmentFilePath) {
            final var vertexShaderSource = Files.loadResource(vertexFilePath);
            final var fragmentShaderSource = Files.loadResource(fragmentFilePath);

            LOGGER.info("Compiling shader from source: [vertexFilePath=\"{}\", fragmentFilePath=\"{}\"]", vertexFilePath, fragmentFilePath);
            this.shaderProgram = new OpenGlShaderProgram(vertexShaderSource, fragmentShaderSource);
        }
    }

    public static OpenGlShaderProgram defaultRgba() {
        return Shader.RGBA.shaderProgram;
    }

    public static OpenGlShaderProgram defaultTexture() {
        return Shader.TEXTURE.shaderProgram;
    }
}