package com.gps.capstone.traceroute.sensors.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.gps.capstone.traceroute.sensors.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;

/**
 * Created by saryana on 4/18/15.
 */
public class GyroscopeListener extends SensorListener implements SensorEventListener {
    // TAG for logging
    private final String TAG = getClass().getSimpleName();
    // filter value for the values we actually need
    private static final float EPSILON = .1f;
    // Nanoseconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    // Gyroscope sensor with units rad/s in x, y, z
    private Sensor mGyroscope;
    // Timestamp for previous time
    private long mTimestamp;

    /**
     * Register the gyroscope sensor
     * @param context Context we are getting called in
     */
    public GyroscopeListener(Context context) {
        super(context);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void register() {
        mBus.register(this);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void unregister() {
        mBus.unregister(this);
        mSensorManager.unregisterListener(this);
    }

    /**
     * If this proves to be too much we should move it elsewhere
     * @param event Event that got fired
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type != Sensor.TYPE_GYROSCOPE) {
            Log.e(TAG, "Something has gone terribly wrong");
            return;
        }
        float[] deltaRotationVector = null;
        if (mTimestamp != 0) {
            final float dT = (event.timestamp - mTimestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector = new float[4];
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        // Update the new timestamp
        mTimestamp = event.timestamp;
        if (deltaRotationVector != null) {
            float[] deltaRotationMatrix = new float[9];
            // transform the new quaternion to a matrix for the grapics to use
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

            mBus.post(new NewDataEvent(deltaRotationMatrix, EventType.DELTA_ROTATION_MATRIX));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
