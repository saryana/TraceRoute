package com.gps.capstone.traceroute.sensors.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;

/**
 * Created by saryana on 5/6/15.
 */
public class AltitudeListener extends MySensorListener implements SensorEventListener {

    // Barometric sensor for elevation
    private Sensor mBarometricSensor;

    public AltitudeListener(Context context) {
        super(context);
        mBarometricSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void register() {
        super.register();
        mSensorManager.registerListener(this, mBarometricSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void unregister() {
        super.unregister();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Pressure in mbars
        float pressure = event.values[0];
        // Altitude in meters
        float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
        // Lets convert it to feet since that is what we are using for the height
        altitude *= 3.28084;
        mBus.post(new NewDataEvent(new float[]{altitude}, EventType.ALTITUDE_CHANGE));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
