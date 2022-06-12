#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in mat4 aInstanceModel;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec3 fPosition;
out vec3 fNormal;

void main()
{
    fPosition = vec3(aInstanceModel * uModel * vec4(aPosition, 1.0));
    fNormal = mat3(transpose(inverse(uModel))) * aNormal;

    gl_Position = uProjection * uView * vec4(fPosition, 1.0);
}