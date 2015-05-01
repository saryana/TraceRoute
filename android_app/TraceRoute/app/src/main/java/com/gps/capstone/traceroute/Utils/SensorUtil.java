package com.gps.capstone.traceroute.Utils;

/**
 * Created by saryana on 4/16/15.
 */
public class SensorUtil {
    public enum EventType {
        ROTATION_MATRIX_CHANGE, DELTA_ROTATION_MATRIX, ROTATION_VECTOR_CHANGE, GYROSCOPE_CHANGE, DIRECTION_CHANGE
    }

    public enum SensorSource {
        ACCEL_GRAV, GRYO
    }

    public enum Direction {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;
    }

}
