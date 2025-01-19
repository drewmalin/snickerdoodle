package com.drewmalin.snickerdoodle.engine.ecs.component;

import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import org.joml.Vector4f;

public class Color
    implements Material {

    private static final Color RED = fromRGB(1.0f, 0.0f, 0.0f);
    private static final Color GREEN = fromRGB(0.0f, 1.0f, 0.0f);
    private static final Color BLUE = fromRGB(0.0f, 0.0f, 1.0f);
    private static final Color YELLOW = fromRGB(1.0f, 1.0f, 0.0f);
    private static final Color GRAY = fromRGB(0.4f, 0.4f, 0.4f);

    private final Vector4f ambient;
    private final Vector4f diffuse;
    private final Vector4f specular;
    private final float reflectance;

    private Color(final Vector4f rgba) {
        this.ambient = rgba;
        this.diffuse = rgba;
        this.specular = rgba;
        this.reflectance = 0;
    }

    private Color(final Builder builder) {
        this(
            new Vector4f(
                builder.r,
                builder.g,
                builder.b,
                builder.a
            )
        );
    }

    @Override
    public Vector4f getAmbient() {
        return this.ambient;
    }

    @Override
    public Vector4f getDiffuse() {
        return this.diffuse;
    }

    @Override
    public Vector4f getSpecular() {
        return this.specular;
    }

    @Override
    public float getReflectance() {
        return this.reflectance;
    }

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
            + "]";
    }

    public static Color red() {
        return RED;
    }

    public static Color green() {
        return GREEN;
    }

    public static Color blue() {
        return BLUE;
    }

    public static Color yellow() {
        return YELLOW;
    }

    public static Color gray() {
        return GRAY;
    }

    public static Color fromRGB(final float r, final float g, final float b) {
        return new Color(new Vector4f(r, g, b, 0.0f));
    }

    public static Color fromRGBA(final float r, final float g, final float b, final float a) {
        return new Color(new Vector4f(r, g, b, a));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private float r;
        private float g;
        private float b;
        private float a;

        public Builder rgb(final float r, final float g, final float b) {
            return rgba(r, g, b, 0f);
        }

        public Builder rgba(final float r, final float g, final float b, final float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }

        public Color build() {
            return new Color(this);
        }
    }
}
