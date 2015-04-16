package com.gps.capstone.traceroute.sensors;

/**
 * Created by saryana on 4/11/15.
 */
public class OrientationChangeEvent {
    public float[] data;
    public SensorUtil.EventType type;

    public OrientationChangeEvent(float[] data, SensorUtil.EventType type) {
        this.data = data;
        this.type = type;
    }
}
