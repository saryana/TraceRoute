package com.gps.capstone.traceroute.sensors.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.gps.capstone.traceroute.sensors.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.squareup.otto.Produce;

/**
 * Created by saryana on 4/18/15.
 */
public class GyroscopeListener extends MySensorListener implements SensorEventListener {
    /**
     * The threshold that indicates an outlier of the rotation vector. If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1,
     * if they are exactly the same) the system falls back to the gyroscope values only and just ignores the
     * rotation vector.
     *
     * This value should be quite high (> 0.7) to filter even the slightest discrepancies that causes jumps when
     * tiling the device. Possible values are between 0 and 1, where a value close to 1 means that even a very small
     * difference between the two sensors will be treated as outlier, whereas a value close to zero means that the
     * almost any discrepancy between the two sensors is tolerated.
     */
    private static final float OUTLIER_THRESHOLD = 0.85f;

    /**
     * The threshold that indicates a massive discrepancy between the rotation vector and the gyroscope orientation.
     * If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1, if
     * they are exactly the same), the system will start increasing the panic counter (that probably indicates a
     * gyroscope failure).
     *
     * This value should be lower than OUTLIER_THRESHOLD (0.5 - 0.7) to only start increasing the panic counter,
     * when there is a huge discrepancy between the two fused sensors.
     */
    private static final float OUTLIER_PANIC_THRESHOLD = 0.75f;

    // TAG for logging
    private final String TAG = getClass().getSimpleName();
    // filter value for the values we actually need
    private static final float EPSILON = .1f;
    // Nanoseconds to seconds
    private static final float NS2S = 1.0f / 1000000000.0f;

    // Gyroscope sensor with units rad/s in x, y, z
    private Sensor mGyroscope;
    // Rotation vector for correcting
    private Sensor mRotationVector;
    // Store the rotation vector data for when the gyroscope drifts
    private float[] mRotationVectorValues;
    // If we have initialized the initial vector
    private boolean mInitialized;
    // Timestamp for previous time
    private long mTimestamp;
    // Store the current quat
    private float[] mCurrentQuat;
    // Panic counter
    private int mPanic;

    /**
     * Register the gyroscope sensor
     * @param context Context we are getting called in
     */
    public GyroscopeListener(Context context) {
        super(context);
        Log.i(TAG, "IN GYRO");
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationVectorValues = new float[4];
        mInitialized = false;
        mCurrentQuat = null;
    }

    @Override
    public void register() {
        Log.i(TAG, "REGISTER GYROSCOPE");
        mBus.register(this);
        // LOOK INTO
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mPanic = 0;
    }

    @Override
    public void unregister() {
        mBus.unregister(this);
        mSensorManager.unregisterListener(this);
        mInitialized = false;
    }

    /**
     * If this proves to be too much we should move it elsewhere
     * @param event Event that got fired
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeData(event);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVectorData(event);
                break;
            default:
                Log.e(TAG, "Something has gone terribly wrong");
        }
    }

    /**
     * For the rotation vector, we only want to submit data when we need to initialize
     * and when the gyroscope has begun to drift.
     * @param event Event that got fired
     */
    private void rotationVectorData(SensorEvent event) {
        mRotationVectorValues = event.values;
        if (!mInitialized) {
            mInitialized = true;
            mCurrentQuat = mRotationVectorValues;
            float[] deltaRotationMatrix = new float[16];
            // transform the new quaternion to a matrix for the graphics to use
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, mRotationVectorValues);

            mBus.post(new NewDataEvent(deltaRotationMatrix, EventType.DELTA_ROTATION_MATRIX));
        }
    }

    /**
     * We need to process the gyroscope data a little by filtering out values that aren't
     * large enough to trigger an event and get it into terms that we can actually use
     * @param event Event that got fired
     */
    private void gyroscopeData(SensorEvent event) {
        float[] deltaRotationVector = null;
        // We can't compute a delta without an initialized timestamp
        // and we are now depending ont he rotation vector values
        if (mTimestamp != 0 && mCurrentQuat != null) {
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

            // Move the current quat by the rotation we got
            multiplyByQuat(mCurrentQuat, mCurrentQuat, deltaRotationVector);

            float dotProd = Math.abs(dotProduct(mCurrentQuat, mRotationVectorValues));
            // Is the dot product outside of our threshold
            if (dotProd < OUTLIER_THRESHOLD) {
                if (dotProd < OUTLIER_PANIC_THRESHOLD) {
                    Log.e(TAG, "PANIC");
                    mPanic++;
                }
            } else {
                slerp(mCurrentQuat, mRotationVectorValues, mCurrentQuat, omegaMagnitude * .01f);
                mPanic = 0;
            }
            if (mPanic > 60) {
                Log.e(TAG, "PANNNNNNNNNIC");
                if (omegaMagnitude < 3) {
                    mPanic = 0;
                    mCurrentQuat = mRotationVectorValues;
                    Log.d(TAG, "STILL OK");
                } else {
                    Log.e(TAG, "WE REALLY FUCKED UP");
                }
            }
            float[] deltaRotationMatrix = new float[16];
            // transform the new quaternion to a matrix for the graphics to use
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, mCurrentQuat);
            mBus.post(new NewDataEvent(deltaRotationMatrix, EventType.DELTA_ROTATION_MATRIX));
        }

        // Before this occurs we need to do a lot more checking of things

        // Update the new timestamp
        mTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    /**
     * Multiply this quaternion by the input quaternion and store the result in the out quaternion
     *
     * @param output the output of the multiplication
     * @param current the current state of the quat
     * @param input the new input data
     */
    public static void multiplyByQuat(float[] output, float[] current, float[] input) {
        float[] inputCopy = new float[4];
        if (input != output) {
            output[3] = (current[3] * input[3] - current[0] * input[0] - current[1] * input[1] - current[2]
                    * input[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output[0] = (current[3] * input[0] + current[0] * input[3] + current[1] * input[2] - current[2]
                    * input[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output[1] = (current[3] * input[1] + current[1] * input[3] + current[2] * input[0] - current[0]
                    * input[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output[2] = (current[3] * input[2] + current[2] * input[3] + current[0] * input[1] - current[1]
                    * input[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        } else {
            inputCopy[0] = input[0];
            inputCopy[1] = input[1];
            inputCopy[2] = input[2];
            inputCopy[3] = input[3];

            output[3] = (current[3] * inputCopy[3] - current[0] * inputCopy[0] - current[1]
                    * inputCopy[1] - current[2] * inputCopy[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output[0] = (current[3] * inputCopy[0] + current[0] * inputCopy[3] + current[1]
                    * inputCopy[2] - current[2] * inputCopy[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output[1] = (current[3] * inputCopy[1] + current[1] * inputCopy[3] + current[2]
                    * inputCopy[0] - current[0] * inputCopy[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output[2] = (current[3] * inputCopy[2] + current[2] * inputCopy[3] + current[0]
                    * inputCopy[1] - current[1] * inputCopy[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        }
    }

    /**
     * @return Dot product between to quats
     */
    public float dotProduct(float[] q1, float[] q2) {
        return q1[0] * q2[0] + q1[1] * q2[1] + q1[2] * q2[2] + q1[3] * q2[3];
    }

    /**
     * Get a linear interpolation between this quaternion and the input quaternion, storing the result in the output
     * quaternion.
     *
     * @param q1 The quaternion to be slerped with this quaternion.
     * @param q2 The quaternion to get the interpolation from
     * @param output The quaternion to store the result in.
     * @param t The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t will bring rotation
     *            closer to the input quaternion.
     */
    public void slerp(float[] q1, float[] q2, float[] output, float t) {
        float[] bufferQuat = new float[4];
        float cosHalftheta = dotProduct(q1, q2);

        if (cosHalftheta < 0) {
            cosHalftheta = -cosHalftheta;
            bufferQuat[0] = (-q2[0]);
            bufferQuat[1] = (-q2[1]);
            bufferQuat[2] = (-q2[2]);
            bufferQuat[3] = (-q2[3]);
        } else {
            bufferQuat = q2;
        }
        if (Math.abs(cosHalftheta) >= 1.0) {
            output[0] = (q1[0]);
            output[1] = (q1[1]);
            output[2] = (q1[2]);
            output[3] = (q1[3]);
        } else {
            double sinHalfTheta = Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
            double halfTheta = Math.acos(cosHalftheta);

            double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
            double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

            //Calculate Quaternion
            output[3] = ((float) (q1[3] * ratioA + bufferQuat[3] * ratioB));
            output[0] = ((float) (q1[0] * ratioA + bufferQuat[0] * ratioB));
            output[1] = ((float) (q1[1] * ratioA + bufferQuat[1] * ratioB));
            output[2] = ((float) (q1[2] * ratioA + bufferQuat[2] * ratioB));

            //}
        }
    }
}
