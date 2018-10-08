//#version 300 es
attribute vec2 vPosition;
attribute vec2 vCoordinate;
varying vec2 aCoordinate;
void main(){
    gl_Position = vec4(vPosition, 0, 1); // 这里的0表示z轴坐标, 1 是为了矩阵计算而定的固定值
    aCoordinate=vCoordinate;
}