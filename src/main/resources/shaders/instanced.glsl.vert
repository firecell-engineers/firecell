#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in mat4 aInstanceModel;

uniform mat4 uProjection;
uniform mat4 uView;

out vec3 fNormal;

void main()
{
    fNormal = mat3(transpose(inverse(aInstanceModel))) * aNormal;

    gl_Position = uProjection * uView * aInstanceModel * vec4(aPosition, 1.0);
}