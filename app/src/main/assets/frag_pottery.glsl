#version 100
precision mediump float;

uniform sampler2D uTexture;
uniform samplerCube uCube;

//light parameter
uniform vec4 uAmbientL;
uniform vec4 uDiffuseL;
uniform vec4 uSpecularL;
uniform vec4 uLightPosition;

//light2
uniform vec4 uAmbientL2;
uniform vec4 uDiffuseL2;
uniform vec4 uSpecularL2;
uniform vec4 uLightPosition2;

//material parameter
uniform vec4 uAmbientM;
uniform vec4 uDiffuseM;
uniform vec4 uSpecularM;
uniform float uShininess;

//look at point;
const vec4 uLookAt = vec4(0, 0, 0, 0);

//data from vertex shader
varying vec4 vPosition;
varying vec3 vNormal;    //法向座标
varying vec2 vTexCoord;  //纹理座标
void main(){
    //computer ambient;
    vec4 ambient = uAmbientM * uAmbientL;
    vec4 ambient2 = uAmbientM * uAmbientL2;

    //computer diffuse;
    float dis = length(uLightPosition - vPosition);
    vec4 lightVector = normalize(uLightPosition - vPosition);
    lightVector = normalize(vec4(-1,1,2,0));
    vec4 diffuse = uDiffuseL * uDiffuseM * max(dot(vec3(lightVector), vNormal), 0.0);

    vec4 lightVector2 = normalize(uLightPosition2 - vPosition);
    lightVector2 = vec4(1.2, 0, -1, 1);
    vec4 diffuse2 = uDiffuseL2 * uDiffuseM * max(dot(vec3(lightVector2), vNormal), 0.0);

    //computer specular
    vec4 s = normalize(normalize(uLookAt - vPosition) + lightVector);
    float specularFactor = pow(max(dot(vec3(s), vNormal), 0.0), uShininess);
    vec4 specular = uSpecularL * uSpecularM * specularFactor;

    vec4 s2 = normalize(normalize(uLookAt - vPosition) + lightVector2);
    float specularFactor2 = pow(max(dot(vec3(s2), vNormal), 0.0), uShininess);
    vec4 specular2 = uSpecularL2 * uSpecularM * specularFactor2;

    vec3 texCoordCube = reflect(normalize(uLookAt - vPosition).xyz, vNormal);
    //computer final color
    gl_FragColor =
        texture2D(uTexture, vTexCoord) 
    	* (diffuse + ambient + diffuse2 + ambient2 )
    	+ specular + specular2
        + textureCube(uCube,texCoordCube) * 0.01;
}
