package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.gps.capstone.traceroute.GLFiles.math.Quaternion;
import com.gps.capstone.traceroute.GLFiles.util.TouchType;
import com.gps.capstone.traceroute.GLFiles.util.TouchUtil;
import com.gps.capstone.traceroute.GLFiles.util.VectorLibrary;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.sensors.events.NewPathFromFile;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by saryana on 4/9/15.
 *
 * NOTE: THERE'S getHistoricalX and getHistoricalY methods built into the
 * touch event. We may not need any of these fields.
 */
public class MySurfaceView extends GLSurfaceView {
    // The class tag
    private final String TAG = this.getClass().getSimpleName();

    // Keeps track of the most recent motion event.
    private TouchType previousMotion = TouchType.SHIT;

    private MyGLRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 90.0f / 320;


    // Fields to keep track of 1-finger movement. This is for rotation.
    private float mPreviousX;
    private float mPreviousY;

    // Fields to keep track of two-finger movement.
    // These will be used for panning and zooming.
    private float previousXFingerOne;
    private float previousYFingerOne;

    private float previousXFingerTwo;
    private float previousYFingerTwo;
    // Keeps track of the distance between the two fingers.
    private float previousDistance;
    // Keep track of the pointerIDs for both fingers to make
    // see if they're part of the same touch event or not.
    private int fingerOneID;
    private int fingerTwoID;


    public MySurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing values
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void onDataChange(NewLocationEvent e) {
        if (e.location == null) {
            mRenderer.clearPath();
        } else {
            mRenderer.addNewFace(e.location);
        }
        requestRender();
    }

    @Subscribe
    public void onDataChange(NewDataEvent e) {
        if (OpenGLActivity.FOLLOW_PATH || OpenGLActivity.USER_CONTROL) {
            return;
        }
        // TODO Need to clean this logic up honestly
        switch (e.type) {
            case ROTATION_MATRIX_CHANGE:
                mRenderer.setModelMatrix(e.values);
                break;
            case DELTA_ROTATION_MATRIX:
                // this is for when we have the information form the
                // gyroscope
                mRenderer.setModelMatrix(e.values);
                break;
            default:
                float[] m = new float[16];
                Matrix.setIdentityM(m, 0);
                mRenderer.setModelMatrix(m);
        }
        requestRender();
    }
    @Subscribe
    public void onNewPath(NewPathFromFile pathEvent) {
        mRenderer.clearPath();
        ArrayList<float[]> path = pathEvent.path;
        OpenGLActivity.FOLLOW_PATH = true;
        Thread.yield();
        // TODO THIS IS AWFUL - THIS ISN'T NECESSARY IF WE DON'T WANT TO ANIMATE THE STEPS
        for (int i = 0; i < path.size(); i++) {
            mRenderer.addNewFace(path.get(i));
            requestRender();
            if (!pathEvent.wait) continue;
            int z = 0;
            // Draws the path slowly by doing things on the main thread
            for (int j = 0; j< 90_099_999; j++) {
                z *= j * i;
            }
        }
        OpenGLActivity.FOLLOW_PATH = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        // First thing we want to figure out is how many fingers are touching the screen.
        int numFingers = e.getPointerCount();


        // This fires if there's only one finger.
        if (numFingers == 1) {
            computeOneFingerMotion(e);
        // For two finger touch events.
        } else if (numFingers == 2) {
            // compute the pan and zoom.
            computeTwoFingerMotion(e);
        } else {
        // For garbage touch events.
            previousMotion = TouchType.SHIT;
        }

        requestRender();
        return true;
    }

    // computes camera rotation for single-finger movement.
    private void computeOneFingerMotion(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        // If the previous motion wasn't a single finger
        // touch, set the previous coordinates to be the coordinates
        // of this single finger touch and exit.
        if (previousMotion != TouchType.SINGLE) {
            // Update the previousMotion to be a single finger touch event!
            previousMotion = TouchType.SINGLE;
            mPreviousX = x;
            mPreviousY = y;
            return;
        }

        // If this is not the first single-finger touch event,
        // then compute rotation.
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                // get a sphere radius that's half the size of the screen.
                float sphereRadius = getWidth() / 2;

                Quaternion rotation = determineRotation(sphereRadius, x, y, mPreviousX, mPreviousY);

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                mRenderer.translate(dx, dy);
                // reverse direction of rotation above the mid-line
                //if (y > getHeight() / 2) {
                //    dx = dx * -1;
                //}

                // reverse direction of rotation to left of the mid-line
                //if (x < getWidth() / 2) {
                //    dy = dy * -1;
                //}

                mRenderer.rotate(rotation);
                mPreviousX = x;
                mPreviousY = y;
                break;
            // This fires when the user lifts their finger up
            // from the screen after performing a single
            // finger touch event.
            case MotionEvent.ACTION_UP:
                previousMotion = TouchType.SHIT;
                break;
        }
    }

    // Decides how to change the view for a two-finger motion event.
    private void computeTwoFingerMotion(MotionEvent e) {
        // if the previous motion wasn't a double touch, or we don't recognise the
        // pointer IDs, set the initial state for a double touch event.
        if (previousMotion != TouchType.DOUBLE || (e.findPointerIndex(fingerOneID) == -1 ||
                e.findPointerIndex(fingerTwoID) == -1)) {
            previousMotion = TouchType.DOUBLE;
            fingerOneID = e.getPointerId(0);
            fingerTwoID = e.getPointerId(1);

            previousXFingerOne = e.getX(0);
            previousYFingerOne = e.getY(0);

            previousXFingerTwo = e.getX(1);
            previousYFingerTwo = e.getY(1);

            previousDistance = TouchUtil.distanceFormula(previousXFingerOne, previousYFingerOne, previousXFingerTwo, previousYFingerTwo);

        } else {
            // if everything checks out, get the indicies for finger one and two,
            // and compute the pan and zoom changes!
            int fingerOne = e.findPointerIndex(fingerOneID);
            int fingerTwo = e.findPointerIndex(fingerTwoID);
            float curXFingerOne = e.getX(fingerOne);
            float curYFingerOne = e.getY(fingerOne);
            float curXFingerTwo = e.getX(fingerTwo);
            float curYFingerTwo = e.getY(fingerTwo);
            //computeTwoFingerPan(e, curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
            computeTwoFingerZoom(e, curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
            // update the previous values of the finger positions.
            previousXFingerOne = curXFingerOne;
            previousYFingerOne = curYFingerOne;
            previousXFingerTwo = curXFingerTwo;
            previousYFingerTwo = curYFingerTwo;
        }
    }

    // Compute the pan amount for a two finger touch event.
    // Panning is a little more nuanced than zoom - there could be certain two finger gestures
    // that could confuse this. Right now the method I'm using for panning
    // is simple: Compute the previous midpoint and the current midpoint between the two fingers,
    // and pan the camera based on how the midpoint changed.
    private void computeTwoFingerPan(MotionEvent e, float curXFingerOne,
                                     float curYFingerOne, float curXFingerTwo, float curYFingerTwo) {
        float[] prevMidpoint = TouchUtil.midpointFormula(previousXFingerOne, previousYFingerOne, previousXFingerTwo, previousYFingerTwo);
        float[] curMidpoint = TouchUtil.midpointFormula(curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
        float deltaMidpointX = curMidpoint[0] - prevMidpoint[0];
        float deltaMidpointY = curMidpoint[1] - prevMidpoint[1];
        if (deltaMidpointX == Float.NaN || deltaMidpointY == Float.NaN) {
            Log.i("DEBUG", "HOLY BALLS NAN1");
        }
        // TODO: Do something with these deltas! Pan the camera based on them.
        mRenderer.translate(deltaMidpointX, deltaMidpointY);
//        Log.i(TAG, "Delta X " + deltaMidpointX + " Delta Y " + deltaMidpointY);

    }

    // Compute the zoom amount for a two-finger touch event.
    private void computeTwoFingerZoom(MotionEvent e, float curXFingerOne,
                                      float curYFingerOne, float curXFingerTwo, float curYFingerTwo) {
        float curDistance = TouchUtil.distanceFormula(curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
        float deltaDistance = previousDistance - curDistance;
        // Set the previous distance to be the current distance.
        previousDistance = curDistance;
        // TODO: do something with deltaDistance! Change the zoom level based on the change in finger distance.
        mRenderer.zoom(deltaDistance);
    }

    /*
     * Determines arcball rotation based on a single finger gesture.
     */
    private Quaternion determineRotation(float radius, float curX, float curY, float prevX, float prevY) {

        float[] curSphereVector = getSphereVector(radius, curX, curY);
        float[] prevSphereVector = getSphereVector(radius, prevX, prevY);

        float[] rotationAxis = VectorLibrary.crossProduct(prevSphereVector, curSphereVector);

        float angle = VectorLibrary.dotProduct(prevSphereVector, curSphereVector);

        Quaternion rotation = new Quaternion(rotationAxis[0], rotationAxis[1], rotationAxis[2], (float)Math.acos(angle));


        return rotation;
    }

    /*
    Determines where the touch coordinates fall on a sphere with
    radius radius.
     */
    private float[] getSphereVector(float radius, float x, float y) {
        // flip around the y coordinate; y increases in value towards the bottom of the screen.
        y *= -1;
        // for storing the final coordinates.
        float[] result = new float[3];

        float sphereX;
        float sphereY;
        float sphereZ;

        float length = (float)Math.sqrt(x*x + y*y);
        // if the touch falls within the sphere, we're in good shape.
        if (length <= radius) {
            sphereZ = (float)Math.sqrt((radius*radius) - (length*length));
            sphereX = x;
            sphereY = y;
        } else {
            // the touch was outside the sphere.
            // scale down the x and y coordinates to be on the edge of the sphere
            float ratio = radius / length;
            x *= ratio;
            y *= ratio;

            sphereX = x;
            sphereY = y;
            sphereZ = 0;
        }

        result[0] = sphereX;
        result[1] = sphereY;
        result[2] = sphereZ;
        return result;
    }
}
