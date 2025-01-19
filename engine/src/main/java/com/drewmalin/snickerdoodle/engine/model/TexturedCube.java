package com.drewmalin.snickerdoodle.engine.model;

import com.drewmalin.snickerdoodle.engine.ecs.component.Material;
import com.drewmalin.snickerdoodle.engine.ecs.component.Mesh;
import com.drewmalin.snickerdoodle.engine.ecs.component.Texture;
import org.joml.Vector4f;

/**
 * A cube that can be adorned with a texture file. Note that texture files should be square, with a height and width
 * values each equalling a power of two (e.g. 1024x1024).
 *
 * See {@link /resources/textures/debug.png} for an example of a correct texture.
 */
public class TexturedCube
    implements Model {

    private static final Mesh MESH = new ModelMesh();
    private final Material texture;

    public TexturedCube(final String textureFilePath) {
        this.texture = new ModelTexture(textureFilePath);
    }

    @Override
    public Mesh getMesh() {
        return MESH;
    }

    @Override
    public Material getMaterial() {
        return this.texture;
    }

    /**
     * A cube composed of 14 vertices (see {@link ColoredCube} for a simpler cube).
     *
     * Why 14?
     *
     * The use of a VAO to render textures requires packing vertex data like so:
     * |           Vertex 0         |           Vertex 1          | ...
     * | x  y  z  r  g  b  a  s  t  |  x  y  z  r  g  b  a  s  t  | ...
     * | 0  4  8 12 16 20 24 28 32  | ...
     *
     * This means that each logical vertex consists of:
     * - a position (x, y, z)
     * - a color    (r, g, b, a)
     * - a texture coordinate (s, t)
     *
     * When drawing these vertices, we specify the render order by *index*, so rather than creating a triangle like:
     * 1: (0, 0)
     * 2: (0, 1)
     * 3: (1, 0)
     * instead we load each vertex position into the VAO:
     * | 0 0 | 1 0 | 0 1 |
     * then draw based on the position in the buffer:
     * 1: 0
     * 2: 2
     * 3: 1
     *
     * When textures are introduced, their position (which is the x,y position in the texture image) is stored per vertex:
     * | 0 0 0.5 0.5 | 1 0 1 0.5 | 0 1 .75 1 |
     *
     * This works well, but consider a cube which will re-use vertices when building the triangles of individual faces.
     * If a re-used vertex suddenly requires an entirely different location on the texture image, we have no way of
     * representing that the same exactly item in the VBO requires two separate texture coordinates.
     *
     * The trick below is to introduce additional entries for those vertices which appear more than once in the render
     * sequence but require different texture coordinates. Rather than re-use these entries, a "duplicate" entry is made
     * with the same position values but different texture coordinates.
     *
     * Below, the "prime" (e.g.: V5') comments signify duplicated positions (e.g. 8 - V5' means "this is technically vertex
     * 8, but it duplicates vertex 5).
     *
     * See {@link ColoredCube} for a simpler cube that does not attempt to make it easy to pair with a texture (so, lots of
     * vertices are re-used).
     */
    private static class ModelMesh
        implements Mesh {

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

                VERTEX_LENGTH, VERTEX_LENGTH, -VERTEX_LENGTH,      // 8 - V5'
                -VERTEX_LENGTH, VERTEX_LENGTH, -VERTEX_LENGTH,     // 9 - V4'

                -VERTEX_LENGTH, -VERTEX_LENGTH, -VERTEX_LENGTH,    // 10 - V6'
                VERTEX_LENGTH, -VERTEX_LENGTH, -VERTEX_LENGTH,     // 11 - V7'

                VERTEX_LENGTH, -VERTEX_LENGTH, -VERTEX_LENGTH,     // 12 - V7''
                VERTEX_LENGTH, VERTEX_LENGTH, -VERTEX_LENGTH,      // 13 - V5''
            };
        }

        @Override
        public int[] getVertexRenderOrder() {
            return new int[]{
                0, 1, 3, 3, 1, 2, // front
                4, 0, 3, 5, 4, 3, // top
                3, 2, 7, 8, 3, 7, // right
                6, 1, 0, 6, 0, 9, // left
                2, 1, 10, 2, 10, 11, // bottom
                12, 6, 9, 12, 9, 13, // back
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

                NORMAL_LENGTH, NORMAL_LENGTH, -NORMAL_LENGTH,    // 8 - V5'
                -NORMAL_LENGTH, NORMAL_LENGTH, -NORMAL_LENGTH,   // 9 - V4'

                -NORMAL_LENGTH, -NORMAL_LENGTH, -NORMAL_LENGTH,  // 10 - V6'
                NORMAL_LENGTH, -NORMAL_LENGTH, -NORMAL_LENGTH,   // 11 - V7'

                NORMAL_LENGTH, -NORMAL_LENGTH, -NORMAL_LENGTH,   // 12 - V7''
                NORMAL_LENGTH, NORMAL_LENGTH, -NORMAL_LENGTH,    // 13 - V5''
            };
        }
    }

    /**
     * A purpose-built texture meant for use with {@link ModelMesh}. Note the use of the additional texture
     * coordinates which correspond to the additional triangles used in the mesh.
     *
     * @param filepath
     */
    private record ModelTexture(String filepath)
        implements Texture {

        private static final Vector4f OPAQUE_WHITE = new Vector4f(1f, 1f, 1f, 1f);

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

        @Override
        public String getFilePath() {
            return this.filepath;
        }

        @Override
        public Vector4f getAmbient() {
            return OPAQUE_WHITE;
        }

        @Override
        public Vector4f getDiffuse() {
            return OPAQUE_WHITE;
        }

        @Override
        public Vector4f getSpecular() {
            return OPAQUE_WHITE;
        }

        @Override
        public float getReflectance() {
            return 0;
        }
    }
}
