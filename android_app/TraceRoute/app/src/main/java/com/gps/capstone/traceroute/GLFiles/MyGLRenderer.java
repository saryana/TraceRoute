package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Axis;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.Cube;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.PrismPath;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.RectangularPrism;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.SmartRectangularPrism;
import com.gps.capstone.traceroute.GLFiles.GLPrimitives.TriangularPrism;
import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by saryana on 4/9/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;

    private static final float THICKNESS = 0.1f;
    private final String TAG = getClass().getSimpleName();

    // All of the matricies for rendering objects to our viewport.
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mGyroRotationMatrix = new float[16];
    private boolean mHaveInitialOrientation = false;

    private ProgramManager mGraphicsEnvironment;

    // All of the geometric primitives that can
    // be drawn to the screen.
    private Axis mAxis;
    private Cube mCube;
    private TriangularPrism mTriangularPrism;
    private PrismPath mPath;
    private SmartRectangularPrism mRectangularPrism;
    private boolean mInit;
    private List<SmartRectangularPrism> mPathTest;

    public MyGLRenderer(Context context) {
        mPathTest = new LinkedList<>();
        this.context = context;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Does this break if it is in the constructor? Yes - we get an OpenGL error.
        mGraphicsEnvironment = new ProgramManager(context);

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        mAxis = new Axis(mGraphicsEnvironment);
        mCube = new Cube(mGraphicsEnvironment);
        mTriangularPrism = new TriangularPrism(mGraphicsEnvironment);
        mPath = new PrismPath(mGraphicsEnvironment);
        mRectangularPrism = new SmartRectangularPrism(mGraphicsEnvironment);
        float[] faceOne = {-0.3f, 0.0f, 0.0f};
        float[] faceTwo = {0.3f, 0.0f, 0.0f};
        mRectangularPrism.setDimensions(faceOne, faceTwo);
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
        }

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, scratch, 0);

        mAxis.draw(scratch2);
        // If we don't want to use a shape that means
        // we are drawing a path!
        if (!OpenGLActivity.USE_SHAPE) {
            mPath.draw(scratch2);
        // Renders the mutlicolor cube
        } else if (OpenGLActivity.USE_CUBE) {
            //mCube.draw(scratch2);
            mRectangularPrism.draw(scratch2);
        // Renders the mutlicolor prism
        } else {
            mTriangularPrism.draw(scratch2);
        }
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
    public void addFaces(float[] oldFaces, float[] newFace) {
        mInit = true;
        mRectangularPrism.setDimensions(oldFaces, newFace);
        float[] opposite = new float[3];
        for (int i = 0; i < newFace.length; i++) {
            opposite[i] = -newFace[i];
        }
        mPath.addPoint(opposite);
        // My hunch is that since we are changeling mRectangularPrism that the reference in the
        // list is getting updated too so we have just a list of the same objects.
        // As far as why PrismPath isn't working is beyond me that suspicion was based off of
        // nested draw calls that Andrew doesn't think is a problem.
        mPathTest.add(mRectangularPrism);
    }
}
