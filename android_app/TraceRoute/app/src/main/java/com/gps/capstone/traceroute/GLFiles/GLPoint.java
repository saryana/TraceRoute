package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.DrawableObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by keith619 on 4/19/15.
 *
 * This represents and draws a point in 3-d space.
 */
public class GLPoint extends DrawableObject {


    // The coordinate of the point in 3-d space
    private float[] mVertex = new float[3];

    // The color of the point
    private static float color[] = { 0.5f, 0.0f, 0.5f, 1.0f };

    public GLPoint(ProgramManager graphicsEnv, float x, float y, float z) {
        super(graphicsEnv, new float[3]);
        mVertex[0] = x;
        mVertex[1] = y;
        mVertex[2] = z;

        // Currently extending DrawableObject requires us to initialize the vertex values again because
        // the points take the coordinates as parameters

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                mVertex.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexData = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexData.put(mVertex);
        // set the buffer to read the first coordinate
        vertexData.position(0);
    }

    // Getters for the coordinate values
    public float getX() {
        return mVertex[0];
    }

    public float getY() {
        return mVertex[1];
    }

    public float getZ() {
        return mVertex[2];
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

        // Set color for drawing the axis
        GLES20.glUniform4fv(mVertexColorHandle, 1, color, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniform1f(mPointSizeHandle, 20.0f);

        // Draw point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
    }
}