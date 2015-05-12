package com.gps.capstone.traceroute.GLFiles.util;

import android.content.Context;
import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.DiffuseLightingObject;
import com.gps.capstone.traceroute.R;

/**
 * Stores the master OpenGL shader and program code, and allows user to fetch
 * the compiled shaders and the openGL runtime environment. The only public functionality
 * is getting the full shading programs.
 */
public class ProgramManager {

    // Each program is clustered together with its shaders.

    // RAW COLOR
    // Stores the openGL program
    private Integer mRawColorProgram;
    // Stores the shaders for
    private Integer vertexShader;
    private Integer fragmentShader;
    private String vertexShaderCode;
    private String fragmentShaderCode;

    // DIFFUSE SHADING PROGRAM
    private Integer mDiffuseProgram;
    // shaders for the diffuse shading program
    private Integer diffuseVertexShader;
    private Integer diffuseFragmentShader;
    private String diffuseVertexShaderCode;
    private String diffuseFragmentShaderCode;

    // POINT LIGHT SHADING PROGRAM
    private Integer mLightProgram;
    // shaders for the light program.
    private Integer lightVertexShader;
    private Integer lightFragmentShader;
    private String lightVertexShaderCode;
    private String lightFragmentShaderCode;

    /**
     * Initializes a program manager.
     */
    public ProgramManager(Context context) {
        vertexShaderCode = context.getString(R.string.vertex_shader);
        fragmentShaderCode = context.getString(R.string.fragment_shader);

        diffuseVertexShaderCode = context.getString(R.string.vertex_shader_diffuse);
        diffuseFragmentShaderCode = context.getString(R.string.fragment_shader);

        lightVertexShaderCode = context.getString(R.string.light_vertex_shader);
        lightFragmentShaderCode = context.getString(R.string.light_fragment_shader);
    }

    // VERTEX SHADER LOADERS

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
     * @return A handle for the vertex shader used in the openGL program.
     */
    public int getDiffuseVertexShader() {
        if (diffuseVertexShader == null) {
            diffuseVertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    diffuseVertexShaderCode);
        }
        return diffuseVertexShader;
    }

    /**
     *
     * @return A handle for the vertex shader used in the openGL program.
     */
    public int getLightVertexShader() {
        if (lightVertexShader == null) {
            lightVertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    lightVertexShaderCode);
        }
        return lightVertexShader;
    }



    // FRAGMENT SHADER LOADERS

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

    /**
     *
     * @return A handle for the fragment shader used in the openGL program.
     */
    public int getDiffuseFragmentShader() {
        if (diffuseFragmentShader == null) {
            diffuseFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    diffuseFragmentShaderCode);
        }
        return diffuseFragmentShader;
    }

    /**
     *
     * @return A handle for the fragment shader used in the openGL program.
     */
    public int getLightFragmentShader() {
        if (lightFragmentShader == null) {
            lightFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    lightFragmentShaderCode);
        }
        return lightFragmentShader;
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

    // PROGRAM GETTERS

    /**
     *
     * @return A handle to the 'raw color' openGL program.
     */
    public int getRawColorProgram() {
        if (mRawColorProgram == null) {
            // create empty OpenGL ES Program
            mRawColorProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mRawColorProgram, getVertexShader());

            // add the fragment shader to program
            GLES20.glAttachShader(mRawColorProgram, getFragmentShader());

            // Bind attributes
            GLES20.glBindAttribLocation(mRawColorProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mRawColorProgram, 1, "a_Color");

            // Links the shaders together.
            GLES20.glLinkProgram(mRawColorProgram);
        }
        return mRawColorProgram;
    }

    /**
     *
     * @return A handle to the 'raw color' openGL program.
     */
    public int getDiffuseProgram() {
        if (mDiffuseProgram == null) {
            // create empty OpenGL ES Program
            mDiffuseProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mDiffuseProgram, getVertexShader());

            // add the fragment shader to program
            GLES20.glAttachShader(mDiffuseProgram, getFragmentShader());

            // Bind attributes
            GLES20.glBindAttribLocation(mDiffuseProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mDiffuseProgram, 1, "a_Color");
            GLES20.glBindAttribLocation(mDiffuseProgram, 2, "a_Normal");

            // Links the shaders together.
            GLES20.glLinkProgram(mDiffuseProgram);
        }
        return mDiffuseProgram;
    }

    /**
     *
     * @return A handle to the 'raw color' openGL program.
     */
    public int getLightProgram() {
        if (mLightProgram == null) {
            // create empty OpenGL ES Program
            mLightProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mLightProgram, getVertexShader());

            // add the fragment shader to program
            GLES20.glAttachShader(mLightProgram, getFragmentShader());

            // Bind attributes
            GLES20.glBindAttribLocation(mLightProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mLightProgram, 1, "a_Color");

            // Links the shaders together.
            GLES20.glLinkProgram(mLightProgram);
        }
        return mLightProgram;
    }
}
