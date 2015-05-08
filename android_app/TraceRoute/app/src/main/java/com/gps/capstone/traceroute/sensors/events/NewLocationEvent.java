package com.gps.capstone.traceroute.sensors.events;

/**
 * Created by saryana on 5/7/15.
 */
public class NewLocationEvent {
    public float[] location;
    public float[] direction;
    public NewLocationEvent(float[] location, float[] direction) {
        this.location = location;
        this.direction = direction;
    }
}
