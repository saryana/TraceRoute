package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

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
    private static final float THRESHOLD = 2f;

    public DirectionListener(Activity activity) {
        super(activity);
        mActivity = activity;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void register() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
        String s = "";
        for (int i = 0; i < values.length; i++) {
            if (Math.abs(values[i]) > THRESHOLD) {
                if (i == 0) {
                    s += "Movement in X\n";
                } else if (i == 1) {
                    s += "Movement in \t\t\t\tY\n";
                } else {
                    s += "Movement in \t\t\t\t\t\t\t\tZ\n";
                }
            }
        }
        TextView tv = (TextView)mActivity.findViewById(R.id.walking_direction_value);
        tv.setText(s + tv.getText());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
