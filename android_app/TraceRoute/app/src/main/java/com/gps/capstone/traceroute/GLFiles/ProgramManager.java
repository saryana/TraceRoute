package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLES20;

/**
 * Stores the master OpenGL shader and program code, and allows user to fetch
 * the compiled shaders and the openGL runtime environment
 */
public class ProgramManager {
    // Stores the openGL program
    private Integer mProgram;

    // Stores a shaderID for the compiled vertex shader code.
    private Integer vertexShader;
    // Stores a shaderID for the compiled fragment shader code.
    private Integer fragmentShader;

    // Vertex Shader
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Fragment Shader
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // Get the vertex shader
    public int getVertexShader() {
        if (vertexShader == null) {
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
        }
        return vertexShader;
    }

    // Get the fragment shader
    public int getFragmentShader() {
        if (fragmentShader == null) {
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
        }
        return fragmentShader;
    }

    // Compile and load the shader of the given type.
    private int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    // Get the openGL program
    public int getProgram() {
        if (mProgram == null) {
            // create empty OpenGL ES Program
            mProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mProgram, vertexShader);

            // add the fragment shader to program
            GLES20.glAttachShader(mProgram, fragmentShader);

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(mProgram);
        }
        return mProgram;
    }
}
