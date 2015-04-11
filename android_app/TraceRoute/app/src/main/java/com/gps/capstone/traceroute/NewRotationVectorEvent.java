package com.gps.capstone.traceroute;

/**
 * Created by saryana on 4/11/15.
 */
public class NewRotationVectorEvent {
    public float[] data;
    public int type;

    public NewRotationVectorEvent(float[] data, int type) {
        this.data = data;
        this.type = type;
    }
}
