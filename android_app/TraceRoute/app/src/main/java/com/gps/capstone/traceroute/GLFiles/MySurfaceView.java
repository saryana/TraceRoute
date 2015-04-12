package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.gps.capstone.traceroute.BusProvider;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 4/9/15.
 *
 * NOTE: THERE'S getHistoricalX and getHistoricalY methods built into the
 * touch event. We may not need any of these fields.
 */
public class MySurfaceView extends GLSurfaceView {

    private final String TAG = this.getClass().getSimpleName();

    // Keeps track of the most recent motion event.
    private PreviousTouch previousMotion = PreviousTouch.SHIT;

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

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        BusProvider.getInstance().register(this);
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
            computeOneFingerRotation(e);
        // For two finger touch events.
        } else if (numFingers == 2) {
            // compute the pan and zoom.
            computeTwoFingerMotion(e);
        } else {
        // For garbage touch events.
            previousMotion = PreviousTouch.SHIT;
        }

        return true;
    }

    // computes camera rotation for single-finger movement.
    private void computeOneFingerRotation(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        // If the previous motion wasn't a single finger
        // touch, set the previous coordinates to be the coordinates
        // of this single finger touch and exit.
        if (previousMotion != PreviousTouch.SINGLE) {
            // Update the previousMotion to be a single finger touch event!
            previousMotion = PreviousTouch.SINGLE;
            mPreviousX = x;
            mPreviousY = y;
            return;
        }

        // If this is not the first single-finger touch event,
        // then compute rotation.
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
                mPreviousX = x;
                mPreviousY = y;
        }
    }

    // Decides how to change the view for a two-finger motion event.
    private void computeTwoFingerMotion(MotionEvent e) {
        // if the previous motion wasn't a double touch, or we don't recognise the
        // pointer IDs, set the initial state for a double touch event.
        if (previousMotion != PreviousTouch.DOUBLE || (e.findPointerIndex(fingerOneID) == -1 ||
                e.findPointerIndex(fingerTwoID) == -1)) {
            previousMotion = PreviousTouch.DOUBLE;
            fingerOneID = e.getPointerId(0);
            fingerTwoID = e.getPointerId(1);

            previousXFingerOne = e.getX(0);
            previousYFingerOne = e.getY(0);

            previousXFingerTwo = e.getX(1);
            previousYFingerTwo = e.getY(1);

            previousDistance = distanceFormula(previousXFingerOne, previousYFingerOne, previousXFingerTwo, previousYFingerTwo);

        } else {
            // if everything checks out, get the indicies for finger one and two,
            // and compute the pan and zoom changes!
            int fingerOne = e.findPointerIndex(fingerOneID);
            int fingerTwo = e.findPointerIndex(fingerTwoID);
            float curXFingerOne = e.getX(fingerOne);
            float curYFingerOne = e.getY(fingerOne);
            float curXFingerTwo = e.getX(fingerTwo);
            float curYFingerTwo = e.getY(fingerTwo);
            computeTwoFingerPan(e, curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
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
        float[] prevMidpoint = midpointFormula(previousXFingerOne, previousYFingerOne, previousXFingerTwo, previousYFingerTwo);
        float[] curMidpoint = midpointFormula(curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
        float deltaMidpointX = curMidpoint[0] - prevMidpoint[0];
        float deltaMidpointY = curMidpoint[1] - prevMidpoint[1];
        // TODO: Do something with these deltas! Pan the camera based on them.

        Log.i(TAG, "Delta X " + deltaMidpointX + " Delta Y " + deltaMidpointY);

    }

    // Compute the zoom amount for a two-finger touch event.
    private void computeTwoFingerZoom(MotionEvent e, float curXFingerOne,
                                      float curYFingerOne, float curXFingerTwo, float curYFingerTwo) {
        float curDistance = distanceFormula(curXFingerOne, curYFingerOne, curXFingerTwo, curYFingerTwo);
        float deltaDistance = previousDistance - curDistance;
        // TODO: do something with deltaDistance! Change the zoom level based on the change in finger distance.
    }

    // Enumerated type to indicate what the previous touch event was.
    private enum PreviousTouch {
        SINGLE, DOUBLE, SHIT;
    }

    // computes the distance between two points and returns it in a float.
    private float distanceFormula(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // computes the midpoint between two points and returns a tuple with
    // the x-coordinate and y-coordinate of the midpoint.
    private float[] midpointFormula(float x1, float y1, float x2, float y2) {
        // I know this is bad style. You can hit me if you want.
        // 0 = x-coordinate, 1 = y-coordinate
        float[] tuple = new float[2];
        // compute the relative position of the midpoint.
        float dx = (x1 - x2) / 2;
        float dy = (y1 - y2) / 2;

        // if x1's value is less than x2, add -1 * dx to x1 to get the x coordinate of the midpoint.
        if (dx < 0) {
            tuple[0] = x1 + (dx * -1);
        } else {
            // add the negative value of dx to x1 to get the x-coordinate of the midpoint.
            tuple[0] = x1 - dx;
        }

        // if y1's value is less than y2, add -1 * dy to y1 to get the y-coordinate of the midpoint.
        if (dy < 0) {
            tuple[1] = y1 + (dy * -1);
        } else {
            // add the negative value of dy to y1 to get the y-coordinate of the midpoint.
            tuple[1] = y1 - dy;
        }
        // return the midpoint.
        return tuple;
    }
}
