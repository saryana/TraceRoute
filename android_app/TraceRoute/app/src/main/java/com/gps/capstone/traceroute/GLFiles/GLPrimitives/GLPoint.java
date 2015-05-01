package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

/**
 * Created by keith619 on 4/19/15.
 *
 * This represents and draws a point in 3-d space.
 */
@Deprecated
public class GLPoint extends DrawableObject {

    float[] mPointCoords = new float[3];


    // The color of the point
    private static float color[] = { 0.5f, 0.0f, 0.5f, 1.0f };

    public GLPoint(ProgramManager graphicsEnv, float x, float y, float z) {
        super(graphicsEnv);
        // The coordinate of the point in 3-d space

        mPointCoords[0] = x;
        mPointCoords[1] = y;
        mPointCoords[2] = z;
        setVerticies(mPointCoords);
    }

    // Getters for the coordinate values
    public float getX() {
        return mPointCoords[0];
    }

    public float getY() {
        return mPointCoords[1];
    }

    public float getZ() {
        return mPointCoords[2];
    }

    /**
     * Draw the point.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, getVertexData());

        // Prepare the color for the point.
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4, GLES20.GL_FLOAT, false,
                16, convertFloatArray(color));

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniform1f(mPointSizeHandle, 20.0f);

        // Draw point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }
}