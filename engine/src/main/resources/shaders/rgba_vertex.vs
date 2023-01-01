#version 330

layout (location =0) in vec3 position;
layout (location =1) in vec4 inColor;
layout (location =2) in vec3 vertexNormal;

out vec4 exColor;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;

uniform mat4 entityTransformation;
uniform mat4 frustumTransformation;

void main()
{
    vec4 mvPos = entityTransformation * vec4(position, 1.0);
	gl_Position = frustumTransformation * mvPos;
	exColor = inColor;
	mvVertexNormal = normalize(entityTransformation * vec4(vertexNormal, 0.0)).xyz;
	mvVertexPos = mvPos.xyz;
}