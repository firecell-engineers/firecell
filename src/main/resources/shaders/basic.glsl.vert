#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;

uniform mat4 uProjection;
uniform mat4 uModel;
uniform mat4 uView;

out vec3 fNormal;

void main()
{
    fNormal = mat3(transpose(inverse(uModel))) * aNormal;

    gl_Position = uProjection * uView * aInstanceModel * vec4(aPosition, 1.0);
}