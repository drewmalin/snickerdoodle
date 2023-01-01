#version 330

in vec4 exColor;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PositionalLight
{
    vec3 color;
    vec3 position;
    float intensity;
    Attenuation att;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PositionalLight positionalLight;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material) {
    ambientC = material.ambient;
    diffuseC = material.diffuse;
    specularC = material.specular;
}

vec4 calcPositionalLight(PositionalLight light, vec3 position, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specularColor = vec4(0, 0, 0, 0);

    vec3 lightDirection = light.position - position;
    vec3 normalizedLight = normalize(lightDirection);
    float diffuseFactor = max(dot(normal, normalizedLight), 0.0);
    diffuseColor = diffuseC * vec4(light.color, 1.0) * light.intensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 reflectedLight = normalize(reflect(-normalizedLight, normal));
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specularColor = specularC * specularFactor * material.reflectance * vec4(light.color, 1.0);

    float distance = length(lightDirection);
    float attenuation = light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance;

    return (diffuseColor + specularColor) / attenuation;
}

void main()
{
    setupColors(material);
    vec4 lightColor = calcPositionalLight(positionalLight, mvVertexPos, mvVertexNormal);
// 	fragColor = exColor;
    fragColor = ambientC * vec4(ambientLight, 1) + lightColor;
}