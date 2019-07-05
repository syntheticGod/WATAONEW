//#version 200
uniform mat4 uMVPMatrix; //总变换矩阵
uniform mat4 uMVMatrix;  //视图模型矩阵

attribute vec3 aPosition;   //顶点位置
attribute vec3 aNormal;     //顶点法向
attribute vec2 aTexCoord;   //顶点纹理座标

varying vec4 vPosition;
varying vec3 vNormal;
varying vec2 vTexCoord;
void main(){
    vec4 position = vec4(aPosition, 1);

    //根据总变换矩阵计算此次绘制此顶点位置
    gl_Position = uMVPMatrix * position;

    //计算顶点在世界座标中的位置
    vPosition = uMVMatrix * position;
    vNormal = vec3(uMVMatrix * vec4(aNormal,0));
    vTexCoord = aTexCoord;
}
