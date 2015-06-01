package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.R;

import java.nio.FloatBuffer;

/**
 * Defines a 2-d grid of lines
 */
public class TronGrid extends BasicLightingObject {

    // The size of the grid. This needs to be an even number.
    private static int GRID_SIZE = 100;

    // The sidelength of each grid element.
    private static int STEP_FACTOR = 1;

    //private final int vertexCount = axisLineCoords.length / COORDS_PER_VERTEX;
    private final int VERTEX_STRIDE = DrawableObject.DIMENSIONS * 4; // 4 bytes per vertex
    private static final int COLOR_STRIDE = 16;

    // The lines goiong parallel to the the x-axis
    // each sub-array is a pair of 3d points that make up a line segment.
    private static float[][] xLines;

    // The lines going parallel to the y-axis.
    // each sub-array is a pair of 3d points that make up a line segment.
    private static float[][] yLines;

    private static FloatBuffer colorBuffer;

    private int bufferLength;


    public TronGrid() {
        xLines = new float[GRID_SIZE][DrawableObject.DIMENSIONS * 2];
        yLines = new float[GRID_SIZE][DrawableObject.DIMENSIONS * 2];
        int index = 0;
        // fill out the X grid and y grid lines.
        // have i mark out the important coordinates of the grid lines.
        for (int i = -GRID_SIZE / 2 * STEP_FACTOR; i < GRID_SIZE / 2 * STEP_FACTOR; i += STEP_FACTOR) {
            xLines[index][0] = -GRID_SIZE / 2 * STEP_FACTOR;
            xLines[index][1] = i;
            xLines[index][2] = 0;

            xLines[index][3] = GRID_SIZE / 2 * STEP_FACTOR;
            xLines[index][4] = i;
            xLines[index][5] = 0;

            // For each y-line, the relationship is flipped between the x and y coordinates.
            yLines[index][0] = i;
            yLines[index][1] = -GRID_SIZE / 2 * STEP_FACTOR;
            yLines[index][2] = 0;

            yLines[index][3] = i;
            yLines[index][4] = GRID_SIZE / 2 * STEP_FACTOR;
            yLines[index][5] = 0;

            index++;
        }


        // stuff them into one big array.
        float[] masterVertexArray = new float[2 * GRID_SIZE * (DIMENSIONS * 2)];
        bufferLength = masterVertexArray.length / DIMENSIONS;
        index = 0;
        for (int i = 0; i < masterVertexArray.length / 2; i += DIMENSIONS * 2) {
            System.arraycopy(xLines[index], 0, masterVertexArray, i, DIMENSIONS * 2);
            index++;
        }

        index = 0;
        for (int i = masterVertexArray.length / 2; i < masterVertexArray.length; i += DIMENSIONS * 2) {
            System.arraycopy(yLines[index], 0, masterVertexArray, i, DIMENSIONS * 2);
            index++;
        }

        // fill up the color array with blue.
        float[] colorArray = new float[2 * GRID_SIZE * (4 * 2)];
        for (int i = 0; i < colorArray.length; i += 4) {
            colorArray[i] = 182/255f;
            colorArray[i + 1] = 182/255f;
            colorArray[i + 2] = 182/255f;
            colorArray[i + 3] = 1.0f;
        }

        setVerticies(masterVertexArray);
        colorBuffer = convertFloatArray(colorArray);
    }

    /**
     * Draw the axis.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, DIMENSIONS,
                GLES20.GL_FLOAT, false,
                DIMENSIONS * 4, getVertexData());

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Set color for drawing the axis
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4,
                GLES20.GL_FLOAT, false,
                COLOR_STRIDE, colorBuffer);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        for (int i = 0; i < bufferLength / 2; i++) {
            GLES20.glDrawArrays(GLES20.GL_LINES, i * 2, 2);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);

        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }

}
