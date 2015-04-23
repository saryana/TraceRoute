package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.listeners.GyroscopeListener;
import com.gps.capstone.traceroute.sensors.listeners.MySensorListener;
import com.gps.capstone.traceroute.sensors.listeners.RotationMatrixListener;

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
    // Context we got called from
    private Context mContext;
//    private SensorSource mSource;


    /**
     * Sets up the basic utilities to make this work
     * @param context Context we got called in
     */
    public SensorDataProvider(Context context) {
        mContext = context;
        mUseGyroscope = PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(context.getString(R.string.pref_key_use_gyroscope), true);
        determineListener();
    }

    private void determineListener() {
        if (mUseGyroscope) {
            Log.i(TAG, "SETTING GYROSCOPE LISTENER");
            mSensorListener = new GyroscopeListener(mContext);
        } else {
            Log.i(TAG, "SETTING MATRIX LISTENER");
            mSensorListener = new RotationMatrixListener(mContext);
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
        } else {
            mSensorListener.register();
        }

        // If the user is in control we don't need the sensors
        if (!userControl) {
            Log.i(TAG, "Registering the RotationMatrixListener");
            mSensorListener.register();
        }
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        mSensorListener.unregister();
    }


}
