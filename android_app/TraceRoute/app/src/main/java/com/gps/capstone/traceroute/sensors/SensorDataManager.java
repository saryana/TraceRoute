package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.BusProvider;
import com.gps.capstone.traceroute.R;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 4/11/15.
 */
public class SensorDataManager implements SensorEventListener {
    // Used for a low pass filter
    public static float ALPHA = 0.02f;

    // Tag for logging
    private final String TAG = this.getClass().getSimpleName();

    // Context we were created in
    private Context mContext;
    // Sensor manager we are using
    private SensorManager mSensorManager;
    // Current acceleration values
    private float[] mAccelVals;
    // Current gravity values
    private float[] mGravVals;
    // Gravity sensor
    private Sensor mGravitySensor;
    // Accelerometer sensor
    private Sensor mAccelerationSensor;

    /**
     * Creates a new SensorDataManager that post evens about new data being received from the
     * accelerometer and gravity sensor for now
     * @param context Context we are being called in
     */
    public SensorDataManager(Context context) {
        ALPHA = PreferenceManager.getDefaultSharedPreferences(context).getFloat(context.getString(R.string.pref_key_alpha), ALPHA);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mContext = context;
        // Grab and register listeners for the accelerometer and the gravity sensors
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Log.e(TAG, "GRAV SENSOR " + mSensorManager.getSensorList(Sensor.TYPE_GRAVITY).size());
        Log.e(TAG, "ACCEL SENS " + mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size());
        register();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelVals = lowPass(event.values, mAccelVals);
        } else {
            mGravVals = lowPass(event.values, mGravVals);
        }
        // If we don't have any new data, we can't compute the orientation and post an event
        if (mAccelVals != null && mGravVals != null) {
            // Rotation matrix
            float[] R = new float[16];
            // Inclination of the phone
            float[] I = new float[16];

            // Did we get valid data?
            if (SensorManager.getRotationMatrix(R, I, mAccelVals, mGravVals)) {
                BusProvider.getInstance().post(new OrientationChangeEvent(R, 0));
            } else {
                Log.e(TAG, "Didn't get information from rotation matrix");
            }
        }
    }

    /**
     * Low pass filter to smooth the data according to a specified alpha, a lower alpha means more
     * smoothing
     * @param input New data we received
     * @param previous Old values, null on start
     * @return Now filtered data
     */
    private float[] lowPass(float[] input, float[] previous) {
        if (previous == null) {
            return input;
        }
        for (int i = 0; i < input.length; i++) {
            previous[i] = previous[i] + ALPHA * (input[i] - previous[i]);
        }

        return previous;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Currently not used
    }

    /**
     * Registers the sensor's listeners
     */
    public void register() {
        mSensorManager.registerListener(this, mAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Unregister the sensor listener when we are destroying the activity
     */
    public void unregister() {
        mSensorManager.unregisterListener(this);
    }
}
