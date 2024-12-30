package com.drewmalin.snickerdoodle.engine.ecs.component;

import com.drewmalin.snickerdoodle.engine.opengl.OpenGlShaderProgram;
import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import org.joml.Vector4f;

public class Color extends Material {

    public Color(final Vector4f rgba, final OpenGlShaderProgram shaderProgram) {
        super(
                rgba,
                rgba,
                rgba,
                0,
            shaderProgram
        );
    }

    public Color(final Vector4f rgba) {
        this(rgba, OpenGlShaderProgram.defaultRgba());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public float[] getColorsForVertices(final float[] vertices) {
        final float[] colors = new float[(vertices.length / 3) * 4];

        int colorIdx = 0;
        for (int i = 0; i < vertices.length; i += 3) {
            colors[colorIdx++] = getAmbient().x();
            colors[colorIdx++] = getAmbient().y();
            colors[colorIdx++] = getAmbient().z();
            colors[colorIdx++] = getAmbient().w();
        }
        return colors;
    }

    @Override
    public String toString() {
        return "Color["
            + "rgba=" + Vectors.toString(getAmbient()) + ", "
            + "shader=" + getShaderProgram()
            + "]";
    }

    public static class Builder {
        private float r;
        private float g;
        private float b;
        private float a;
        private OpenGlShaderProgram shaderProgram;

        public Builder red(final float r) {
            this.r = r;
            return this;
        }


        public Builder green(final float g) {
            this.g = g;
            return this;
        }

        public Builder blue(final float b) {
            this.b = b;
            return this;
        }

        public Builder alpha(final float a) {
            this.a = a;
            return this;
        }

        public Builder shader(final OpenGlShaderProgram shaderProgram) {
            this.shaderProgram = shaderProgram;
            return this;
        }

        public Color build() {
            return new Color(new Vector4f(this.r, this.g, this.b, this.a));
        }
    }
}
