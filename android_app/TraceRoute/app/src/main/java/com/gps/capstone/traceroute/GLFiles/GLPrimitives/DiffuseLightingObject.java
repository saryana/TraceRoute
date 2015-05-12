package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.nio.FloatBuffer;

/**
 * Created by Miaku_000 on 5/11/2015.
 */
public abstract class DiffuseLightingObject extends DrawableObject {
    // The position of the imaginary light for all objects.
    // We need this to be 4 dimensions so we can manipulate it
    // with the matrix functions.
    protected float[] lightPos = {0f, 0f, 2f, 1f};
    // Stores the vertex normal data that's passed to openGL.
    protected FloatBuffer normalData;

    // the diffuse lighting program handle.
    protected static int programHandle;

    // Various handles for the raw color program.
    protected static int mMVPMatrixHandle;
    protected static int mMVMatrixHandle;
    protected static int mVertexColorHandle;
    protected static int mVertexPositionHandle;
    protected static int mFragmentColorHandle;
    protected static int mVertexNormalHandle;
    protected static int mPointLightPos;
    protected static int mPointSizeHandle;

    public static void SetOpenGLEnvironment(ProgramManager graphicsEnv) {
        programHandle = graphicsEnv.getDiffuseProgram();
        fetchHandles();
    }

    /*
     * Fetch the handles for diffuse lighting.
     */
    private static void fetchHandles() {
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVMatrix");
        mFragmentColorHandle = GLES20.glGetUniformLocation(programHandle, "v_Color");
        mPointLightPos = GLES20.glGetUniformLocation(programHandle, "u_LightPos");
        mPointSizeHandle = GLES20.glGetUniformLocation(programHandle, "uThickness");

        mVertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mVertexColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mVertexNormalHandle = GLES20.glGetAttribLocation(programHandle, "a_Normal");
    }

    /**
     * Sets the normal data to be
     * @param normals
     */
    public void setNormals(float[] normals) {
        normalData = convertFloatArray(normals);
    }
}
