package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Notification;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewStepEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 4/25/15.
 */
public class StepDetectorListener extends MySensorListener implements SensorEventListener {
    private static final float OPENGL_SCALE = .0118f;
    private static final float STRIDE_RATIO = .41f;
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
    // Heading value in radians
    private float mHeading;
    // The current altitude recorded by the step
    private float mAltitude;
    private float mInitialAltitude;

    /**
     * The step detector will trigger a new event every time it detects a step.
     * The data it broadcasts is a distance and direction
     * @param context Context creating the application
     */
    public StepDetectorListener(Context context) {
        super(context);
        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mOldStepLocation = new float[3];
        mInitialAltitude = 0;
        mAltitude = 0;
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
            // Update the current heading we have
            mHeading = event.values[0];
        } else if (event.type == EventType.ALTITUDE_CHANGE) {
            // Write the current altitude so that it gets
            // written on the next step
            mAltitude = event.values[0];
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
            mTotal += mHeight * STRIDE_RATIO;

//            mNotificationManager.notify(1, getNotification());
        }
        // Lets calculate the the distance from the old/previous
        // step with this new info to get the new event
        float[] newLocation = new float[3];
        // TODO integrate the direction into this. For now we can just do NESW and change just xyz
        // accordingly

        // Get unit vector of heading
        float[] xy = getVectorFromAngle(mHeading);

        // scale the vector to stride length and add to old location
        float strideLength = mHeight * STRIDE_RATIO * OPENGL_SCALE;
        newLocation[0] = mOldStepLocation[0] + xy[0] * strideLength;
        newLocation[1] = mOldStepLocation[1] + xy[1] * strideLength;
        // Lets set our first step as our initial altitude.
        if (mInitialAltitude == 0) {
            mInitialAltitude = mAltitude;
        }
        newLocation[2] = (mAltitude - mInitialAltitude) * OPENGL_SCALE;
        Log.e("TAG", newLocation[2] + "");

        // TODO: The data is still pretty jump and that is probably based off of the
        // compass as our form of heading which isn't super accurate... There is also
        // a weird issue when walking in a half circle and walking back it doesn't do things
        // in the proper return route, Compass reversed somehow? not getting negative?
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


    // TODO this method might not be correct. I forgot that cardinal direction degrees is different from trig degrees
    /**
     * Converts the given direction to a unit vector in the xy plane.
     * @param theta the direction to convert
     * @return an array of floats representing vector {x, y}
     */
    public static float[] getVectorFromAngle(float theta) {
        // convert cardinal angles to trig angles
        double trigTheta;
        if (theta >= 0 && theta <= Math.PI/2) {
            trigTheta = Math.PI/2 - theta;
        } else if (theta > Math.PI/2 && theta <= 3*Math.PI/2) {
            trigTheta = -(theta - Math.PI/2);
        } else {
            trigTheta = Math.PI - (theta-3*Math.PI/2);
        }

        // calculate a unit vector in xy plane that points in the given direction
        double x = Math.sin(trigTheta);
        double y = Math.cos(trigTheta);

        return new float[]{(float) x, (float) y};
    }
}
