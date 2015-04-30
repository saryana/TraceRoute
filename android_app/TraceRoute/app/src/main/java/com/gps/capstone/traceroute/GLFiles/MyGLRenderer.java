package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Axis;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Cube;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Path;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.RectangularPrism;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.TriangularPrism;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by saryana on 4/9/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final float THICKNESS = 0.01f;
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
    private Path mPath;
    private RectangularPrism mRectPrism;

//    float[] faceOne = {-0.3f, 0.1f, 0.1f};
    float[] faceTwo = {0.0f, 0.0f, 0.0f};

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // create the shader manager object for loading shaders.
        mGraphicsEnvironment = new ProgramManager();

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        mAxis = new Axis(mGraphicsEnvironment);
        mCube = new Cube(mGraphicsEnvironment);
        mPrism = new TriangularPrism(mGraphicsEnvironment);
        mPath = new Path(mGraphicsEnvironment);
        mRectPrism = new RectangularPrism(mGraphicsEnvironment);

        mRectPrism.setDimensions(faceTwo, faceTwo, THICKNESS, THICKNESS);
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
        if (OpenGL.USER_CONTROL) {
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
        if (!OpenGL.USE_SHAPE) {
            mRectPrism.draw(scratch2);
            // Renders the mutlicolor cube
        } else if (OpenGL.USE_CUBE) {
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

    public void addFaces(float[] oldFace, float[] newFace) {
        Log.i(TAG, Arrays.toString(newFace));
        mRectPrism.setDimensions(new float[]{0f, 0f, 0f}, newFace, THICKNESS, THICKNESS);
    }
}
