#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;
uniform float outlineSize;

void main() {
    vec3 adj = position + (normal * outlineSize);
    gl_Position = projectionMatrix * modelViewMatrix * vec4(adj, 1.0);
}
