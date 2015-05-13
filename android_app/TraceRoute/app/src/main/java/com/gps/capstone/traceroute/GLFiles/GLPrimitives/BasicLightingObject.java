package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

/**
 * All objects that want only basic colors should extend this.
 */
public abstract class BasicLightingObject extends DrawableObject {

    // the handle for the raw color program
    protected static int programHandle;
    // Various handles for the raw color program.
    protected static int mMVPMatrixHandle;
    protected static int mVertexColorHandle;
    protected static int mVertexPositionHandle;
    protected static int mFragmentColorHandle;
    protected static int mPointSizeHandle;


    public static void SetOpenGLEnvironment(ProgramManager graphicsEnv) {
        programHandle = graphicsEnv.getRawColorProgram();
        fetchHandles();

    }

    public static void fetchHandles() {
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        mVertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mVertexColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mFragmentColorHandle = GLES20.glGetUniformLocation(programHandle, "v_Color");
        mPointSizeHandle = GLES20.glGetUniformLocation(programHandle, "uThickness");
    }
}
