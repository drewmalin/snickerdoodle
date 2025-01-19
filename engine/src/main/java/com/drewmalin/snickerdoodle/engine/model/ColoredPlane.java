package com.drewmalin.snickerdoodle.engine.model;

import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;

public class ColoredPlane
    implements Model {

    private static final Mesh MESH = new ModelMesh();
    private final Color color;

    public ColoredPlane(final Color color) {
        this.color = color;
    }

    @Override
    public Mesh getMesh() {
        return MESH;
    }

    @Override
    public Material getMaterial() {
        return this.color;
    }

    private static class ModelMesh
        implements Mesh {

        @Override
        public float[] getVertices() {
            return new float[]{
                -0.5f, 0.5f, 0f,    // V0
                -0.5f, -0.5f, 0f,   // V1
                0.5f, -0.5f, 0f,    // V2
                0.5f, 0.5f, 0f,     // V3
            };
        }

        @Override
        public int[] getVertexRenderOrder() {
            return new int[]{
                0, 1, 3,
                3, 1, 2,
            };
        }

        @Override
        public float[] getVertexNormals() {
            return new float[]{
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
            };
        }
    }
}
