package com.gps.capstone.traceroute.sensors.events;

import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;

/**
 * Created by saryana on 4/14/15.
 */
public class NewDataEvent {
    // type of values
    public EventType type;
    // values received
    public float[] values;

    /**
     * Creates a new data event that keeps track of the values received and the
     * type of data it is
     * @param values Values from the sensors
     * @param type Type of data we got
     */
    public NewDataEvent(float[] values, EventType type) {
        this.type = type;
        this.values = values;
    }

}
