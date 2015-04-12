package com.gps.capstone.traceroute.GLFiles.util;

/**
 * Created by gammoa on 4/12/15.
 */
public class TouchUtil {
    // computes the midpoint between two points and returns a tuple with
    // the x-coordinate and y-coordinate of the midpoint.
    public static float[] midpointFormula(float x1, float y1, float x2, float y2) {
        // I know this is bad style. You can hit me if you want.
        // 0 = x-coordinate, 1 = y-coordinate
        float[] tuple = new float[2];
        // compute the relative position of the midpoint.
        float dx = (x1 - x2) / 2;
        float dy = (y1 - y2) / 2;

        // if x1's value is less than x2, add -1 * dx to x1 to get the x coordinate of the midpoint.
        if (dx < 0) {
            tuple[0] = x1 + (dx * -1);
        } else {
            // add the negative value of dx to x1 to get the x-coordinate of the midpoint.
            tuple[0] = x1 - dx;
        }

        // if y1's value is less than y2, add -1 * dy to y1 to get the y-coordinate of the midpoint.
        if (dy < 0) {
            tuple[1] = y1 + (dy * -1);
        } else {
            // add the negative value of dy to y1 to get the y-coordinate of the midpoint.
            tuple[1] = y1 - dy;
        }
        // return the midpoint.
        return tuple;
    }

    // computes the distance between two points and returns it in a float.
    public static float distanceFormula(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
