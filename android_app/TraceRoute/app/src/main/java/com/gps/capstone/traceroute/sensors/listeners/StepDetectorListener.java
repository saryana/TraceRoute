package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Notification;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewStepEvent;
import com.squareup.otto.Subscribe;

import java.util.Arrays;

/**
 * Created by saryana on 4/25/15.
 */
public class StepDetectorListener extends MySensorListener implements SensorEventListener {
    private static final float OPENGL_SCALE = .0118f;
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    // Step detector
    private Sensor mStepDetector;
    // Height in inches
    private int mHeight;
    // total distance for now
    private int mTotal;
    private Notification notification;
    // We need to keep track of the old step
    // and based off of the new step and direction
    // we will calculate a new step location that we will send
    // the view
    private float[] mOldStepLocation;

    /**
     * The step detector will trigger a new event every time it detects a step.
     * The data it broadcasts is a distance and direction
     * @param context Context creating the application
     */
    public StepDetectorListener(Context context) {
        super(context);
        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mOldStepLocation = new float[3];
        mOldStepLocation[1] = 0.1f;
        mOldStepLocation[2] = 0.1f;
    }

    /**
     * Registers the listener and the bus for communication. The ReportingMode claims to only
     * work in >21
     */
    @Override
    public void register() {
        mSensorManager.registerListener(this, mStepDetector, Sensor.REPORTING_MODE_SPECIAL_TRIGGER);
        mBus.register(this);
        mHeight = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_total_height_in), 0);
        mTotal = 0;
        if (mHeight == 0) {
            Toast.makeText(mContext, "YOU MUST ADD YOUR HEIGHT IN THE SETTINGS PANEL", Toast.LENGTH_LONG).show();
        } else {
            Log.i(TAG, "HEIGHT " + mHeight);
        }
    }

    /**
     * Make sure we don't leak
     */
    @Override
    public void unregister() {
        mSensorManager.unregisterListener(this);
        mBus.unregister(this);
    }

    /**
     * We may want to keep track of the distance here or in the view
     * @param event
     */
    @Subscribe
    public void onDataChange(NewDataEvent event) {
        if (event.type == EventType.DIRECTION_CHANGE) {
            // set the direction here so we can grab the direction
            // and tag on the distance
            Log.i(TAG, "DIRECTION CHANGE");
        }
    }

    /**
     * Hey we got a step event, lets let the view know
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mHeight == 0) {
            Log.e(TAG, "User height not defined");
        } else {
            mTotal += mHeight * .41;

            mNotificationManager.notify(1, getNotification());
        }
        // Lets calculate the the distance from the old/previous
        // step with this new info to get the new event
        float[] newLocation = new float[3];
        newLocation[0] = mOldStepLocation[0] - mHeight*.41f*OPENGL_SCALE;
        newLocation[1] = mOldStepLocation[1];
        newLocation[2] = mOldStepLocation[2];
        Log.i(TAG, "OLD " + Arrays.toString(mOldStepLocation) + " NEW " + Arrays.toString(newLocation));
        mBus.post(new NewStepEvent(mOldStepLocation, newLocation));
        mOldStepLocation = newLocation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * @return Gets a notification with the updated value
     */
    public Notification getNotification() {
        return mBuilder.setContentTitle("Distance")
                        .setContentText(String.format("Distance traveled: %d ft %d inches", mTotal / 12, mTotal % 12))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .build();
    }
}
