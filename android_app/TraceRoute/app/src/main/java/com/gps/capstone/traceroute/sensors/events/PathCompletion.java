package com.gps.capstone.traceroute.sensors.events;

/**
 * Created by saryana on 5/31/15.
 */
public class PathCompletion {
    public int steps;
    public float distance;
    public float initialAltitude;
    public float finalAltitude;

    public PathCompletion(int steps, float distance, float initalAltitude, float finalAltitude) {
        this.steps = steps;
        this.distance = distance;
        this.initialAltitude = initalAltitude;
        this.finalAltitude = finalAltitude;
    }
}
