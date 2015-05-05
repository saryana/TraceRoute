package com.gps.capstone.traceroute.sensors;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.listeners.AccelerometerCompassListener;
import com.gps.capstone.traceroute.sensors.listeners.DirectionListener;
import com.gps.capstone.traceroute.sensors.listeners.DirectionTestClass;
import com.gps.capstone.traceroute.sensors.listeners.GyroscopeListener;
import com.gps.capstone.traceroute.sensors.listeners.MySensorListener;
import com.gps.capstone.traceroute.sensors.listeners.StepDetectorListener;

/**
 * Created by saryana on 4/16/15.
 * This will help keep state of what we actually need to change and send as time goes on.
 * For now it will do a lot of book keeping until we can figure out what we actually need.
 */
public class SensorDataProvider {
    // Tag for logging
    private final String TAG = getClass().getSimpleName();
    public static boolean USE_ACCELERATION;
    private final DirectionTestClass mDirectionTest;
    // Marker for knowing whether or not to use the gyroscope as our source of data
    private boolean mUseGyroscope;

    // The current sensor lister we are using
    private MySensorListener mSensorListener;
    // We need to keep track of the step detector for the entire period
    private StepDetectorListener mStepDetector;
    // Direction Listener
    private DirectionListener mDirectionDeterminer;
    // Context we got called from
    private Activity mActivity;
    // Shared prefrences
    private SharedPreferences mSharedPrefs;

    /**
     * Sets up the basic utilities to make this work
     * @param activity Context we got called in
     */
    public SensorDataProvider(Activity activity) {
        this.mActivity = activity;
        mUseGyroscope = PreferenceManager.getDefaultSharedPreferences(activity)
                                .getBoolean(activity.getString(R.string.pref_key_use_gyroscope), true);
        mStepDetector = new StepDetectorListener(activity);
        mDirectionDeterminer = new DirectionListener(activity);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        mDirectionTest = new DirectionTestClass(activity);
        determineListener();
    }

    /**
     * Depending on the settings it will set the default listener to use, currently it is
     * between the Gyroscope and the Accelerometer
     */
    private void determineListener() {
        if (mUseGyroscope) {
            Log.i(TAG, "SETTING GYROSCOPE LISTENER");
            mSensorListener = new GyroscopeListener(mActivity);
        } else {
            Log.i(TAG, "SETTING MATRIX LISTENER");
            mSensorListener = new AccelerometerCompassListener(mActivity);
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
            determineListener();
        }
        USE_ACCELERATION = mSharedPrefs.getBoolean(mActivity.getString(R.string.pref_key_use_acceleration), true);
        mDirectionDeterminer.register();
        mSensorListener.register();
        mStepDetector.register();
        mDirectionTest.register();
        // If we have user control we might have to change it here
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        mSensorListener.unregister();
        mStepDetector.unregister();
        mDirectionDeterminer.unregister();
        mDirectionTest.unregister();
    }


}
