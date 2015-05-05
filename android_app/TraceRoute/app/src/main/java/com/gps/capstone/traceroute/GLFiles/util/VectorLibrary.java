package com.gps.capstone.traceroute.GLFiles.util;

/**
 * Defines several utility functions that operate on vectors in 3-D space.
 */
public class VectorLibrary {

    // compute the coordinate position based on a 'length' offset of a vector (targetVector).
    public static float[] computePositionFromVector(float[] targetVector, float length) {
        // compute the ratio that allows us to compute the new
        // vector who points to where the corner lies on the original vector.
        float ratio = length/vectorLength(targetVector);
        float[] result = {ratio*targetVector[0],
                ratio*targetVector[1],
                ratio*targetVector[2]};

        return result;
    }

    // Returns the length of a given vector.
    public static float vectorLength(float[] vec) {
        return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    // Compute the cross product between two vectors and return the result.
    // THIS COULD GIVE STRANGE VECTOR DIRECTIONS
    public static float[] crossProduct(float[] vectorOne, float[] vectorTwo) {
        // CROSS PRODUCT FORMULA (a x b = c)
        //cx = aybz − azby
        //cy = azbx − axbz
        //cz = axby − aybx
        float[] result = {vectorOne[1]*vectorTwo[2]-vectorOne[2]*vectorTwo[1],
                vectorOne[2]*vectorTwo[0]-vectorOne[0]*vectorTwo[2],
                vectorOne[0]*vectorTwo[1]-vectorOne[1]*vectorTwo[0]};

        return result;
    }

    // Add two vectors together, return the result.
    public static float[] addVector(float[] vectorOne, float[] vectorTwo) {
        float[] result = {vectorOne[0]+vectorTwo[0],vectorOne[1]+vectorTwo[1],vectorOne[2]+vectorTwo[2]};
        return result;
    }
}
