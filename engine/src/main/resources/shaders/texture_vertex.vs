#version 330

layout (location =0) in vec3 position;
layout (location =1) in vec2 texCoord;

out vec2 outTexCoord;

uniform mat4 entityTransformation;
uniform mat4 frustumTransformation;

void main()
{
	gl_Position = frustumTransformation * entityTransformation * vec4(position, 1.0);
	outTexCoord = texCoord;
}