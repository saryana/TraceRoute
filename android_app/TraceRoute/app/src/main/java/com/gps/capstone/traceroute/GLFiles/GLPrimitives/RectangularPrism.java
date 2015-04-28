package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import com.gps.capstone.traceroute.GLFiles.ProgramManager;

import java.util.ArrayList;

/**
 *
 */
public class RectangularPrism extends DrawableObject {

    private float[] colors = {

    };

    /**
     * Create a new rectangular prism in the given graphics environment.
     * @param graphicsEnv
     */
    public RectangularPrism(ProgramManager graphicsEnv) {
        super(graphicsEnv);
    }

    /**
     * Set up the dimensions and location for the rectangular prism.
     * @param firstEndCoords The location of one face of the rectangular prism
     * @param secondEndCoords The location of another face of the rectangular prism.
     * @param baseXWidth The width of the prism base. This is the line that is
     *                   parallel the x-z pane.
     * @param baseHeight The height of the prism base.
     */
    public void setDimensions(float[] firstEndCoords, float[] secondEndCoords,
                                float baseXWidth, float baseHeight) {
        float[] firstFace = calculateFace(firstEndCoords, secondEndCoords, baseXWidth, baseHeight);
        float[] secondFace = calculateSecondFace(firstEndCoords, secondEndCoords, firstFace);
    }

    /*
    Calculates the coordinate positions for the first face for the rectangular prism.
     */
    private float[] calculateFace(float[] firstEndCoords, float[] secondEndCoords, float baseXWidth, float baseHeight) {

        // get the direction vector between two faces.
        float[] directionVector = {secondEndCoords[0]-firstEndCoords[0], secondEndCoords[1]-firstEndCoords[1],
                secondEndCoords[2]-firstEndCoords[2]};

        // drop a vector down to the x-z plane, so we can compute a couple cross products.
        float[] directionVectorFlat = {directionVector[0], 0, directionVector[2]};

        // Compute two orthogonal vectors to the direction vector that are orthogonal to each other.
        // These three vectors together will define the orientation of the prism.
        float[] normalXZVector = crossProduct(directionVector, directionVectorFlat);
        float[] normalVectorTwo = crossProduct(directionVector, normalXZVector);

        // compute the corner coordinate positions for this face, and return.
        float[] result = computeCorners(firstEndCoords, normalXZVector, normalVectorTwo,
                baseXWidth, baseHeight);
        return result;
    }

    /*
    Compute the opposing face.
     */
    private float[] calculateSecondFace(float[] firstEndCoords, float[] secondEndCoords, float[] firstFace) {
        // TODO: Make the second face.
        return null;
    }

    // compute the corner positions for one of the bases.
    // NormalXZ is the orthogonal vector to the prism directionVector that's also parallel
    // to the XZ plane.
    private float[] computeCorners(float[] center, float[] normalXZ, float[] normalTwo,
                                   float baseXWidth, float baseHeight) {
        ArrayList<Float> result = new ArrayList<Float>();
        // Keep in mind that the directions here are oriented from the perspective
        // of if you're staring at the base from INSIDE the prism, and you're standing on the XZ plane.
        float[] bottomLeft = addVector(center, addVector(computeOffset(normalXZ, -baseXWidth/2), computeOffset(normalTwo, -baseHeight/2)));
        float[] bottomRight = addVector(center, addVector(computeOffset(normalXZ, baseXWidth/2), computeOffset(normalTwo, -baseHeight/2)));
        float[] topRight = addVector(center, addVector(computeOffset(normalXZ, baseXWidth/2), computeOffset(normalTwo, baseHeight/2)));
        float[] topLeft = addVector(center, addVector(computeOffset(normalXZ, -baseXWidth/2), computeOffset(normalTwo, baseHeight/2)));
        // slap the arrays all together!
        result.addAll(toArrayList(bottomLeft));
        result.addAll(toArrayList(bottomRight));
        result.addAll(toArrayList(topRight));
        result.addAll(toArrayList(topLeft));
        // return this bad boy.
        return floatToFloat(result.toArray(new Float[0]));
    }

    /////////////////////////////////
    // UTILITY FUNCTIONS
    /////////////////////////////////

    // Converts a Float array to a float array, because goddamn type system sucks donkey balls.
    public float[] floatToFloat(Float[] target) {
        float[] result = new float[target.length];
        for (int i = 0; i < target.length; i++) {
            result[i] = target[i];
        }
        return result;
    }

    // Converts a float array to a float array list.
    public ArrayList<Float> toArrayList(float[] target) {
        ArrayList<Float> result = new ArrayList<Float>();
        for (int i = 0; i < 3; i++) {
            result.add(target[i]);
        }
        return result;
    }

    // compute the coordinate position of a corner based on the direction vector
    // targetVector. Math 308 all over again!
    private float[] computeOffset(float[] targetVector, float length) {
        // compute the ratio that allows us to compute the new
        // vector who points to where the corner lies on the original vector.
        float ratio = length/vectorLength(targetVector);
        float[] result = {ratio*targetVector[0],
                            ratio*targetVector[1],
                            ratio*targetVector[2]};

        return result;
    }

    // Returns the length of a given vector.
    private float vectorLength(float[] vec) {
        return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    // Compute the cross product between two vectors and return the result.
    // THIS COULD GIVE STRANGE VECTOR DIRECTIONS
    private float[] crossProduct(float[] vectorOne, float[] vectorTwo) {
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
    private float[] addVector(float[] vectorOne, float[] vectorTwo) {
        float[] result = {vectorOne[0]+vectorTwo[0],vectorOne[1]+vectorTwo[1],vectorOne[2]+vectorTwo[2]};
        return result;
    }



}
