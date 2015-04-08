package com.gps.capstone.traceroute.listners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

import java.util.Arrays;

/**
 * Created by saryana on 4/7/15.
 *
 * Used to detect the number of steps in a day
 */
public class StepCounterListener implements SensorEventListener {

    private RelativeLayout view;

    public StepCounterListener(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
    }

    /**
     * Gets fired every time the user takes a step and accumulates the value as the day goes on.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        ((TextView) view.findViewById(R.id.steps_val)).setText(String.valueOf(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
