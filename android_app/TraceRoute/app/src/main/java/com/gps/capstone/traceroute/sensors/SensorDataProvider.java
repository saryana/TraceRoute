package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.gps.capstone.traceroute.sensors.events.PathCompletion;
import com.gps.capstone.traceroute.sensors.listeners.AltitudeListener;
import com.gps.capstone.traceroute.sensors.listeners.CompassListener;
import com.gps.capstone.traceroute.sensors.listeners.GyroscopeListener;
import com.gps.capstone.traceroute.sensors.listeners.StepDetectorListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by saryana on 4/16/15.
 * This will help keep state of what we actually need to change and send as time goes on.
 * For now it will do a lot of book keeping until we can figure out what we actually need.
 *
 * This may be kind of confusing for now, but i'm keeping things like current and past seperate
 * even though the current is short lived. I am hoping to potentially be holding a list of
 * steps and detect something like steps and be able to hot swap values by keeping a reference to them
 * this may be to optimistic but we're going with it for now.  This is almost god class but think
 * of it as a giant state machine that needs to keep track of certain things and detects change in
 * different cases
 */
public class SensorDataProvider {
    // Tag for logging
    private final String TAG = getClass().getSimpleName();
    // Threshold for detecting a difference in sole altitude change
    private static final float ALTITUDE_THRESHOLD = 5; // in feet
    // Threshold for detecting a difference in direction
    private static float DIRECTION_THRESHOLD; // 2 degrees in radians
    // The two OpenGL scales need to be tested so we can determine the proper values for them
    // The vertical need to be exaggerated more
    private static final float OPENGL_VERTICAL_SCALE = .06f;
    private static final float OPENGL_ONLY_VERTICAL_SCALE = .09f;
    // Scale for converting ft to openGL units, currently arbitrary value that looks good
    private static final float OPENGL_SCALE = .0218f;

    // Bus for posting and receiving event
    private Bus mBus;

    private CompassListener mCompass;
    // Marker for knowing whether or not to use the gyroscope as our source of data

    // The current sensor lister we are using
    private GyroscopeListener mOrientationSensor;
    // We need to keep track of the step detector for the entire period
    private StepDetectorListener mStepDetector;
    // Altitude listener
    private AltitudeListener mAltitudeListener;
    // Context we got called from
    private Context mContext;
    private ArrayList<float[]> mPath;

    // All the values we need to keep the state of
    // in feet
    private float mInitalAltitude;
    private float mAltitude;
    private float mPrevAltitude;

    // in radians
    private float mHeading;

    // (x, y, z) in feet
    private float[] mNewLocation;
    private float[] mOldLocation;
    // (x, y, z) in OpenGLUnits
    private float[] mNewLocationOGL;
    private float mStrideLength;
    private boolean mPathTracking;
    private int mSteps;

    /**
     * Sets up the basic utilities to make this work
     * @param context Context we got called in
     */
    public SensorDataProvider(Context context) {
        this.mContext = context;
        mBus = BusProvider.getInstance();
        SensorUtil.ALPHA = Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_alpha), ""+ SensorUtil.ALPHA));

        // Path determining sensors
        mStepDetector = new StepDetectorListener(mContext);
        // Uses the compass
        mCompass = new CompassListener(mContext);
        mAltitudeListener = new AltitudeListener(mContext);
        mPathTracking = false;
        // We are no longer using the accelerometer, just the gyroscope
        mOrientationSensor = null;
    }

    /**
     * Register the things necessary for the data provider to work. Still requires path starting and stopping
     * based off of user interaction
     */
    public void register() {
        DIRECTION_THRESHOLD = (float) Math.toRadians(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.pref_key_degree_filter), "" + 2)));
        mBus.register(this);
        mHeading = 0;
    }

    /**
     * Activates all the listeners and all the things we need to keep track of for a path.
     * This includes getting the stride length and resetting everything
     */
    public void startPath() {
        // This will reset anything that is keeping track of all our current steps
        mBus.post(new NewLocationEvent(null, null));
        mPath = new ArrayList<>();
        rotateModeFromGyroscope(false);
        mPathTracking = true;
        mInitalAltitude = 0;
        mAltitude = 0;
        mSteps = 0;
        mPrevAltitude = 0;
        mOldLocation = new float[3];
        mNewLocation = new float[3];
        mNewLocationOGL = new float[3];
        mStrideLength = PreferenceManager.getDefaultSharedPreferences(mContext).getFloat(mContext.getString(R.string.pref_key_stride_length), 0);
        mStepDetector.register();
        mAltitudeListener.register();
    }

    /**
     * Stops all the listeners involved in path tracking
     */
    public void stopPath() {
        mPathTracking = false;
        mStepDetector.unregister();
        mAltitudeListener.unregister();
        mBus.post(new PathCompletion(
                mSteps,   // Distance in inches
                mSteps*mStrideLength,
                mInitalAltitude,        // Initial altitude we detected
                mAltitude               // Final altitude
        ));
    }

    public void rotateModeFromGyroscope(boolean rotate) {
        if (rotate) {
            if (mOrientationSensor == null) {
                mOrientationSensor = new GyroscopeListener(mContext);
            }
            mCompass.unregister();
            mOrientationSensor.register();
        } else {
            if (mOrientationSensor != null) {
                mOrientationSensor.unregister();
                mOrientationSensor = null;
            }
            mCompass.register();
        }
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        if (mPathTracking) {
            stopPath();
        }

        mBus.unregister(this);
        if (mOrientationSensor != null) {
            mOrientationSensor.unregister();
        }
        mCompass.unregister();
    }

    public boolean saveCurrentPath(String pathName) {
        FileOutputStream fos;
        try {
            // Don't need no snitches stealing our files
            fos = mContext.openFileOutput(pathName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // Ideally we would use parcelable, but lists are already serializable
            oos.writeObject(mPath);
            oos.close();
            fos.close();
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Subscribe
    public void onDataChange(NewDataEvent event) {
        switch (event.type) {
            case ALTITUDE_CHANGE:
                handleAltitude(event.values[0]);
                break;
            // Set the new heading direction
            case DIRECTION_CHANGE:
                handleHeadingChange(event.values[0]);
                break;
            case STEP_DETECTED:
                handleStepChange();
                break;
        }
    }

    private void handleHeadingChange(float heading) {
        if (heading < 0) {
            heading = (float) (Math.PI +(Math.PI + heading));
        }
        // Did we break the direction threshold?
        if (Math.abs(heading - mHeading) > DIRECTION_THRESHOLD) {
            mHeading = heading;
        }
    }

    /**
     * AYY we have a step
     */
    private void handleStepChange() {
        mSteps++;
        // Get unit vector of heading
        float[] xy = SensorUtil.getVectorFromAngle3(mHeading);
        Log.d(TAG, "heading " + SensorUtil.radianToDegree(mHeading) + " angles " + Arrays.toString(xy) + " stride length" + mStrideLength);
        // scale the vector to stride length and add to old location
        mNewLocation[0] = mOldLocation[0] + xy[0] * mStrideLength;
        mNewLocation[1] = mOldLocation[1] + xy[1] * mStrideLength;
        // every time we record the altitude lets do it relative to
        // what the initial reading was.
        mNewLocation[2] = mAltitude - mInitalAltitude;
        Log.d(TAG, "location " + Arrays.toString(mNewLocation));
        // Woo! new data lets update the values
        updateView();

        // TODO: The data is still pretty jump and that is probably based off of the
        // compass as our form of heading which isn't super accurate... There is also
        // a weird issue when walking in a half circle and walking back it doesn't do things
        // in the proper return route, Compass reversed somehow? not getting negative?
    }

    /**
     * If it detects an altitude change significant enough it will update the
     * view with the change in just altitude change. This doesn't detect steps though
     * since that will be a combination of altitude and motion
     * @param altitudeFt The current reading of the value
     */
    private void handleAltitude(float altitudeFt) {
        mAltitude = altitudeFt;
        // initial reading lets set the first value
        if (mInitalAltitude == 0) {
            mInitalAltitude = mAltitude;
        // Are we breaking our threshold?
        } else if (Math.abs(mPrevAltitude - mAltitude) > ALTITUDE_THRESHOLD) {
            mNewLocation[2] = mAltitude - mInitalAltitude;
            updateView();
        }

    }

    /**
     * Updates the OpenGl values and sends it to the view to render. It is in OpenGL
     * units in 3D space so there isn't any computation required for it to do or state
     * to keep track of.
     *
     * This requires that the values are changed before calling this method.
     * This simply updaes a couple states and sends the event
     */
    private void updateView() {
        // mark this as the last altitude that we sent to the view
        mPrevAltitude = mAltitude;
        // Calculate the new openGL values
        updateOpenGLvalues();
//        Log.d("NewPint", Arrays.toString(mNewLocationOGL));
        mPath.add(mNewLocationOGL.clone());
        // Post the new location with the new direction
        mBus.post(new NewLocationEvent(mNewLocationOGL, mNewLocation));
        // Set the old values to the values we just read
        mOldLocation = mNewLocation;
    }
    /**
     * This is what we are going to be sending the view
     */
    private void updateOpenGLvalues() {
        // is this just a change in z?
        if (mNewLocation[0] == mOldLocation[0] && mNewLocation[2] != mOldLocation[2]) {
            mNewLocationOGL[2] = mNewLocation[2] * OPENGL_ONLY_VERTICAL_SCALE;
        } else {
            // Set the X and Y
            mNewLocationOGL[0] = mNewLocation[0] * OPENGL_SCALE;
            mNewLocationOGL[1] = mNewLocation[1] * OPENGL_SCALE;
            // The Z axis needs to be exaggerated
            mNewLocationOGL[2] = mNewLocation[2] * OPENGL_VERTICAL_SCALE;
        }
    }
}
