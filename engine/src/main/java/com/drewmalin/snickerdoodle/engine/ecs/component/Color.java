package com.drewmalin.snickerdoodle.engine.ecs.component;

import com.drewmalin.snickerdoodle.engine.utils.Vectors;
import org.joml.Vector4f;

public class Color
    extends Material {

    private Color(final Vector4f rgba) {
        super(
            rgba,
            rgba,
            rgba,
            0
        );
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
