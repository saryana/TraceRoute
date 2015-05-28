package com.gps.capstone.traceroute.sensors.events;

/**
 * Created by saryana on 5/7/15.
 */
public class NewLocationEvent {
    public float[] location;
    public float[] otherLocation;
    public NewLocationEvent(float[] location, float[] otherLocation) {
        this.otherLocation = otherLocation;
        this.location = location;
    }
}
