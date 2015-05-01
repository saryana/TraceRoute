package com.gps.capstone.traceroute.debugConsole;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
        float meterAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureValue);
        maxAlt = Math.max(meterAltitude, maxAlt);
        minAlt = Math.min(meterAltitude, minAlt);
        ((TextView) view.findViewById(R.id.altitude_value_meters)).setText(String.valueOf(meterAltitude));
        ((TextView) view.findViewById(R.id.max_alt)).setText("Max alt: " + String.valueOf(maxAlt) + " m");
        ((TextView) view.findViewById(R.id.min_alt)).setText("Min alt: " + String.valueOf(minAlt) + " m");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
