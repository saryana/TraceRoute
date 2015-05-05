package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Notification;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.SensorUtil.Direction;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;
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
    // Threshold for how far a movement we have to go until we register it
    private static final float THRESHOLD = 1.0f;
    // Threshold angle used to detect when two acceleration vectors differ too much in direction
    private static final float THRESHOLD_ANGLE = 20.0f;
    // number of samples in rolling average
    private static final int NSAMPLES = 5;
    // number of cardinal directions
    private static final int NUMCARDINAL = 8;
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

    // Used when a pulse in acceleration is detected
    private boolean mPulseBegan;
    // This is the direction of the current pulse
    private float[] mPulseDirection;

    // direction we are currently headed
    private int mMovementSector;
    // are we moving?
    private boolean mMoving;

    public DirectionListener(Context context) {
        super(context);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Android claims in java 1.7 the diamond operator works
        mSamples = new LinkedList<>();
        mRunningTotal = new float[3];
        mOldAccel = new float[4];
        mVelocity = new float[3];
        mHeading = 0;
        mPulseBegan = false;
        mPulseDirection = new float[4];
        mMoving = false;
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
        // Do we want to use the accelerometer?
        if (SensorDataProvider.USE_ACCELERATION) {
            directionFromAcceleration(worldSpaceAccel);
        // Lets use velocity
        } else {
            directoinFromVelocity(mTimestamp, worldSpaceAccel);
        }
        // If we find this inaccurate we can put restrictions on the user and
        // and possibly use the compass. For now the restrictions include


        // TODO detect when possibly stopped?
        // TODO filter out insignificant accelerations


//        String s = "";
//        if (magnitude(average) > THRESHOLD) {
//            mHeading = vectorToDirection(average);
            //s += "Heading: " + vectorToDirection(average) + "{" + average[0] + ", " + average[1] + ", " + average[2] + "}" + "\n";
            //s += "{" + average[0] + ", " + average[1] + ", " + average[2] + "}" +"\n";
//        }
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

        // TODO KEITH this is where you can throw things into the notification bar
        // so we can be testing in the OpenGL part of the app and not the debug console.
        // As a tip if you want a new notification change the first parameter to something new.
        // 1 is used for step stuff, 2 for direction....
//        mNotificationManager.notify(2, getNotification());
//        mNotificationManager.notify(3, getMovementNotification());
    }

    /**
     * Essentially integrate acceleration to get velocity to determine direction
     * @param worldSpaceAccel Acceleration in world space location
     */
    private void directoinFromVelocity(float timestamp, float[] worldSpaceAccel) {
        // Using the velocity derived from acceleration for direction
        // calculate change in velocity
        if (mTimestamp != 0) {
            // interpolate the change in acceleration to reduce error
            float[] averageAccel = new float[3];
            averageAccel[0] = /*mOldAccel[0] +*/ (worldSpaceAccel[0] + mOldAccel[0]) / 2;
            averageAccel[1] = /*mOldAccel[1] +*/ (worldSpaceAccel[1] + mOldAccel[1]) / 2;
            averageAccel[2] = /*mOldAccel[2] +*/ (worldSpaceAccel[2] + mOldAccel[2]) / 2;

            float deltaT = (timestamp - mTimestamp) * NS2S;

            mVelocity[0] += averageAccel[0] * deltaT;
            mVelocity[1] += averageAccel[1] * deltaT;
            mVelocity[2] += averageAccel[2] * deltaT;
        }
    }

    /**
     * Determines the direction based off of the linear acceleration sensor (acceleration - gravity)
     * by determining an average in a given pulse
     * @param worldSpaceAccel Acceleration in world space location
     */
    private void directionFromAcceleration(float[] worldSpaceAccel) {
        // Using acceleration for direction

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

        // Compute the current average acceleration
        float average[] = {mRunningTotal[0] / mSamples.size(), mRunningTotal[1] / mSamples.size(), mRunningTotal[2] / mSamples.size()};

        if (mPulseBegan) {
            // analyse the average to see if it belongs to the current pulse
            if (magnitude(average) > THRESHOLD) {
                // check if the average is going in the same direction as the initial pulse direction
                if (angleBetweenVectors(average, mPulseDirection) > THRESHOLD_ANGLE) {
                    mPulseBegan = false;
                }
            } else {
                // magnitude is below threshold. the pulse has ended
                mPulseBegan = false;
            }

            if (!mPulseBegan) {
                // The pulse has just ended, figure out the direction of the pulse
                float pulseDirection = vectorToDirection(mPulseDirection);
                int pulseSector = directionToSector(pulseDirection, NUMCARDINAL);
                if (mMoving) {
                    // If we are currently moving in a particular direction
                    int currentHeadingSector = directionToSector(mHeading, NUMCARDINAL);

                    // check if the pulses are in opposite directions
                    if (Math.abs(currentHeadingSector - pulseSector) == NUMCARDINAL/2) {
                        mMoving = false;
                    } else {
                        mHeading = pulseDirection;
                        mMovementSector = pulseSector;
                    }
                } else {
                    mHeading = pulseDirection;
                    mMoving = true;
                    mMovementSector = pulseSector;
                }
            }
            //TODO average out the direction of the pulse?
        } else if (magnitude(average) > THRESHOLD)  {
            // This is the beginning of a pulse
            mPulseBegan = true;
            mPulseDirection = average;
        }
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

    /**
     * This need to be moved into a util class. I believe we are using magnitude
     * in the OpenGL files as well
     */
    public static float magnitude(float[] v) {
        float magSquared = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        return (float) Math.sqrt(magSquared);
    }

    // Converts the x and y components in given vector {x,y,z} to compass degrees
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

    // Converts the given direction (in compass degrees) to a sector on a compass.
    // Must specify the number of sectors the compass has. Using values other than 4, 8, 16, 32 do not map to cardinal directions well.
    // The returned sector number has a 0 based index
    public static int directionToSector(float direction, int sectors) {
        // figure out the size of each sector
        float sectorSize = 360f / sectors;

        // Shift the direction value over by half a sector because sectors on a compass
        float shiftedDirection = direction + sectorSize / 2;

        // direction that map to sector 0 with high degree will go over 360 so wrap it back
        shiftedDirection %= 360;
        return (int) (shiftedDirection / sectorSize);
    }

    // Calculates the angle between the two given vectors. returned value is in degrees
    public static float angleBetweenVectors(float[] v1, float[] v2) {
        float v1Dotv2 = v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
        float cosineTheta = v1Dotv2 / (magnitude(v1) * magnitude(v2));
        if (cosineTheta > 1) {
            cosineTheta = 1;
        } else if (cosineTheta < -1) {
            cosineTheta = -1;
        }
        double theta = (float) Math.acos(cosineTheta);
        return (float) (theta / Math.PI * 180);
    }

    public Notification getNotification() {
        return mBuilder.setContentTitle("Heading")
                .setContentText("Heading Direction " + mHeading)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public Notification getMovementNotification() {
        return mBuilder.setContentTitle("Movement")
                .setContentText("Moving: " + mMoving + " Direction: " + Direction.values()[mMovementSector])
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
