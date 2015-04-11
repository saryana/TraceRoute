package com.gps.capstone.traceroute.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

/**
 * Created by saryana on 4/7/15.
 *
 * Barometer listener
 */
public class BarometerListener implements SensorEventListener {

    private RelativeLayout view;
    // max altitude we have seen so far
    private float maxAlt;
    // min altitude we have seen so far
    private float minAlt;

    public BarometerListener(RelativeLayout relativeLayout) {
        this.view = relativeLayout;
        maxAlt = -Float.MAX_VALUE;
        minAlt = Float.MAX_VALUE;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressureValue = event.values[0];
        ((TextView) view.findViewById(R.id.pressure_value)).setText(String.valueOf(pressureValue));
        float feetAltitude = (float)Math.pow(pressureValue/1013.25, .190284);
        feetAltitude = 1 - feetAltitude;
        feetAltitude *= 135366.45;
        maxAlt = Math.max(feetAltitude, maxAlt);
        minAlt = Math.min(feetAltitude, minAlt);
        float meterAltitude = (float) (feetAltitude * .3048);
        ((TextView) view.findViewById(R.id.altitude_value_ft)).setText(String.valueOf(feetAltitude));
        ((TextView) view.findViewById(R.id.altitude_value_meters)).setText(String.valueOf(meterAltitude));
        ((TextView) view.findViewById(R.id.max_alt)).setText("Max alt: " + String.valueOf(maxAlt) + " feet");
        ((TextView) view.findViewById(R.id.min_alt)).setText("Min alt: " + String.valueOf(minAlt) + " feet");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
