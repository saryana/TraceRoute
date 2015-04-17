package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.ProgramManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Defines an over-arching class
 * that all drawable objects should implement.
 *
 * REQUIREMENTS:
 *
 * The drawable object should use floating point numbers for
 * coordinates, and it needs to be 3-d
 */
public abstract class DrawableObject {
    // Stores the graphics environment manager.
    protected ProgramManager mGraphicsEnv;
    protected int programHandle;

    // Various handles.
    protected int mMVPMatrixHandle;
    protected int mFragmentColorHandle;
    protected int mFragmentPositionHandle;
    protected int mVertexPositionHandle;
    protected int mVertexColorHandle;

    // The number of dimensions per vertex. (This should always be 3. We don't
    // support 2-D)
    private final int DIMENSIONS = 3;

    private final int FLOAT_SIZE = 4;

    protected FloatBuffer vertexData;

    /**
     * The constructor for this object.
     * Takes in a graphics environment object that stores the
     * openGL runtime environment and a buffer for
     * vertex data that stores information about the shape.
     * @param graphicsEnvironment
     * @param verticies
     */
    public DrawableObject(ProgramManager graphicsEnvironment, float[] verticies) {

        // HANDLING THE OPEN GL SETUP

        mGraphicsEnv = graphicsEnvironment;
        // Compile the shaders and return the program handle.
        programHandle = graphicsEnvironment.getProgram();
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        mFragmentPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mVertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition");
        mFragmentColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mVertexColorHandle = GLES20.glGetAttribLocation(programHandle, "vColor");

        // STORING THE VERTEX DATA
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                verticies.length * FLOAT_SIZE);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexData = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexData.put(verticies);
        // set the buffer to read the first coordinate
        vertexData.position(0);


    }


    /**
     * Returns the handle for the MVP matrix for this openGL session.
     * @return
     */
    public int getMVPMatrixHandle() {
        return mMVPMatrixHandle;
    }

    /**
     * Returns the vertex data.
     * @return
     */
    public FloatBuffer getVertexData() {
        return vertexData;
    }

    // UTILITY FUNCTIONS

    /**
     * Takes a draw order and converts it into an openGL-compatible format.
     * @param list
     * @return
     */
    public static ShortBuffer convertDrawList(short[] list) {
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
}
