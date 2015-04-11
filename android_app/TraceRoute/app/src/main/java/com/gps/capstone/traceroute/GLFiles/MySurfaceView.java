package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by saryana on 4/9/15.
 *
 * NOTE: THERE'S getHistoricalX and getHistoricalY methods built into the
 * touch event. We may not need any of these fields.
 */
public class MySurfaceView extends GLSurfaceView {
    // Keeps track of the most recent motion event.
    private PreviousTouch previousMotion = PreviousTouch.SHIT;

    private MyGLRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;


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
            computeTwoFingerPan(e, fingerOne, fingerTwo);
            computeTwoFingerZoom(e, fingerOne, fingerTwo);
        }
    }

    // Compute the pan amount for a two finger touch event.
    private void computeTwoFingerPan(MotionEvent e, int fingerOne, int fingerTwo) {

    }

    // Compute the zoom amount for a two-finger touch event.
    private void computeTwoFingerZoom(MotionEvent e, int fingerOne, int fingerTwo) {

    }

    // Enumerated type to indicate what the previous touch event was.
    private enum PreviousTouch {
        SINGLE, DOUBLE, SHIT;
    }

    // computes the distance between two points and returns it in a float.
    private float distanceFormula(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
