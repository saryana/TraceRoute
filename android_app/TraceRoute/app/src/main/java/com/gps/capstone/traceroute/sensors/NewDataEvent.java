package com.gps.capstone.traceroute.sensors;

/**
 * Created by saryana on 4/14/15.
 */
public class NewDataEvent {
    // type of data
    public int type;
    // data received
    public float[] data;

    public NewDataEvent(float[] data, int type) {
        this.type = type;
        this.data = data;
    }

}
