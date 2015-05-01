package com.gps.capstone.traceroute.debugConsole;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;

import java.util.Arrays;

/**
 * Created by saryana on 4/11/15.
 */
public class DirectionListener implements SensorEventListener {

    private RelativeLayout mView;

    public DirectionListener(RelativeLayout viewById) {
        mView = viewById;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Is this for the rotation vector?
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            ((TextView) (mView.findViewById(R.id.direction_vector))).setText("DV: " + Arrays.toString(event.values));
        // Is this the geomagnetic rotation vector?
        } else {
            ((TextView) (mView.findViewById(R.id.geomagnetic_direction_vector))).setText("GDV: " + Arrays.toString(event.values));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
