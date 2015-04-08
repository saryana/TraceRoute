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
 */
public class LinearAccelerationListener implements SensorEventListener {

    private RelativeLayout view;

    public LinearAccelerationListener(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
    }

    /**
     * acceleration = gravity + linear-acceleration
     *
     * So this gives us the acceleration felt by the device so this might be better than accelerometer
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        ((TextView) view.findViewById(R.id.lin_acc_x_val)).setText(String.valueOf(event.values[0]));
        ((TextView) view.findViewById(R.id.lin_acc_y_val)).setText(String.valueOf(event.values[1]));
        ((TextView) view.findViewById(R.id.lin_acc_z_val)).setText(String.valueOf(event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
