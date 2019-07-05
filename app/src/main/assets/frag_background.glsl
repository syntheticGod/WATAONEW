#version 100
precision mediump float;

uniform sampler2D uTexture;

uniform float factor;

varying vec2 vTexCoord;  //纹理座标


void main(){
    gl_FragColor = texture2D(uTexture, vTexCoord) * factor;
}
