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

    /**
     * Computes the cross product between two 3d vectors and returns the result.
     * @param vectorOne The first vector.
     * @param vectorTwo The second vector.
     * @return The cross product of vectorOne with vectorTwo.
     */
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

    /**
     * Computes the dot product between two vectors and returns the result.
     * @param vectorOne
     * @param vectorTwo
     * @return The dot product between vectorOne and vectorTwo.
     */
    public static float dotProduct(float[] vectorOne, float[] vectorTwo) {
        return vectorOne[0] * vectorTwo[0] +
                vectorOne[1] * vectorTwo[1] +
                vectorOne[2] * vectorTwo[2];
    }

    // Add two vectors together, return the result.
    public static float[] addVector(float[] vectorOne, float[] vectorTwo) {
        float[] result = {vectorOne[0]+vectorTwo[0],vectorOne[1]+vectorTwo[1],vectorOne[2]+vectorTwo[2]};
        return result;
    }
}
