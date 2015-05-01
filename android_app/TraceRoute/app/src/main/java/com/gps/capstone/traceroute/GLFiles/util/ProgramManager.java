package com.gps.capstone.traceroute.GLFiles.util;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;

import com.gps.capstone.traceroute.R;

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
    private String vertexShaderCode;

    // Fragment Shader
    private String fragmentShaderCode;

    /**
     * Initializes a program manager.
     */
    public ProgramManager(Context context) {
        vertexShaderCode = context.getString(R.string.vertex_shader);
        fragmentShaderCode = context.getString(R.string.fragment_shader);
    }

    /**
     *
     * @return A handle for the vertex shader used in the openGL program.
     */
    public int getVertexShader() {
        if (vertexShader == null) {
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
        }
        return vertexShader;
    }

    /**
     *
     * @return A handle for the fragment shader used in the openGL program.
     */
    public int getFragmentShader() {
        if (fragmentShader == null) {
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
        }
        return fragmentShader;
    }

    // Compiles the shader (stored in shaderCode) of the given type.
    private int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     *
     * @return A handle to the openGL program.
     */
    public int getProgram() {
        if (mProgram == null) {
            // create empty OpenGL ES Program
            mProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mProgram, getVertexShader());

            // add the fragment shader to program
            GLES20.glAttachShader(mProgram, getFragmentShader());

            // Bind attributes
            GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mProgram, 1, "a_Color");

            // Links the shaders together.
            GLES20.glLinkProgram(mProgram);
        }
        return mProgram;
    }
}
