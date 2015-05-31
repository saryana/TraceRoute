package com.gps.capstone.traceroute.sensors.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;

/**
 * Created by saryana on 5/3/15.
 */
public class CompassListener extends MySensorListener implements SensorEventListener {

    private Sensor mMagnetic;
    private Sensor mGravity;
    private float[] mGravValues;
    private float[] mMagneticValues;
    private String mCurrentDir;

    /**
     * Dummy class for direction that will use the compass and put the restriction on users
     * to be holding their phone so we have the proper heading.
     */
    public CompassListener(Context context) {
        super(context);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravValues = null;
        mMagneticValues = null;
        mCurrentDir = "";
    }

    @Override
    public void register() {
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticValues = event.values;
        } else {
            mGravValues = event.values;
            if (mMagneticValues != null) {
                float[] rotationMatrix = new float[9];
                SensorManager.getRotationMatrix(rotationMatrix, null, mGravValues, mMagneticValues);

                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                // Posting the radian value we are getting
                mBus.post(new NewDataEvent(new float[]{orientation[0], orientation[2]}, EventType.DIRECTION_CHANGE));

//                // TODO NOTE TO KEITH: It may not be an idea to sector it up since the data readings still
//                // jump around a bit we can have a pretty small sector size, but i'm not sure if that
//                // actually helps anything
//                mBus.post(new NewDataEvent(new float[]{heading}, EventType.DIRECTION_CHANGE));
            }
        }
    }

    private String headingToDir(float heading) {
        String dir;
        if (heading >= 337.5 || heading < 22.5) {
            dir = "NORTH";
        } else if (heading >= 22.5f && heading < 67.5f) {
            dir = "NORTH WEST";
        } else if (heading >= 67.5 && heading < 112.5) {
            dir = "WEST";
        } else if (heading >= 112.5 && heading < 157.5) {
            dir = "SOUTH WEST";
        } else if (heading >= 157.5 && heading < 202.5) {
            dir = "SOUTH";
        } else if (heading >= 202.5 && heading < 247.5) {
            dir = "SOUTH EAST";
        } else if (heading >= 247.5 && heading < 292.5) {
            dir = "EAST";
        } else if (heading >= 292.5 && heading < 337.5) {
            dir = "NORTH EAST";
        } else {
            dir = "SEAN FUCKED UP";
        }
        return dir;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
