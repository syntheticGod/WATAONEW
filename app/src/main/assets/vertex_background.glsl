//#version 200
uniform mat4 uMVPMatrix; //总变换矩阵

attribute vec3 aPosition;   //顶点位置
attribute vec2 aTexCoord;   //顶点纹理座标

varying vec2 vTexCoord;
void main(){
    vec4 position = vec4(aPosition, 1);
    //根据总变换矩阵计算此次绘制此顶点位置
    gl_Position = uMVPMatrix * position;
    vTexCoord = aTexCoord;
}
