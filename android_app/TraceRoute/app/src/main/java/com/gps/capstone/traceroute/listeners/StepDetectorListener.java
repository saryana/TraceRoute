package com.gps.capstone.traceroute.listeners;

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
 * Used to detect steps
 */
public class StepDetectorListener implements SensorEventListener {
    private int steps;
    private RelativeLayout view;

    public StepDetectorListener(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
        steps = 0;
    }

    /**
     * The step detector gets fired every time a user takes a step. This is probably going to
     * be more useful than the step counter since, the step counter accumulates as the day goes
     * on, but we can start a clean slate with this. The event doesn't provide any information
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        ((TextView) view.findViewById(R.id.steps_detect_val)).setText(String.valueOf(steps++));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
