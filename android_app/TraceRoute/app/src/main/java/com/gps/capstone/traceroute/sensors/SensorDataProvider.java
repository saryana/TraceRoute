package com.gps.capstone.traceroute.sensors;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gps.capstone.traceroute.BusProvider;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.SensorUtil.EventType;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 4/16/15.
 * This will help keep state of what we actually need to change and send as time goes on.
 * For now it will do a lot of book keeping until we can figure out what we actually need.
 */
public class SensorDataProvider {
    private static final float EPSILON = .1f;
    // Nanoseconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    // Tag for logging
    private final String TAG = getClass().getSimpleName();

    // Marker for knowing whether or not to use the gyroscope as our source of data
    private boolean mUseGyroscope;

    // Marker for if we have sent the first rotation matrix
    private boolean mInitialRotationMatrix;

    // Rotation matrix from the accelerometer and gravity sensor data
    private float[] mRotationMatrix;
    // Gyroscope data in rad/s
    private float[] mRawGyroscopeValues;
    // Time we got the gyroscope data
    private long mTimestamp;

    // Bus used for communication
    private Bus mBus;

    // Sensor manager that does slight filtering
    private RawSensorManager mRawSensorManager;


    /**
     * Sets up the basic utilities to make this work
     * @param context Context we got called in
     */
    public SensorDataProvider(Context context) {
        mRawSensorManager = new RawSensorManager(context);
        mUseGyroscope = PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(context.getString(R.string.pref_key_use_gyroscope), false);
        mInitialRotationMatrix = false;
        mBus = BusProvider.getInstance();
        mTimestamp = 0;
    }

    /**
     * HEY WE GOT NEW DATA! Relay it to the proper method so we can see what we need
     * to update
     * @param event
     */
    @Subscribe
    public void onRawDataChange(RawDataEvent event) {
        switch (event.type) {
            case GYROSCOPE_CHANGE:
                updateGyroscopeSate(event.event);
                break;
            case ROTATION_MATRIX_CHANGE:
                updateRotationState(event.values);
                break;
            default:
                Log.e(TAG, "Event that we cannot handle");
        }
    }

    /**
     * Current we are going to keep a copy of the rotation matrix and pass it along
     * if we want to use the rotation matrix or if we need the initial matrix for
     * rendering
     * @param values The rotation matrix
     */
    private void updateRotationState(float[] values) {
        mRotationMatrix = values;

        // If we don't want to use the gyroscope or we haven't sent an initial matrix,
        // then post a new event for the the rotation matrix
        if (!mUseGyroscope || !mInitialRotationMatrix) {
            mInitialRotationMatrix = true;
            mBus.post(new NewDataEvent(mRotationMatrix, EventType.ROTATION_MATRIX_CHANGE));
        }
    }

    /**
     * The gyroscope needs to do a lot of math to get it in terms of just radians and
     * figure out the delta with the previous data with the timestamp.
     * @param values Gyroscope values in rad/s around [x,y,z]
     */
    private void updateGyroscopeSate(SensorEvent event) {

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

            if (mUseGyroscope) {
                mBus.post(new NewDataEvent(deltaRotationMatrix, EventType.DELTA_ROTATION_MATRIX));
            }
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
        this.mUseGyroscope = useGyroscope;

        mBus.register(this);
        // If the user is in control we don't need the sensors
        if (!userControl) {
            Log.i(TAG, "Registering the RawSensorManager");
            mRawSensorManager.register();
        }
    }

    /**
     * Un-register things we no longer care about
     */
    public void unregister() {
        mBus.unregister(this);
        mRawSensorManager.unregister();
    }


}
