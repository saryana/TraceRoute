package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Axis;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Cube;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.PrismPath;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.RectangularPrism;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.TriangularPrism;
import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by saryana on 4/9/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final float THICKNESS = 0.1f;
    private final String TAG = getClass().getSimpleName();
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    private float[] mGyroRotationMatrix = new float[16];
    private boolean mHaveInitialOrientation = false;

    private ProgramManager mGraphicsEnvironment;

    private Axis mAxis;
    private Cube mCube;
    private TriangularPrism mPrism;
    private PrismPath mPath;
    private RectangularPrism mRectangularPrism;
    private boolean mInit;

    public MyGLRenderer(Context context) {
        // Does this break if it is here instead of onSurfaceCreated?
        mGraphicsEnvironment = new ProgramManager(context);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // create the shader manager object for loading shaders.
//        mGraphicsEnvironment = new ProgramManager();

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        mAxis = new Axis(mGraphicsEnvironment);
        mCube = new Cube(mGraphicsEnvironment);
        mPrism = new TriangularPrism(mGraphicsEnvironment);
        mPath = new PrismPath(mGraphicsEnvironment);
        mRectangularPrism = new RectangularPrism(mGraphicsEnvironment);
        mRectangularPrism.setDimensions(new float[3], new float[3], THICKNESS, THICKNESS);
        mInit = false;
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 4, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] scratch =  mRotationMatrix; //new float[16];
        float[] scratch2 = new float[16];

        // This determines if the user is taking control or it is based off of the orientation of the phone
        if (OpenGLActivity.USER_CONTROL) {
            Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
            //Matrix.invertM(scratch, 0, mRotationMatrix, 0);
        } else {
            //Matrix.invertM(scratch, 0, mRotationMatrix, 0);
        }

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, scratch, 0);

        mAxis.draw(scratch2);
        // If we don't want to use a shape that means
        // we are drawing a path!
        if (!OpenGLActivity.USE_SHAPE) {
            mRectangularPrism.draw(scratch2);
            if (mInit) {
                mPath.draw(scratch2);
            }
        // Renders the mutlicolor cube
        } else if (OpenGLActivity.USE_CUBE) {
            mCube.draw(scratch2);
        // Renders the mutlicolor prism
        } else {

            mPrism.draw(scratch2);
        }

//        mPath.draw(scratch2);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);

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

    /**
     * Adds a new face to the path
     * @param newFace Face to add
     */
    public void addFaces(float[] newFace) {
        mInit = true;
        mRectangularPrism.setDimensions(new float[3], newFace, THICKNESS, THICKNESS);
        float[] opposite = new float[3];
        for (int i = 0; i < newFace.length; i++) {
            opposite[i] = -newFace[i];
        }
        mPath.addPoint(opposite);
    }
}
