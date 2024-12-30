package com.drewmalin.snickerdoodle.engine.ecs.component;

import com.drewmalin.snickerdoodle.engine.light.ShaderProgram;
import org.joml.Vector4f;

public abstract class Material implements Component {

    private final Vector4f ambient;
    private final Vector4f diffuse;
    private final Vector4f specular;
    private final float reflectance;
    private final ShaderProgram shaderProgram;

    public Material(final Vector4f ambient,
                    final Vector4f diffuse,
                    final Vector4f specular,
                    final float reflectance,
                    final ShaderProgram shaderProgram) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.reflectance = reflectance;
        this.shaderProgram = shaderProgram;
    }

    public Vector4f getAmbient() {
        return this.ambient;
    }

    public Vector4f getDiffuse() {
        return this.diffuse;
    }

    public Vector4f getSpecular() {
        return this.specular;
    }

    public float getReflectance() {
        return this.reflectance;
    }

    public ShaderProgram getShaderProgram() {
        return this.shaderProgram;
    }

    public abstract float[] getColorsForVertices(float[] vertices);
}
