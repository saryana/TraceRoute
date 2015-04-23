package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.DrawableObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by keith619 on 4/9/15.
 */
public class Axis extends DrawableObject {


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float axisLineCoords[] = {   // in counterclockwise order:
            -10.0f,  0.0f, 0.0f, // X-axis negative
            10.0f, 0.0f, 0.0f, // X-axis positive
            0.0f,  -10.0f, 0.0f, // Y-axis negative
            0.0f, 10.0f, 0.0f, // Y-axis positive
            0.0f,  0.0f, -10.0f, // Z-axis negative
            0.0f, 0.0f, 10.0f // Z-axis positive
    };

    //private final int vertexCount = axisLineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.0f, 0.0f, 1f, 1.0f };

    public Axis(ProgramManager graphicsEnv) {
        super(graphicsEnv);
        setVerticies(axisLineCoords);

    }

    /**
     * Draw the axis.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, getVertexData());

        // Set color for drawing the axis
        GLES20.glUniform4fv(mVertexColorHandle, 1, color, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the axis
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2);
        GLES20.glDrawArrays(GLES20.GL_LINES, 4, 2);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
    }
}
