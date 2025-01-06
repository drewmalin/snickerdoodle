package com.drewmalin.snickerdoodle.engine.opengl.shader;

import com.drewmalin.snickerdoodle.engine.ecs.entity.Entity;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.light.LightManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A representation of a compiled and linked program corresponding to a vertex and fragment shader. This program may be
 * "bound" into context, at which time various parameters may be set (these parameters link to those defined in the
 * original shader source code).
 */
public abstract class OpenGlShader {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlShader.class);

    private static final Map<Class<? extends OpenGlShader>, OpenGlShader> cache = new HashMap<>();

    private final int vertexShaderID;
    private final int fragmentShaderID;
    private final Map<String, Integer> uniforms;

    private int programID;

    OpenGlShader(final String vertexShaderSourceFile, final String fragmentShaderSourceFile) {
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

    static Optional<? extends OpenGlShader> getCachedShader(final Class<? extends OpenGlShader> clazz) {
        return Optional.ofNullable(cache.get(clazz));
    }

    static void cacheShader(final Class<? extends OpenGlShader> clazz, final OpenGlShader shader) {
        cache.put(clazz, shader);
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
         * Finally, allow the shader implementation to complete linking.
         */
        onLink();
    }

    /**
     * Custom implementation of this shader's link routine.
     */
    abstract void onLink();

    /**
     * Updates this shader, passing the provided arguments to the prepared shader program.
     */
    public void update(Entity entity,
                       EntityManager entityManager,
                       LightManager lightManager,
                       Matrix4f frustumTransformation,
                       Matrix4f cameraTransformation) {
        bind();
        onUpdate(entity, entityManager, lightManager, frustumTransformation, cameraTransformation);
        unbind();
    }

    /**
     * Custom implementation of this shader's update routine.
     */
    abstract void onUpdate(Entity entity,
                           EntityManager entityManager,
                           LightManager lightManager,
                           Matrix4f frustumTransformation,
                           Matrix4f cameraTransformation);

    void prepareUniform(final String uniformName) {
        final var uniformLocation = GL20.glGetUniformLocation(this.programID, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("No uniform found for name: " + uniformName);
        }
        this.uniforms.put(uniformName, uniformLocation);
    }

    void setUniformValue(final String uniformName, final Matrix4f value) {
        try (final var stack = MemoryStack.stackPush()) {
            final var fb = stack.mallocFloat(4 * 4);
            value.get(fb);
            GL20.glUniformMatrix4fv(this.uniforms.get(uniformName), false, fb);
        }
    }

    void setUniformValue(final String uniformName, final int value) {
        GL20.glUniform1i(this.uniforms.get(uniformName), value);
    }

    void setUniformValue(final String uniformName, final float value) {
        GL20.glUniform1f(this.uniforms.get(uniformName), value);
    }

    void setUniformValue(final String uniformName, final Vector3f value) {
        GL20.glUniform3f(this.uniforms.get(uniformName), value.x, value.y, value.z);
    }

    void setUniformValue(final String uniformName, final Vector4f value) {
        GL20.glUniform4f(this.uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    int createAndLoadVBO(final int inputAttributeIndex, final FloatBuffer buffer, final int elementsPerVertex) {
        /*
         * Create and bind the new VBO
         */
        final int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

        /*
         * Write the buffer data into OpenGL for eventual storage in VRAM
         */
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        /*
         * Set the location (really, the offset) of this location in the greater VAO
         */
        GL20.glEnableVertexAttribArray(inputAttributeIndex);

        /*
         * Specify the number of elements for each entry per vertex.
         */
        GL20.glVertexAttribPointer(inputAttributeIndex, elementsPerVertex, GL11.GL_FLOAT, false, 0, 0);

        /*
         * Unbind the VBO to prevent further operations from unintentionally associating data with it
         */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vboID;
    }

    private void bind() {
        GL20.glUseProgram(this.programID);
    }

    private void unbind() {
        GL20.glUseProgram(0);
    }

    public void destroy() {
        onDestroy();
        unbind();
        if (this.programID != 0) {
            GL20.glDeleteProgram(this.programID);
        }
    }

    abstract void onDestroy();

    @Override
    public String toString() {
        return "Shader["
            + "programID=" + this.programID
            + ']';
    }
}