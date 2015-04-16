package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.ProgramManager;

/**
 * Defines an over-arching class
 * that all drawable objects should implement.
 */
public class DrawableObject {
    // Stores the graphics environment manager.
    protected ProgramManager mGraphicsEnv;
    protected int programHandle;

    // These are used for drawing objects.
    protected int mMVPMatrixHandle;
    protected int mPositionHandle;
    protected int mColorHandle;


    public DrawableObject(ProgramManager graphicsEnvironment) {
        mGraphicsEnv = graphicsEnvironment;
        // Compile the shaders and return the program handle.
        programHandle = graphicsEnvironment.getProgram();
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
    }

}
