package com.gps.capstone.traceroute.Utils;

/**
 * Created by saryana on 4/16/15.
 */
public class SensorUtil {
    public enum EventType {
        ROTATION_MATRIX_CHANGE, DELTA_ROTATION_MATRIX, ROTATION_VECTOR_CHANGE, GYROSCOPE_CHANGE, ALTITUDE_CHANGE, STEP_DETECTED, DIRECTION_CHANGE
    }

    public enum SensorSource {
        ACCEL_GRAV, GRYO
    }

    public enum Direction {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
    }


    // TODO this method might not be correct. I forgot that cardinal direction degrees is different from trig degrees
    /**
     * Converts the given direction to a unit vector in the xy plane.
     * @param theta the direction to convert
     * @return an array of floats representing vector {x, y}
     */
    public static float[] getVectorFromAngle2(float theta) {
        // convert cardinal angles to trig angles
        double trigTheta;
        if (theta >= 0 && theta <= Math.PI/2) {
            trigTheta = Math.PI/2 - theta;
        } else if (theta > Math.PI/2 && theta <= 3*Math.PI/2) {
            trigTheta = -(theta - Math.PI/2);
        } else {
            trigTheta = Math.PI - (theta-3*Math.PI/2);
        }

        // calculate a unit vector in xy plane that points in the given direction
        double x = Math.sin(trigTheta);
        double y = Math.cos(trigTheta);

        return new float[]{(float) x, (float) y};
    }

    /**
     * Initial idea simply take the sin and cos of the angle and
     * translate that to x and y coordinates
     * @param theta Angle of heading fomr magnetic north (Aziumth)
     * @return an array of floats representing vector {x, y}
     */
    public static float[] getVectorFromAngle1(float theta) {
        double x = Math.sin(theta);
        double y = Math.cos(theta);
        return new float[]{(float) x, (float) y};
    }
}
