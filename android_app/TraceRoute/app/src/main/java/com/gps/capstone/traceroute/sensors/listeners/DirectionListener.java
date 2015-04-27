package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by saryana on 4/19/15.
 *
 * Notes:
 * acceleration/linear acceleration can be used to determine the direction in terms of
 * figuring out which directions are moving. Adding the values to determine where
 * it is moving in 3d space. However, this doesn't compensate for orientation of the phone.
 * Gravity? can be used to figure out how the phone is standing but I don't think this will get
 * it in world orientation
 */
public class DirectionListener extends MySensorListener implements SensorEventListener {
    // Tag for logging
    private final String TAG = getClass().getSimpleName();
    // Accelerometer that we will be using to get direction
    private Sensor mAccelerometer;
    private Activity mActivity;
    // Threshold for how far a movement we have to go until we register it
    private static final float THRESHOLD = 0.5f;
    // number of samples in rolling average
    private static final int NSAMPLES = 5;

    //    private final Object mLock = new Object();
    private float[] mCurrentRotation;

    // Queue of samples in history
    private Queue<float[]> mSamples;
    // sum of all the data in samples
    private float[] mRunningTotal;

    public DirectionListener(Activity activity) {
        super(activity);
        mActivity = activity;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSamples = new LinkedList<float[]>();
        mRunningTotal = new float[3];
    }

    @Override
    public void register() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(TAG, "Registered the bus");
        mBus.register(this);
    }

    @Override
    public void unregister() {
        mSensorManager.unregisterListener(this);
        mBus.unregister(this);
        ((TextView)mActivity.findViewById(R.id.walking_direction_value)).setText("");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        // Must add an extra dimension to the acceleration vector for multiplication later
        float accelVector[] = {values[0], values[1], values[2], 1};

        // Convert the acceleration vector from phone coordinates to world coordinates
        float[] invertedRotate = new float[16];
        float[] worldSpaceAccel = new float[4];
        if (mCurrentRotation == null) {
            return;
        }
        Matrix.invertM(invertedRotate, 0, mCurrentRotation, 0);
        Matrix.multiplyMV(worldSpaceAccel, 0, invertedRotate, 0, accelVector, 0);

        // If enough samples are in the running average, remove the oldest
        if (mSamples.size() == NSAMPLES) {
            float[] oldestData = mSamples.remove();
            mRunningTotal[0] -= oldestData[0];
            mRunningTotal[1] -= oldestData[1];
            mRunningTotal[2] -= oldestData[2];
        }

        // add most recent data
        mSamples.add(worldSpaceAccel);
        mRunningTotal[0] += worldSpaceAccel[0];
        mRunningTotal[1] += worldSpaceAccel[1];
        mRunningTotal[2] += worldSpaceAccel[2];

        String s = "";
        float average[] = {mRunningTotal[0]/mSamples.size(), mRunningTotal[1]/ mSamples.size(), mRunningTotal[2]/ mSamples.size()};
        if (magnitude(average) > THRESHOLD) {

            s += "{" + average[0] + ", " + average[1] + ", " + average[2] + "}" +"\n";
        }
        //for (int i = 0; i < values.length; i++) {
            /*if (Math.abs(values[i]) > THRESHOLD) {


                if (i == 0) {
                    s += "Movement in X\n";
                } else if (i == 1) {
                    s += "Movement in \t\t\t\tY\n";
                } else {
                    s += "Movement in \t\t\t\t\t\t\t\tZ\n";
                }
            }*/
        //}
        TextView tv = (TextView)mActivity.findViewById(R.id.walking_direction_value);
        tv.setText(s + tv.getText());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Subscribe
    public void onDataChange(NewDataEvent e) {
        switch (e.type) {
            case ROTATION_MATRIX_CHANGE:
                break;
            case DELTA_ROTATION_MATRIX:
                // Replace the current rotation matrix with the new one
                mCurrentRotation = e.values;
                break;
            default:
                Log.e(TAG, "Event that we cannot handle");

        }
    }

    public static float magnitude(float[] v) {
        float magSquared = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        return (float) Math.sqrt(magSquared);
    }
}
