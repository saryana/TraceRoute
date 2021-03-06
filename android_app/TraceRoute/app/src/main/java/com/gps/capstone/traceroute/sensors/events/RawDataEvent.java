package com.gps.capstone.traceroute.sensors.events;

import android.hardware.SensorEvent;

import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;

/**
 * Created by saryana on 4/16/15.
 */
public class RawDataEvent {
    // type of values
    public EventType type;
    // Event that happened
    public SensorEvent event;
    // Values that we will need
    public float[] values;

    /**
     * Creates a new data event that keeps track of the values received and the
     * type of data it is
     * @param event Sensor data event that occured
     * @param type Type of data we got
     */
    public RawDataEvent(SensorEvent event, float[] values, EventType type) {
        this.type = type;
        this.event = event;
        this.values = values;
    }
}
