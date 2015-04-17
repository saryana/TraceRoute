package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by saryana on 4/9/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    private float[] mGyroRotationMatrix = new float[16];
    private boolean mHaveInitialOrientation = false;

    private ProgramManager graphicsEnvironment;

    private Axis mAxis;
    private Cube mCube;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // create the shader manager object for loading shaders.
        graphicsEnvironment = new ProgramManager();

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mAxis = new Axis();
        mCube = new Cube(graphicsEnvironment, null, true);

    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] scratch = new float[16];
        float[] scratch2 = new float[16];

        // This determines if the user is taking control or it is based off of the orientation of the phone
        if (OpenGL.USER_CONTROL) {
            Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        } else {
            Matrix.invertM(scratch, 0, mRotationMatrix, 0);
        }

        // Are we using the gyroscope data?
        if (OpenGL.USE_GYROSCOPE) {


        // This gets fired when we are dealing with just the rotation matrix
        } else {
            // Combine the rotation matrix with the projection and camera view
            // Note that the mMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, scratch, 0);
        }

        mAxis.draw(scratch2);
        mCube.draw(scratch2);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
    public void setRotationMatrix(float[] r) {
        mRotationMatrix = r;
    }

}
