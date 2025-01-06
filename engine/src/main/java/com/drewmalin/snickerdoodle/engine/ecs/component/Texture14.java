package com.drewmalin.snickerdoodle.engine.ecs.component;

import org.joml.Vector4f;

/**
 * A texture meant for use by 14-point meshes (see: {@link Cube14}).
 */
public class Texture14
    extends Material {

    private static final Vector4f OPAQUE_WHITE = new Vector4f(1f, 1f, 1f, 1f);

    private final String filepath;

    public Texture14(final String filepath) {
        super(OPAQUE_WHITE, OPAQUE_WHITE, OPAQUE_WHITE, 0f);

        this.filepath = filepath;
    }

    public float[] getCoordinates() {
        return new float[]{
            0.50f, 0.50f, // V0
            0.50f, 0.75f, // V1
            0.75f, 0.75f, // V2
            0.75f, 0.50f, // V3
            0.50f, 0.25f, // V4
            0.75f, 0.25f, // V5
            0.25f, 0.75f, // V6
            1.00f, 0.75f, // V7
            1.00f, 0.50f, // V8 - V5'
            0.25f, 0.50f, // V9 - V4'
            0.50f, 1.00f, // V10- V6'
            0.75f, 1.00f, // V11- V7'
            0.00f, 0.75f, // V12- V7''
            0.00f, 0.50f, // V13- V5''
        };
    }

    public String getFilePath() {
        return this.filepath;
    }
}
