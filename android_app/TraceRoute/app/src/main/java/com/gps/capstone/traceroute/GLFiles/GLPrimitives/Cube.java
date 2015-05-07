package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by keith619 on 4/14/15.
 */
public class Cube extends DrawableObject {


    private final ShortBuffer drawListBuffer;

    //private int mPositionHandle;
    private int mColorHandle;


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static final int COLOR_SIZE = 4;

    static float cubeCoords[] = {   // in counterclockwise order:
            -0.3f,  0.3f, 0.3f,  // Front top left
            -0.3f, -0.3f, 0.3f,  // Front bottom left
            0.3f,  0.3f, 0.3f,   // Front top right
            0.3f, -0.3f, 0.3f,   // Front bottom right
            -0.3f,  0.3f, -0.3f, // Back top left
            -0.3f, -0.3f, -0.3f, // Back bottom left
            0.3f, 0.3f, -0.3f, // Back top right
            0.3f, -0.3f, -0.3f  // Back bottom right
    };

    static float cubeColors[] = {
            // R G B A
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    private final short drawOrder[] = {0,1,3, 3,2,0, 2,3,7, 7,6,2, 4,0,2, 2,6,4, 4,5,1, 1,0,4, 7,3,1, 1,5,7, 6,7,5, 5,4,6}; // order to draw vertices

    //private final int vertexCount = axisLineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final int colorStride = COLOR_SIZE * 4;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.0f, 0.6f, 0.6f, 1f };

    public Cube() {
        setVerticies(cubeCoords);
        // convert drawOrder into an openGL-compatible format.
        drawListBuffer = convertShortArray(drawOrder);
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);


        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexData);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);


        // Set color for drawing the axis
        //GLES20.glUniform4fv(mFragmentColorHandle, 1, color, 0);

        // Colors?!
        FloatBuffer compatibleColors = convertFloatArray(cubeColors);
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4, GLES20.GL_FLOAT, false,
                colorStride, compatibleColors);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);


        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw Cube
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex attribute arrays.
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }

}
