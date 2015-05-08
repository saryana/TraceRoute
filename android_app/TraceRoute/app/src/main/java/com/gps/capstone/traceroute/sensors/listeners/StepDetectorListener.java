package com.gps.capstone.traceroute.sensors.listeners;

import android.app.Notification;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewStepEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by saryana on 4/25/15.
 *
 * Might want to throw this into SensorDataProvider... feels odd to just be posting
 * an event the problem was this was taking from the functionality of what we really
 * wanted the SensorDataProvider to be doing
 */
public class StepDetectorListener extends MySensorListener implements SensorEventListener {
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    // Step detector
    private Sensor mStepDetector;

    /**
     * The step detector will trigger a new event every time it detects a step.
     * The data it broadcasts is a distance and direction
     * @param context Context creating the application
     */
    public StepDetectorListener(Context context) {
        super(context);
        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    /**
     * Registers the listener and the bus for communication. The ReportingMode claims to only
     * work in >21
     */
    @Override
    public void register() {
        mSensorManager.registerListener(this, mStepDetector, Sensor.REPORTING_MODE_SPECIAL_TRIGGER);
        mBus.register(this);
    }

    /**
     * Make sure we don't leak
     */
    @Override
    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    /**
     * Hey we got a step event, lets let the view know
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // This got simplified so much it may not be a bad idea to integrate part of
        // the sensor data provider
        mBus.post(new NewDataEvent(null, EventType.STEP_DETECTED));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
