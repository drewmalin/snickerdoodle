package com.drewmalin.snickerdoodle.engine.light;

import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.utils.Files;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

public class Shader {

    private static final String UNIFORM_FRUSTUM_TRANSFORMATION = "frustumTransformation";
    private static final String UNIFORM_ENTITY_TRANSFORMATION = "entityTransformation";
    private static final String UNIFORM_SPECULAR_POWER_TRANSFORMATION = "specularPower";
    private static final String UNIFORM_AMBIENT_LIGHT_TRANSFORMATION = "ambientLight";

    // default RGBA shader
    private static final String DEFAULT_RGBA_VERTEX_FILENAME = "/shaders/rgba_vertex.vs";
    private static final String DEFAULT_RGBA_FRAGMENT_FILENAME = "/shaders/rgba_fragment.fs";
    private static final Shader DEFAULT_RGBA = new Shader(
            Files.loadResource(DEFAULT_RGBA_VERTEX_FILENAME),
            Files.loadResource(DEFAULT_RGBA_FRAGMENT_FILENAME)
    );

    // default texture shader
    private static final String DEFAULT_TEXTURE_VERTEX_FILENAME = "/shaders/texture_vertex.vs";
    private static final String DEFAULT_TEXTURE_FRAGMENT_FILENAME = "/shaders/texture_fragment.fs";
    private static final Shader DEFAULT_TEXTURE = new Shader(
            Files.loadResource(DEFAULT_TEXTURE_VERTEX_FILENAME),
            Files.loadResource(DEFAULT_TEXTURE_FRAGMENT_FILENAME)
    );

    private int programID;
    private final String vertexProgramPath;
    private final String fragmentProgramPath;
    private final Map<String, Integer> uniforms;

    public Shader(final String vertexProgramPath, final String fragmentProgramPath) {
        this.vertexProgramPath = vertexProgramPath;
        this.fragmentProgramPath = fragmentProgramPath;
        this.uniforms = new HashMap<>();
    }

    public static Shader defaultRgba() {
        return DEFAULT_RGBA;
    }

    public static Shader defaultTexture() {
        return DEFAULT_TEXTURE;
    }

    public void link() {
        this.programID = GL20.glCreateProgram();
        final int vertexShaderID = initVertexShader(this.vertexProgramPath);
        final int fragmentShaderID = initFragmentShader(this.fragmentProgramPath);

        GL20.glLinkProgram(this.programID);
        if (GL20.glGetProgrami(this.programID, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + GL20.glGetProgramInfoLog(this.programID, 1024));
        }

        if (vertexShaderID != 0) {
            GL20.glDetachShader(this.programID, vertexShaderID);
        }
        if (fragmentShaderID != 0) {
            GL20.glDetachShader(this.programID, fragmentShaderID);
        }

        GL20.glValidateProgram(this.programID);
        if (GL20.glGetProgrami(this.programID, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(this.programID, 1024));
        }
    }

    private int initVertexShader(final String vertexProgramPath) {
        return initShader(vertexProgramPath, GL20.GL_VERTEX_SHADER);
    }

    private int initFragmentShader(final String fragmentProgramPath) {
        return initShader(fragmentProgramPath, GL20.GL_FRAGMENT_SHADER);
    }

    private int initShader(final String shaderProgramPath, int shaderType) {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        GL20.glShaderSource(shaderId, shaderProgramPath);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
        }

        GL20.glAttachShader(this.programID, shaderId);

        return shaderId;
    }

    public void prepareUniform(final String uniformName) {
        final var uniformLocation = GL20.glGetUniformLocation(this.programID, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("No uniform found for name: " + uniformName);
        }
        this.uniforms.put(uniformName, uniformLocation);
    }

    public void setUniformValue(final String uniformName, final Matrix4f value) {
        try (final var stack = MemoryStack.stackPush()) {
            final var fb = stack.mallocFloat(4 * 4);
            value.get(fb);
            GL20.glUniformMatrix4fv(this.uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniformValue(final String uniformName, final int value) {
        GL20.glUniform1i(this.uniforms.get(uniformName), value);
    }

    public void setUniformValue(final String uniformName, final float value) {
        GL20.glUniform1f(this.uniforms.get(uniformName), value);
    }

    public void setUniformValue(final String uniformName, final Vector3f value) {
        GL20.glUniform3f(this.uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniformValue(final String uniformName, final Vector4f value) {
        GL20.glUniform4f(this.uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void prepareMaterialTransformation() {
        prepareUniform("material.ambient");
        prepareUniform("material.diffuse");
        prepareUniform("material.specular");
        prepareUniform("material.reflectance");
    }

    public void setMaterialTransformation(final Material material) {
        setUniformValue("material.ambient", material.getAmbient());
        setUniformValue("material.diffuse", material.getDiffuse());
        setUniformValue("material.specular", material.getSpecular());
        setUniformValue("material.reflectance", material.getReflectance());
    }


    public void preparePositionalLightTransformation() {
        prepareUniform("positionalLight.color");
        prepareUniform("positionalLight.position");
        prepareUniform("positionalLight.intensity");
        prepareUniform("positionalLight.att.constant");
        prepareUniform("positionalLight.att.linear");
        prepareUniform("positionalLight.att.exponent");
    }

    public void setPositionalLightTransformation(final PositionalLight light) {
        setUniformValue("positionalLight.color", light.color());
        setUniformValue("positionalLight.position", light.getPosition());
        setUniformValue("positionalLight.intensity", light.getIntensity());
        setUniformValue("positionalLight.att.constant", light.getAttenuation().getConstant());
        setUniformValue("positionalLight.att.linear", light.getAttenuation().getLinear());
        setUniformValue("positionalLight.att.exponent", light.getAttenuation().getExponent());
    }

    public void prepareFrustumTransformation() {
        prepareUniform(UNIFORM_FRUSTUM_TRANSFORMATION);
    }

    public void setFrustumTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_FRUSTUM_TRANSFORMATION, value);
    }

    public void prepareEntityTransformation() {
        prepareUniform(UNIFORM_ENTITY_TRANSFORMATION);
    }

    public void setEntityTransformation(final Matrix4f value) {
        setUniformValue(UNIFORM_ENTITY_TRANSFORMATION, value);
    }

    public void prepareSpecularPowerTransformation() {
        prepareUniform(UNIFORM_SPECULAR_POWER_TRANSFORMATION);
    }

    public void setSpecularPowerTransformation(final float value) {
        setUniformValue(UNIFORM_SPECULAR_POWER_TRANSFORMATION, value);
    }

    public void prepareAmbientLightTransformation() {
        prepareUniform(UNIFORM_AMBIENT_LIGHT_TRANSFORMATION);
    }

    public void setAmbientLightTransformation(final Vector3f value) {
        setUniformValue(UNIFORM_AMBIENT_LIGHT_TRANSFORMATION, value);
    }

    public void bind() {
        GL20.glUseProgram(this.programID);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void destroy() {
        unbind();
        if (this.programID != 0) {
            GL20.glDeleteProgram(this.programID);
        }
    }
}