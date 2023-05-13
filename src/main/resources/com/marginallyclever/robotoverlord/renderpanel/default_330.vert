#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec4 fragmentColor;
out vec3 normalVector;
out vec3 fragmentPosition;
out vec2 textureCoord;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(aPosition, 1.0);

    fragmentColor = aColor;
    fragmentPosition = vec3(modelMatrix * vec4(aPosition, 1.0));
    normalVector = mat3(transpose(inverse(modelMatrix))) * aNormal;
    textureCoord = aTexture;
}
