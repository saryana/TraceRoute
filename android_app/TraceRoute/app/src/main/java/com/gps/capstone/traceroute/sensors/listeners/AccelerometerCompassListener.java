package com.gps.capstone.traceroute.sensors.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;

/**
 * Created by saryana on 4/11/15.
 *
 * The RawSensor manager is essentially a sensor manager that allows us
 * to get get the values from the sensors and pass them on to the DataProvider
 * that will help the state of the data and make sure we are receiving things in
 * the proper order.
 */
public class AccelerometerCompassListener extends MySensorListener implements SensorEventListener {
    // Tag for logging
    private final String TAG = this.getClass().getSimpleName();

    // Might need to put the low pass filter in a util class
    // Used for a low pass filter
    public static float ALPHA = 0.02f;

    // Sensors we need the previous values of

    // Current acceleration values
    private float[] mAccelerometerValues;
    // Current gravity values
    private float[] mGravityValues;

    // Sensors we need for now
    // Gravity sensor
    private Sensor mGravitySensor;
    // Accelerometer sensor
    private Sensor mAccelerationSensor;

    /**
     * Creates a new AccelerometerCompassListener that post evens about new values being received from the
     * accelerometer and gravity sensor for now
     * @param context Context we are being called in
     */
    public AccelerometerCompassListener(Context context) {
        super(context);
        // Might need a better way to update the value on the fly

        ALPHA = Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_alpha), ""+ALPHA));

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mBus = BusProvider.getInstance();

        // Grab and register listeners for the accelerometer and the gravity sensors
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        // Determine the values we have
        if (type == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerValues = lowPass(event.values, mAccelerometerValues);
        } else if (type == Sensor.TYPE_GRAVITY) {
            mGravityValues = lowPass(event.values, mGravityValues);
        } else if (type == Sensor.TYPE_GYROSCOPE) {
            Log.e(TAG, "Something has gone terribly wrong");
            return;
        }

        // If we don't have any new values, we can't compute the orientation and post an event
        if (mAccelerometerValues != null && mGravityValues != null) {
            // Rotation matrix
            float[] R = new float[16];
            // Inclination of the phone
            float[] I = new float[16];

            // Did we get valid values?
            if (SensorManager.getRotationMatrix(R, I, mAccelerometerValues, mGravityValues)) {
                // Do a little pre-processing then post the matrix on the channel
                mBus.post(new NewDataEvent(R, EventType.ROTATION_MATRIX_CHANGE));
            } else {
                Log.e(TAG, "Didn't get information from rotation matrix");
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Currently not used
    }

    /**
     * Registers the sensor's listeners
     */
    public void register() {
        mBus.register(this);
        mSensorManager.registerListener(this, mAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Unregister the sensor listener when we are destroying the activity
     */
    public void unregister() {
        mBus.unregister(this);
        mSensorManager.unregisterListener(this);
    }

    /**
     * Low pass filter to smooth the values according to a specified alpha, a lower alpha means more
     * smoothing
     * @param input New values we received
     * @param previous Old values, null on start
     * @return Now filtered values
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
}
