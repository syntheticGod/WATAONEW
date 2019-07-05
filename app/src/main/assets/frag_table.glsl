#version 100
precision mediump float;

uniform sampler2D uTexture;

//light parameter
uniform vec4 uAmbientL;
uniform vec4 uDiffuseL;
uniform vec4 uLightPosition;

//material parameter
uniform vec4 uAmbientM;
uniform vec4 uDiffuseM;
uniform float uShininess;

//data from vertex shader
varying vec4 vPosition;
varying vec3 vNormal;    //法向座标
varying vec2 vTexCoord;  //纹理座标

void main(){
    //computer ambient;
    vec4 ambient = uAmbientM * uAmbientL;

    //computer diffuse;
    float dis = length(uLightPosition - vPosition);
    vec4 lightVector = normalize(uLightPosition - vPosition);
    vec4 diffuse = uDiffuseL * uDiffuseM * max(dot(vec3(lightVector), vNormal), 0.0);

    //computer final color
    gl_FragColor = texture2D(uTexture, vTexCoord)*(diffuse + ambient);
}
