package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.listeners.AccelerometerCompassListener;
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

    // Marker for knowing whether or not to use the gyroscope as our source of data
    private boolean mUseGyroscope;

    // The current sensor lister we are using
    private MySensorListener mSensorListener;
    // We need to keep track of the step detector for the entire period
    private StepDetectorListener mStepDetector;
    // Context we got called from
    private Context mContext;

    /**
     * Sets up the basic utilities to make this work
     * @param context Context we got called in
     */
    public SensorDataProvider(Context context) {
        mContext = context;
        mUseGyroscope = PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(context.getString(R.string.pref_key_use_gyroscope), true);
        mStepDetector = new StepDetectorListener(context);
        determineListener();
    }

    /**
     * Depending on the settings it will set the default listener to use, currently it is
     * between the Gyroscope and the Accelerometer
     */
    private void determineListener() {
        if (mUseGyroscope) {
            Log.i(TAG, "SETTING GYROSCOPE LISTENER");
            mSensorListener = new GyroscopeListener(mContext);
        } else {
            Log.i(TAG, "SETTING MATRIX LISTENER");
            mSensorListener = new AccelerometerCompassListener(mContext);
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

        mSensorListener.register();
        mStepDetector.register();
        // If we have user control we might have to change it here
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        mSensorListener.unregister();
        mStepDetector.unregister();
    }


}
