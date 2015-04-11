package com.gps.capstone.traceroute;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

import java.util.Arrays;

/**
 * Created by saryana on 4/11/15.
 */
public class SensorDataManager implements SensorEventListener {

    private final String TAG = this.getClass().getSimpleName();

    // Bus system used for communication
    private Bus mBus;
    // Context we are created
    private Context mContext;
    // Sensor manager we are using
    private SensorManager mSensorManager;
    // Accelerometer
    private Sensor mAccel;
    // Magnetic field
    private Sensor mGrav;
    public float[] R;
    public float[] I;
    private float[] mGravVals;
    private float[] mAccelVals;


    public SensorDataManager(Context context, Bus bus) {
        mBus = bus;
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mGravVals = null;
        mAccelVals = null;

        R = new float[9];
        I = new float[9];

        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelVals = event.values;
        } else {
            mGravVals = event.values;
        }

        if (mAccelVals != null && mGravVals != null) {
            if (SensorManager.getRotationMatrix(R, I, mAccelVals, mGravVals)) {
                float[] orientation = new float[3];
                orientation = SensorManager.getOrientation(R, orientation);
                Log.i(TAG, "I " + Arrays.toString(I));
                Log.i(TAG, "R " + Arrays.toString(R));
                Log.i(TAG, "Orientation " + Arrays.toString(orientation));
                mBus.post(new NewRotationVectorEvent(I, 0));
//                mBus.post(new NewRotationVectorEvent(R, 1));
//                mBus.post(new NewRotationVectorEvent(orientation, 3));
            } else {
                Log.e(TAG, "No data from rotation matrix");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
