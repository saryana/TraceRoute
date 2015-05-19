package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.nio.FloatBuffer;

/**
 * A test object to see if the draw order is screwed up or not.
 */
@Deprecated
public class TriangularPrism extends BasicLightingObject {

    private static final float[] coords = {
        // Front face
        0.0f, 0.3f, 0.0f,
        -0.3f, -0.3f, 0.3f,
        0.3f, -0.3f, 0.3f,

        // Right face
        0.0f, 0.3f, 0.0f,
        0.3f, -0.3f, 0.3f,
        0.0f, -0.3f, -0.3f,

        // left face
        0.0f, 0.3f, 0.0f,
        0.0f, -0.3f, -0.3f,
        -0.3f, -0.3f, 0.3f,

        // bottom face
        0.3f, -0.3f, 0.3f,
       -0.3f, -0.3f, 0.3f,
       0.0f, -0.3f, -0.3f,
    };

    private static final float[] colors = {
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,

        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,

        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,

        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f
    };

    public TriangularPrism() {
         setVerticies(coords);
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);


        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, vertexData);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Colors?!
        FloatBuffer compatibleColors = convertFloatArray(colors);
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4, GLES20.GL_FLOAT, false,
                16, compatibleColors);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw Cube
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12);

        // Disable vertex attribute arrays.
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }

}
