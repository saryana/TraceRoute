package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.nio.FloatBuffer;

/**
 * Created by keith619 on 4/9/15.
 */
public class Axis extends BasicLightingObject {


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float axisLineCoords[] = {   // in counterclockwise order:
            0.0f,  0.0f, -10.0f, // Z-axis negative
            0.0f, 0.0f, 10.0f // Z-axis positive
    };

    // Defines some vertex colors.
    private static float[] vertexColors = {
            190/255f, 190/255f, 190/255f, 1.0f,
            190/255f, 190/255f, 190/255f, 1.0f
    };

    //private final int vertexCount = axisLineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private static final int COLOR_STRIDE = 16;



    private FloatBuffer bufferColors = convertFloatArray(vertexColors);


    public Axis() {
        setVerticies(axisLineCoords);

    }

    /**
     * Draw the axis.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, getVertexData());

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Set color for drawing the axis
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4,
                GLES20.GL_FLOAT, false,
                COLOR_STRIDE, bufferColors);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the axis
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);

        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }
}
