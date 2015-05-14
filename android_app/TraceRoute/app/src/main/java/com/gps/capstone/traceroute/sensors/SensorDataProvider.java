package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.gps.capstone.traceroute.sensors.listeners.AccelerometerCompassListener;
import com.gps.capstone.traceroute.sensors.listeners.AltitudeListener;
import com.gps.capstone.traceroute.sensors.listeners.DirectionListener;
import com.gps.capstone.traceroute.sensors.listeners.DirectionTestClass;
import com.gps.capstone.traceroute.sensors.listeners.GyroscopeListener;
import com.gps.capstone.traceroute.sensors.listeners.MySensorListener;
import com.gps.capstone.traceroute.sensors.listeners.StepDetectorListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
    // Scale for converting ft to openGL units, currently arbitrary value that looks good
    private static final float OPENGL_SCALE = .0118f;
    // https://www.walkingwithattitude.com/articles/features/how-to-measure-stride-or-step-length-for-your-pedometer
    // Men ~ .415 Women ~.413 => this only really matters if we want to display distance traveled
    private static final float STRIDE_RATIO = .41f;

    // Bus for posting and receiving event
    private Bus mBus;

    public static boolean USE_ACCELERATION;
    private final DirectionTestClass mDirectionTest;
    // Marker for knowing whether or not to use the gyroscope as our source of data
    private boolean mUseGyroscope;

    // The current sensor lister we are using
    private MySensorListener mOrientationSensor;
    // We need to keep track of the step detector for the entire period
    private StepDetectorListener mStepDetector;
    // Direction Listener
    private DirectionListener mDirectionDeterminer;
    // Altitude listener
    private AltitudeListener mAltitudeListener;
    // Context we got called from
    private Context mContext;
    // Shared prefrences
    private SharedPreferences mSharedPrefs;

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
    private float[] mOldLocationOGL;
    private float[] mNewLocationOGL;
    // Stride length = height * ratio
    private float mStrideLength;

    /**
     * Sets up the basic utilities to make this work
     * @param context Context we got called in
     */
    public SensorDataProvider(Context context) {
        this.mContext = context;
        mUseGyroscope = PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(context.getString(R.string.pref_key_use_gyroscope), true);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mBus = BusProvider.getInstance();

        // Path determining sensors
        mStepDetector = new StepDetectorListener(mContext);
        // Uses accelerometer detection
        mDirectionDeterminer = new DirectionListener(mContext);
        // Uses the compass
        mDirectionTest = new DirectionTestClass(mContext);
        mAltitudeListener = new AltitudeListener(mContext);

        mInitalAltitude = 0;
        mAltitude = 0;
        mPrevAltitude = 0;
        mHeading = 0;
        mOldLocation = new float[3];
        mNewLocation = new float[3];
        mNewLocationOGL = new float[3];
        mOldLocationOGL = new float[3];
        mStrideLength = 0;

        determineOrientationListener();
    }

    /**
     * Depending on the settings it will set the default listener to use, currently it is
     * between the Gyroscope and the Accelerometer
     */
    private void determineOrientationListener() {
        if (mUseGyroscope) {
            Log.i(TAG, "SETTING GYROSCOPE LISTENER");
            mOrientationSensor = new GyroscopeListener(mContext);
        } else {
            Log.i(TAG, "SETTING MATRIX LISTENER");
            mOrientationSensor = new AccelerometerCompassListener(mContext);
        }
    }

    /**
     * Re-register the things we need to keep track of
     * @param userControl if true, user is controlling the movement,
     *                    otherwise it is based off of sensors
     * @param useGyroscope If true, using the gyroscope for the medium of moving
     *                     the model, false then using the rotation matrix
     */
    public void register(boolean userControl, boolean useGyroscope) {
        // Are we changing our state?
        if (mUseGyroscope != useGyroscope) {
            this.mUseGyroscope = useGyroscope;
            determineOrientationListener();
        }
        mBus.register(this);
        USE_ACCELERATION = mSharedPrefs.getBoolean(mContext.getString(R.string.pref_key_use_acceleration), true);
        int height  = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_total_height_in), 0);
        mStrideLength = height * STRIDE_RATIO;
        mDirectionDeterminer.register();
        mOrientationSensor.register();
        mStepDetector.register();
        mDirectionTest.register();
        mAltitudeListener.register();
        // If we have user control we might have to change it here
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        mBus.unregister(this);
        mOrientationSensor.unregister();
        mStepDetector.unregister();
        mDirectionDeterminer.unregister();
        mDirectionTest.unregister();
        mAltitudeListener.unregister();
    }

    @Subscribe
    public void onDataChange(NewDataEvent event) {
        switch (event.type) {
            case ALTITUDE_CHANGE:
                handleAltitude(event.values[0]);
                break;
            // Set the new heading direction
            case DIRECTION_CHANGE:
                mHeading = event.values[0];
                break;
            case STEP_DETECTED:
                handleStepChange();
                break;
        }
    }

    /**
     * AYY we have a step
     */
    private void handleStepChange() {
        // Get unit vector of heading
        float[] xy = SensorUtil.getVectorFromAngle1(mHeading);
        // scale the vector to stride length and add to old location
        mNewLocation[0] = mOldLocation[0] + xy[0] * mStrideLength;
        mNewLocation[1] = mOldLocation[1] + xy[1] * mStrideLength;
        // every time we record the altitude lets do it relative to
        // what the initial reading was.
        mNewLocation[2] = mAltitude - mInitalAltitude;

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
            mNewLocation[0] = mOldLocation[0];
            mNewLocation[1] = mNewLocation[1];
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
        Log.d(TAG, "Updating view " + Arrays.toString(mNewLocation));
        // mark this as the last altitude that we sent to the view
        mPrevAltitude = mAltitude;
        // Calculate the new openGL values
        updateOpenGLvalues();
        Log.d("NewPint", Arrays.toString(mNewLocationOGL));
        // Post the new location with the new direction
        mBus.post(new NewLocationEvent(mNewLocationOGL));
        // Set the old values to the values we just read
        mOldLocation = mNewLocation;
        mOldLocationOGL = mNewLocationOGL;
    }
    /**
     * This is what we are going to be sending the view
     */
    private void updateOpenGLvalues() {
        for (int i = 0; i < mOldLocation.length; i++)
            mNewLocationOGL[i] = mNewLocation[i] * OPENGL_SCALE;
    }

}
