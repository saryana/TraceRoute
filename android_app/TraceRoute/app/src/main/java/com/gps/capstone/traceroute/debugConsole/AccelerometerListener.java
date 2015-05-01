package com.gps.capstone.traceroute.debugConsole;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

/**
 * Created by saryana on 4/7/15.
 */
public class AccelerometerListener implements SensorEventListener {

    // View that we need to populate
    private RelativeLayout view;

    /**
     * Listener that updates the view
     * @param view View to update
     */
    public AccelerometerListener(RelativeLayout view) {
        this.view = view;
    }

    /**
     * The accelerometer gets an event that has the acceleration in the X, Y, Z plane in m/s^2.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Update the X value
        ((TextView) view.findViewById(R.id.acc_x_val)).setText(String.valueOf(event.values[0]));
        // Update the Y value
        ((TextView) view.findViewById(R.id.acc_y_val)).setText(String.valueOf(event.values[1]));
        // Update the Z value
        ((TextView) view.findViewById(R.id.acc_z_val)).setText(String.valueOf(event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Un-needed method
    }
}

