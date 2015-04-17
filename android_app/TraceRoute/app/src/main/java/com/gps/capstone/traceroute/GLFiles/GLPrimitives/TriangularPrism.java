package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.ProgramManager;

/**
 * Defines a line primitive to be drawn in openGL.
 */
public class TriangularPrism {
    // Stores the graphics environment manager.
    private ProgramManager mGraphicsEnv;

    private float[] coords;

    /**
     * Create a new line object, with the given graphics environment,
     * and the given 3d coordinates.
     * @param graphicsEnv
     */
    public TriangularPrism(ProgramManager graphicsEnv, float x1, float y1, float z1,
                           int x2, int y2, int z2) {
        mGraphicsEnv = graphicsEnv;
        coords = new float[6];
        coords[0] = x1;
        coords[1] = y1;
        coords[2] = z1;
        coords[3] = x2;
        coords[4] = y2;
        coords[5] = z2;



    }

    // draw the line.
    private void draw(float[] mvpMatrix) {
        int programHandle = mGraphicsEnv.getProgram();


    }
}
