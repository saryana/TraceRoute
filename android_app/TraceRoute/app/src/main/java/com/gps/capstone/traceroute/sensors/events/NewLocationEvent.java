package com.gps.capstone.traceroute.sensors.events;

/**
 * Created by saryana on 5/7/15.
 */
public class NewLocationEvent {
    public float[] value;
    public NewLocationEvent(float[] value) {
        this.value = value;
    }
}
