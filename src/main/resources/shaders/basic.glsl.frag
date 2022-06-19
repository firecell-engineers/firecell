#version 330 core

in vec3 fNormal;

uniform vec3 uObjectColor;
uniform vec3 uLightDir;
uniform vec3 uLightColor;

out vec4 FragColor;

void main()
{
    // ambient
    vec3 ambient = uLightColor * 0.3;

    // diffuse
    float diffuseCoeff = max(dot(normalize(fNormal), normalize(-uLightDir)), 0.0);
    vec3 diffuse = diffuseCoeff * uLightColor;

    // result
    vec3 resultColor = (ambient + diffuse) * uObjectColor;
    FragColor = vec4(resultColor, 1.0);
}