package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Axis;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.DrawableObject;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.PrismPath;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.SmartRectangularPrism;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.TronGrid;
import com.gps.capstone.traceroute.GLFiles.math.Quaternion;
import com.gps.capstone.traceroute.GLFiles.math.Matrix4;
import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;
import com.gps.capstone.traceroute.GLFiles.util.VectorLibrary;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.SensorUtil;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by saryana on 4/9/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    // ANDROID UTILITIES
    private Context context;
    private final String TAG = getClass().getSimpleName();

    // GLOBAL MATRICIES
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    //private float[] mGyroRotationMatrix = new float[16];
    //private boolean mHaveInitialOrientation = false;

    // CAMERA
    private float[] cameraPos = {0, 0, 4};
    private float[] cameraTarget = {0, 0, 0};
    private float[] cameraOrientation = {0, 1, 0};

    // GEOMETRY
    private ProgramManager mGraphicsEnvironment;
    private Axis mAxis;
    private TronGrid grid;
    private PrismPath mPath;
    private SmartRectangularPrism mRectangularPrism;
    private boolean mInit;

    // PATH MANAGEMENT
    private float[] mPrevStepLocation;
    private float[] mPrevStepDirection;

    public MyGLRenderer(Context context) {
        this.context = context;
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Does this break if it is in the constructor? Yes - we get an OpenGL error.
        mGraphicsEnvironment = new ProgramManager(context);
        // THIS HAS TO BE THE SECOND CALL. Set all drawable objects
        // to have a reference to various graphics environment handles.
        DrawableObject.SetOpenGLEnvironment(mGraphicsEnvironment);
        float backgroundColor = (context.getResources().getColor(R.color.secondaryText) & 0xFF)/255f;
        // Set the background frame color
        GLES20.glClearColor(backgroundColor, backgroundColor, backgroundColor, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        mAxis = new Axis();
        grid = new TronGrid();
        mPath = new PrismPath();
        mRectangularPrism = new SmartRectangularPrism();
        float[] faceOne = {-0.3f, 0.0f, 0.0f};
        float[] faceTwo = {0.3f, 0.0f, 0.0f};
        mRectangularPrism.setDimensions(faceOne, faceTwo);
        mInit = false;

        mPrevStepLocation = new float[3];
        mPrevStepDirection = new float[3];

        singleFingerRotationMatrix = new float[16];
        Matrix.setIdentityM(singleFingerRotationMatrix, 0);


    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // VIEW AND PROJECTION
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, cameraPos[0], cameraPos[1], cameraPos[2],
                cameraTarget[0], cameraTarget[1], cameraTarget[2],
                cameraOrientation[0], cameraOrientation[1], cameraOrientation[2]);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        // We want this when we are don't want user control or if we are following a path
        if (mInit && OpenGLActivity.FOLLOW_PATH) {
            // xy angle (z axis rotation)
            float x = mPrevStepDirection[0];
            float y = mPrevStepDirection[1];

            float angle = (float)Math.atan2(y, x);
            angle -= Math.PI/2;

            angle = SensorUtil.radianToDegree(angle);
            // create a new model matrix
            float[] modelMatrix = new float[16];
            // add rotation
            Matrix.setRotateM(modelMatrix, 0, angle, 0, 0, 1);

            // computes a bird's eye view.
            float[] rotation2 = new float[16];
            float[] result;
            if (mPrevStepDirection[2] == 0) {
                float[] zLine = {0, 0, 1, 0};
                result = VectorLibrary.crossProduct(mPrevStepDirection, zLine);
            } else {
                float[] dropped = {mPrevStepDirection[0], mPrevStepDirection[1], 0, 0};
                result = VectorLibrary.crossProduct(mPrevStepDirection, dropped);
            }

            Matrix.setRotateM(rotation2, 0, 30, result[0], result[1], result[2]);



            // add translation
            Matrix.translateM(modelMatrix, 0, -mPrevStepLocation[0], -mPrevStepLocation[1], -mPrevStepLocation[2]);
            mModelMatrix = modelMatrix;
        } else if (OpenGLActivity.USER_CONTROL) {
            float[] modelTemp = new float[16];
            // Null pointer exception... On random reboot into setting
           // Matrix.multiplyMM(modelTemp, 0, mModelMatrix, 0, singleFingerRotationMatrix, 0);

            if (translated) {
                computeTranslation();
                translated = false;
            }
            if (zoomed) {
                computeZoom();
                zoomed = false;
            }
        }

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        float[] scratch2 = new float[16];
        Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, mModelMatrix, 0);
        mMVPMatrix = scratch2;

        float[] MVMatrix = new float[16];
        // I'm assuming 'scratch' is the model matrix.
        Matrix.multiplyMM(MVMatrix, 0, mModelMatrix, 0, mViewMatrix, 0);


        mAxis.draw(mMVPMatrix);
        grid.draw(mMVPMatrix);
        // If we don't want to use a shape that means
        // we are drawing a path!
        if (!OpenGLActivity.USE_SHAPE) {
            // Proper path drawing currently not working
            mPath.draw(mMVPMatrix, MVMatrix);
        } else {
            mRectangularPrism.draw(mMVPMatrix, MVMatrix);
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
    }

    /**
     * Adds a new face to the path
     * @param newFace Face to add
     */
    public void addNewFace(float[] newFace) {
        if (mPath == null) {
            return;
        }
        mInit = true;
        // bizarre referencing issues reason for cloning objects.
        // updates would overwrite the older data [1] => [2, 2] => [3, 3, 3]
        mPath.addPoint(newFace.clone());
        mPrevStepDirection = new float[]{newFace[0] - mPrevStepLocation[0],
                                        newFace[1] - mPrevStepLocation[1],
                                        newFace[2] - mPrevStepLocation[2]};
        mPrevStepLocation = newFace.clone();

    }
    public void clearPath() {
        mPath.clear();
        mPath = new PrismPath();
        mPrevStepLocation = new float[3];
        mPrevStepDirection = new float[3];
    }

    ////////////////////////////////
    // MODEL MATRIX MANIPULATION
    ////////////////////////////////

    public volatile float[] singleFingerRotationMatrix;
    private float translateX;
    private float translateY;
    private boolean translated;
    private float zoomAmount;
    private boolean zoomed;

    private static final float TRANSLATION_FACTOR = 0.001f;
    private static final float ZOOM_FACTOR = 0.005f;

    /**
     * Rotates the model with the given quaternion.
     * @param rotation
     */
    public void rotate(Quaternion rotation) {
        Matrix4 rotMatrix = rotation.toMatrix();
        singleFingerRotationMatrix = rotMatrix.getAsArray();
    }

    /**
     * Translates the model by the given amount.
     * @param x
     * @param y
     */
    public void translate(float x, float y) {
        x *= TRANSLATION_FACTOR;
        y *= TRANSLATION_FACTOR;
        translateX = x;
        translateY = y;
        if (translateX == Float.NaN || translateY == Float.NaN) {
            Log.i("DEBUG", "HOLY BALLS NAN2");
        }
        translated = true;
    }

    /**
     * Zooms in on the model by the given amount.
     * @param zoom
     */
    public void zoom(float zoom) {
        zoom *= ZOOM_FACTOR;
        zoomAmount = zoom;
        zoomed = true;
    }

    /**
     * Sets the model matrix to r.
     * @param r
     */
    public void setModelMatrix(float[] r) {
        mModelMatrix = r;
    }

    private void computeTranslation() {
        // get the original vector length
        float length = (float)Math.sqrt(translateX*translateX + translateY*translateY);
        float[] translationVector = {translateX, translateY, 0, 0};
        float[] result = new float[4];
        // convert the matrix into the rotation matrix.
        Matrix.multiplyMV(result, 0, mModelMatrix, 0, translationVector, 0);

        float vectorLength = VectorLibrary.vectorLength(result);
        float ratio = length / vectorLength;
        float count = 0;
        for (int i = 0; i < result.length; i++) {
            count += result[i];
        }
        if (count != 0.0f) {
            Matrix.translateM(mModelMatrix, 0, -ratio*result[0], -ratio*result[1], -ratio*result[2]);
        }
    }

    private void computeZoom() {
        Matrix.translateM(mModelMatrix, 0, 0, 0, zoomAmount);
    }
}
