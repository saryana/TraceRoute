package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

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



    public TronGrid() {
        xLines = new float[GRID_SIZE][DrawableObject.DIMENSIONS * 2];
        yLines = new float[GRID_SIZE][DrawableObject.DIMENSIONS * 2];
        int index = 0;
        // fill out the X grid and y grid lines.
        // have i mark out the important coordinates of the grid lines.
        for (int i = -GRID_SIZE / 2 * DIMENSIONS; i < GRID_SIZE / 2 * DIMENSIONS; i += DIMENSIONS) {
            xLines[index][0] = -GRID_SIZE / 2 * DIMENSIONS;
            xLines[index][1] = i;
            xLines[index][2] = 0;

            xLines[index][3] = GRID_SIZE / 2 * DIMENSIONS;
            xLines[index][4] = i;
            xLines[index][5] = 0;

            // For each y-line, the relationship is flipped between the x and y coordinates.
            yLines[index][0] = i;
            yLines[index][1] = -GRID_SIZE / 2 * DIMENSIONS;
            yLines[index][2] = 0;

            yLines[index][3] = i;
            yLines[index][4] = GRID_SIZE / 2 * DIMENSIONS;
            yLines[index][5] = 0;

            index++;
        }


        // stuff them into one big array.
        float[] masterVertexArray = new float[2 * GRID_SIZE * (DIMENSIONS * 2)];
        index = 0;
        for (int i = 0; i < masterVertexArray.length / 2; i += DIMENSIONS * 2) {
            for (int j = 0; j < DIMENSIONS * 2; j++) {
                masterVertexArray[i + j] = xLines[index][j];
            }
            index++;
        }

        index = 0;
        for (int i = masterVertexArray.length / 2; i < masterVertexArray.length; i += DIMENSIONS * 2) {
            for (int j = 0; j < DIMENSIONS * 2; j++) {
                masterVertexArray[i + j] = yLines[index][j];
            }
            index++;
        }

        // fill up the color array with blue.
        float[] colorArray = new float[2 * GRID_SIZE * (4 * 2)];
        for (int i = 0; i < colorArray.length; i += 4) {
            colorArray[i] = 0.0f;
            colorArray[i + 1] = 0.0f;
            colorArray[i + 2] = 1.0f;
            colorArray[i + 3] = 1.0f;
        }

        setVerticies(masterVertexArray);
        colorBuffer = convertFloatArray(colorArray);
    }
}
