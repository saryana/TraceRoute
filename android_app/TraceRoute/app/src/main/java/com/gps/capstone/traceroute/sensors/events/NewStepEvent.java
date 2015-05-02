package com.gps.capstone.traceroute.sensors.events;

/**
 * Created by saryana on 4/29/15.
 */
public class NewStepEvent {
    public float[] newFace;

    public NewStepEvent(float[] newFace) {
        this.newFace = newFace;
    }
}
