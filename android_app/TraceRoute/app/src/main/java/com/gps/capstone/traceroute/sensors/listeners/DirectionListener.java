package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Activity;
import android.app.Notification;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
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
    // Nano seconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    //    private final Object mLock = new Object();
    private float[] mCurrentRotation;

    // Queue of samples in history
    private Queue<float[]> mSamples;
    // sum of all the data in samples
    private float[] mRunningTotal;
    // previous acceleration, used to calculate change in acceleration
    private float[] mOldAccel;
    // Running sum of velocity from accelerometer data
    private float[] mVelocity;
    // Timestamp used to calculate change in velocity
    private long mTimestamp;
    public float mHeading;

    public DirectionListener(Activity activity) {
        super(activity);
        mActivity = activity;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSamples = new LinkedList<float[]>();
        mRunningTotal = new float[3];
        mOldAccel = new float[4];
        mVelocity = new float[3];
        mHeading = 0;
    }

    @Override
    public void register() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
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


        float average[] = {mRunningTotal[0] / mSamples.size(), mRunningTotal[1] / mSamples.size(), mRunningTotal[2] / mSamples.size()};
        // calculate change in velocity
        if (mTimestamp != 0) {
            // interpolate the change in acceleration to reduce error
            float[] averageAccel = new float[3];
            averageAccel[0] = /*mOldAccel[0] +*/ (worldSpaceAccel[0] + mOldAccel[0]) / 2;
            averageAccel[1] = /*mOldAccel[1] +*/ (worldSpaceAccel[1] + mOldAccel[1]) / 2;
            averageAccel[2] = /*mOldAccel[2] +*/ (worldSpaceAccel[2] + mOldAccel[2]) / 2;

            float deltaT = (event.timestamp - mTimestamp) * NS2S;

            mVelocity[0] += averageAccel[0] * deltaT;
            mVelocity[1] += averageAccel[1] * deltaT;
            mVelocity[2] += averageAccel[2] * deltaT;
        }

        // TODO detect when possibly stopped?
        // TODO filter out insignificant accelerations


        String s = "";
        if (magnitude(mVelocity) > THRESHOLD) {
            s += "Heading: " + vectorToDirection(mVelocity) + "{" + mVelocity[0] + ", " + mVelocity[1] + ", " + mVelocity[2] + "}" + "\n";
            //s += "{" + average[0] + ", " + average[1] + ", " + average[2] + "}" +"\n";
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
        // Update the new timestamp
        mTimestamp = event.timestamp;
        mOldAccel = worldSpaceAccel;

        //TextView tv = (TextView)mActivity.findViewById(R.id.walking_direction_value);
        //tv.setText(s + tv.getText());
        Log.i(TAG, s);
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

    //
    public static float vectorToDirection(float[] v) {
        //float magnitude = magnitude(v);
        //float normalized[] = {v[0]/magnitude, v[1]/magnitude, v[2]/magnitude};

        // find arctan(-x/y) instead of arctan(y/x), this makes it easier to convert to cardinal direction
        double angleRad = Math.atan2(-v[0], v[1]);
        double angleDegree = angleRad * 180 / Math.PI;

        if (angleDegree < 0) {
            angleDegree = -angleDegree;
        } else if (angleDegree > 0) {
            angleDegree = 360 - angleDegree;
        }

        return (float) angleDegree;
    }

    public Notification getNotification() {
        return mBuilder.setContentTitle("Heading")
                .setContentText("Heading Direction " + mHeading)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
