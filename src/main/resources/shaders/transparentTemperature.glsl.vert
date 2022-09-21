#version 330 core

layout (location = 0) in vec3  aPosition;
layout (location = 1) in vec3  aNormal;
layout (location = 2) in vec3  aInstancePosition;
layout (location = 3) in float aInstanceTemperature;
layout (location = 4) in int   aInstanceMaterial;
layout (location = 5) in int   aInstanceBurningTime;

uniform mat4 uProjection;
uniform mat4 uView;

out vec3 fNormal;
out vec4 fColor;

const vec4 DEBUG_COLOR = vec4(1.0, 0.0, 1.0, 0.5);
const vec4 TRANSPARENT_COLOR = vec4(0.0);
const vec4 COLD_COLOR = vec4(0.5, 0.5, 1.0, 0.05);
const vec4 HOT_COLOR  = vec4(1.0, 0.5, 0.5, 0.05);

const float TEMP_MIN_TRESHOLD = 25.0;
const float TEMP_MAX_TRESHOLD = 300.0;

mat4 modelFromPosition(vec3 position) {
    mat4 model = mat4(1.0);
    model[3][0] = position.x;
    model[3][1] = position.y;
    model[3][2] = position.z;
    return model;
}

vec3 transformNormal(vec3 normal, mat4 model) {
    return mat3(transpose(inverse(model))) * aNormal;
}

vec4 transformPosition(vec3 position, mat4 mvp) {
    return mvp * vec4(aPosition, 1.0);
}

vec4 resolveColor(float temperature) {
    float tempInterpolant = (temperature - TEMP_MIN_TRESHOLD) /
                            (TEMP_MAX_TRESHOLD - TEMP_MIN_TRESHOLD);
    return mix(COLD_COLOR, HOT_COLOR, tempInterpolant);
}

void main()
{
    mat4 model = modelFromPosition(aInstancePosition);
    mat4 mvp   = uProjection * uView * model;

    fNormal = transformNormal(aNormal, model);
    fColor = resolveColor(aInstanceTemperature);
    gl_Position = transformPosition(aPosition, mvp);
}

