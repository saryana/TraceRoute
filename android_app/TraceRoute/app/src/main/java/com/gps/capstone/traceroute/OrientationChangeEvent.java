package com.gps.capstone.traceroute;

/**
 * Created by saryana on 4/11/15.
 */
public class OrientationChangeEvent {
    public float[] data;
    public int type;

    public OrientationChangeEvent(float[] data, int type) {
        this.data = data;
        this.type = type;
    }
}
