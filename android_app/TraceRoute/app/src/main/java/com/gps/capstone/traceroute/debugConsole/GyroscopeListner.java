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
public class GyroscopeListner implements SensorEventListener {

    private RelativeLayout view;

    public GyroscopeListner(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
    }

    /**
     * Gyroscope provides the values in rad/sec for each coordinate. Look into roll, pitch, yaw.
     * The values are wiggin out right now.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        ((TextView) view.findViewById(R.id.gyro_x_val)).setText(String.valueOf(event.values[0]));
        ((TextView) view.findViewById(R.id.gyro_y_val)).setText(String.valueOf(event.values[1]));
        ((TextView) view.findViewById(R.id.gyro_z_val)).setText(String.valueOf(event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
