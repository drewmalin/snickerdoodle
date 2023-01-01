package com.drewmalin.snickerdoodle.engine.ecs.component;

public class Cube implements Mesh {

    private static final float VERTEX_LENGTH = 0.5f;
    private static final float NORMAL_LENGTH = 0.5774f;

    @Override
    public float[] getVertices() {
        return new float[]{
                -VERTEX_LENGTH, VERTEX_LENGTH, VERTEX_LENGTH,      // VO
                -VERTEX_LENGTH, -VERTEX_LENGTH, VERTEX_LENGTH,     // V1
                VERTEX_LENGTH, -VERTEX_LENGTH, VERTEX_LENGTH,      // V2
                VERTEX_LENGTH, VERTEX_LENGTH, VERTEX_LENGTH,       // V3
                -VERTEX_LENGTH, VERTEX_LENGTH, -VERTEX_LENGTH,     // V4
                VERTEX_LENGTH, VERTEX_LENGTH, -VERTEX_LENGTH,      // V5
                -VERTEX_LENGTH, -VERTEX_LENGTH, -VERTEX_LENGTH,    // V6
                VERTEX_LENGTH, -VERTEX_LENGTH, -VERTEX_LENGTH,     // V7
        };
    }

    @Override
    public int[] getVertexRenderOrder() {
        return new int[]{
                0, 1, 3, 3, 1, 2, // front
                4, 0, 3, 5, 4, 3, // top
                3, 2, 7, 5, 3, 7, // right
                6, 1, 0, 6, 0, 4, // left
                2, 1, 6, 2, 6, 7, // bottom
                7, 6, 4, 7, 4, 5, // back
        };
    }

    @Override
    public float[] getVertexNormals() {
        return new float[]{
                -NORMAL_LENGTH, NORMAL_LENGTH, NORMAL_LENGTH,    // V0
                -NORMAL_LENGTH, -NORMAL_LENGTH, NORMAL_LENGTH,   // V1
                NORMAL_LENGTH, -NORMAL_LENGTH, NORMAL_LENGTH,    // V2
                NORMAL_LENGTH, NORMAL_LENGTH, NORMAL_LENGTH,     // V3
                -NORMAL_LENGTH, NORMAL_LENGTH, -NORMAL_LENGTH,   // V4
                NORMAL_LENGTH, NORMAL_LENGTH, -NORMAL_LENGTH,    // V5
                -NORMAL_LENGTH, -NORMAL_LENGTH, -NORMAL_LENGTH,  // V6
                NORMAL_LENGTH, -NORMAL_LENGTH, -NORMAL_LENGTH,   // V7
        };
    }
}
