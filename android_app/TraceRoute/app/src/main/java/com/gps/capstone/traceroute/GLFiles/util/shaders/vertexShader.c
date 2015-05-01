uniform mat4 uMVPMatrix;
uniform float uThickness;
attribute vec4 a_Position;
attribute vec4 a_Color;
varying vec4 v_Color;

void main() {
   v_Color = a_Color;
   gl_PointSize = uThickness;
   gl_Position = uMVPMatrix * a_Position;
}