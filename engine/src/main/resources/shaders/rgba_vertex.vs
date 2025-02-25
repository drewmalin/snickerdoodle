#version 330

// The graphics pipeline looks something like:
//  1. Vertex List
//     This is the input, represented as Vertex Buffers (or, a data structure that packs the vertices needed to be
//     rendered).
//  2. Vertex Processing
//     In this step, the Vertex Buffers are processed such that each vertex's projected position is calculated. This
//     phase can also take into account color or texture, but its main goal is to calculate vertices.
//  3. Geometry Processing
//     In this step, the vertices are taken from the vertex shaderProgram and are connected into triangles (though more
//     custom transformations can be made).
//  4. Rasterization
//     In this step, the triangles from the geometry shaderProgram are taken and are transformed into pixel-sized fragments.
//  5. Fragment Processing
//     In this step, the fragments from the rasterization step are passed to the fragment shaderProgram to be assigned color
//     data, preparing each pixel for writing to the final frame buffer.
//
// All of the above is highly parallelizable.

// Inputs
layout (location =0) in vec3 position;
layout (location =1) in vec4 inColor;
layout (location =2) in vec3 vertexNormal;

// Outputs
out vec4 exColor;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;

uniform mat4 entityTransformation;
uniform mat4 frustumTransformation;

void main()
{
    // The final matrix to multiply any given vertex position by is represented by the following equation:
    //
    //   matrix =
    //     frustum_matrix *
    //     translation_matrix *
    //     rotation_matrix *
    //     scale_matrix
    //
    // with this, we can perform the matrix multiplication of:
    //
    //    |m0  m1  m2  m3 |   |position_x|   |new_position_x|
    //    |m4  m5  m6  m7 |   |position_y|   |new_position_y|
    //    |m8  m9  m10 m11| * |position_z| = |new_position_z|
    //    |m12 m13 m14 m15|   |    1     |   |      1       |
    //
    // Remember: the "gl_Position" value is built-in and will be used as this new position vector.
    vec4 mvPos = entityTransformation * vec4(position, 1.0);
	gl_Position = frustumTransformation * mvPos;

	exColor = inColor;
	mvVertexNormal = normalize(entityTransformation * vec4(vertexNormal, 0.0)).xyz;
	mvVertexPos = mvPos.xyz;
}