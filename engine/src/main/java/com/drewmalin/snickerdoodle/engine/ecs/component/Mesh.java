package com.drewmalin.snickerdoodle.engine.ecs.component;

public interface Mesh extends Component {

    /**
     * Returns an array of floats corresponding to the x, y, and z
     * positions of each point of the mesh. Starting at index 0, every triplet of
     * floats is considered to be the (x, y, z) value for a single point.
     * <p>
     * Note: the order of the triplets matters, as the Mesh::getVertexRenderOrder
     * method will index into this array.
     */
    float[] getVertices();

    /**
     * Returns an array of index values representing the order in which the
     * values returned from Mesh::getVertices should be rendered. Starting at index
     * 0, every triplet of indices will represent a triangle.
     * <p>
     * Example:
     * <p>
     * getVertices() == new float[]{
     * 0, 0, 0,
     * 1, 1, 1,
     * 2, 2, 2,
     * 3, 3, 3,
     * };
     * <p>
     * getVertexRenderOrder() == new float[]{
     * 0, 1, 2, 2, 1, 3
     * };
     * <p>
     * In the above, two triangles will be created:
     * - ((0, 0, 0), (1, 1, 1), (2, 2, 2))
     * - ((2, 2, 2), (1, 1, 1), (3, 3, 3))
     */
    int[] getVertexRenderOrder();

    float[] getVertexNormals();
}
