package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Defines a class
 * that all openGL figures should implement.
 *
 * REQUIREMENTS:
 *
 * The drawable object should use floating point numbers for
 * coordinates, and it needs to be 3-d
 */
public abstract class DrawableObject {

    // The number of dimensions per vertex. (This should always be 3. We don't
    // support 2-D.
    public static final int DIMENSIONS = 3;
    public static final int FLOAT_SIZE = 4;

    // A buffer for the vertex position data.
    protected FloatBuffer vertexData;

    protected FloatBuffer colorData;

    /**
     * Initializes all different lighting objects
     * @param graphicsEnv
     */
    public static void SetOpenGLEnvironment(ProgramManager graphicsEnv) {
        DiffuseLightingObject.SetOpenGLEnvironment(graphicsEnv);
        BasicLightingObject.SetOpenGLEnvironment(graphicsEnv);
    }

    /**
     * Set the drawable verticies for this object. MAKE SURE YOU CALL THIS
     * BEFORE ATTEMPTING TO DRAW THE OBJECT
     * @param verticies
     */
    public void setVerticies(float[] verticies) {
        // This next segment converts the vertex data to a FloatBuffer.
        vertexData = convertFloatArray(verticies);
    }

    public void setColors(float[] colors) {
        colorData = convertFloatArray(colors);
    }

    ///////////////////
    // GETTERS
    ///////////////////

    /**
     * Returns the vertex data.
     * @return The FloatBuffer the stores the vertex data.
     */
    public FloatBuffer getVertexData() {
        return vertexData;
    }

    /////////////////////////
    // UTILITY FUNCTIONS
    /////////////////////////

    /**
     * Takes a draw order and converts it into an openGL-compatible format.
     * @param list The draw order list
     * @return The draw order list converted to a ShortBuffer.
     */
    public static ShortBuffer convertShortArray(short[] list) {
        // Will hold the openGL-compatible draw order
        ShortBuffer result;

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                list.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        result = dlb.asShortBuffer();
        result.put(list);
        result.position(0);

        return result;
    }

    /**
     * Takes in a float array and converts it into an openGL friendly format.
     * @param list The array to convert
     * @return The array as a float buffer
     */
    public static FloatBuffer convertFloatArray(float[] list) {
        FloatBuffer result;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                list.length * FLOAT_SIZE);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        result = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        result.put(list);
        // set the buffer to read the first coordinate
        result.position(0);

        return result;
    }
}
