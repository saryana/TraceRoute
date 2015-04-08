package com.gps.capstone.traceroute.listners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

/**
 * Created by saryana on 4/7/15.
 *
 * Listens for changes in gravity
 */
public class GravityListener implements SensorEventListener {

    private RelativeLayout view;

    public GravityListener(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
    }

    /**
     * Gravity that is felt in each direction. When at rest it will be the same as the
     * accelerometer
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        ((TextView) view.findViewById(R.id.grav_x_val)).setText(String.valueOf(event.values[0]));
        ((TextView) view.findViewById(R.id.grav_y_val)).setText(String.valueOf(event.values[1]));
        ((TextView) view.findViewById(R.id.grav_z_val)).setText(String.valueOf(event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
