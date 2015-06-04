package com.gps.capstone.traceroute.sensors.events;

import java.util.ArrayList;

/**
 * Created by saryana on 5/12/15.
 */
public class NewPathFromFile {
    public ArrayList<float[]> path;
    public boolean wait;

    public NewPathFromFile(ArrayList<float[]> path, boolean wait) {
        this.path = path;
        this.wait = wait;
    }
}
