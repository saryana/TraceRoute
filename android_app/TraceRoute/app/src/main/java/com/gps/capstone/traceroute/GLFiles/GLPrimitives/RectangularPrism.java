package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;

import com.gps.capstone.traceroute.GLFiles.ProgramManager;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Defines a rectangular prism class that can be used to draw the the path.
 */
public class RectangularPrism extends DrawableObject {

    private float[] colors = {
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f
    };

    // The draw order for this prism.
    private short[] drawOrder = {
        // Front Face                                                         Rear Face
        2,1,0, 0,3,2, 3,0,4, 4,7,3, 0,1,5, 5,4,0, 6,5,1, 1,2,6, 6,2,3, 3,7,6, 7,4,5, 5,6,7
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
        float[] result = new float[24];
        // calculate coordinate data for both faces.
        float[] firstFace = calculateFace(firstEndCoords, secondEndCoords, baseXWidth, baseHeight);
        float[] secondFace = calculateSecondFace(firstEndCoords, secondEndCoords, firstFace);

        // Set the
        for (int i = 0; i < firstFace.length; i++) {
            result[i] = firstFace[i];
        }

        for (int i = 0; i < secondFace.length; i++) {
            result[i+firstFace.length] = secondFace[i];
        }

        setVerticies(result);

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
        // The normalXZVector is a vector that's parallel to the XZ-plane and orthogonal to the
        // prism's direction vector.
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
        // get the direction vector between two faces.
        float[] directionVector = {secondEndCoords[0]-firstEndCoords[0], secondEndCoords[1]-firstEndCoords[1],
                secondEndCoords[2]-firstEndCoords[2]};

        float[] result = new float[12];

        for (int i = 0 ; i < 12; i++) {
            result[i] = firstFace[i] + directionVector[i%3];
        }

        return result;
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

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);


        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, DrawableObject.DIMENSIONS,
                GLES20.GL_FLOAT, false,
                DrawableObject.FLOAT_SIZE * DrawableObject.DIMENSIONS, vertexData);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Colors?!
        FloatBuffer compatibleColors = convertFloatArray(colors);
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4, GLES20.GL_FLOAT, false,
                DrawableObject.FLOAT_SIZE * 4, compatibleColors);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);


        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        ShortBuffer drawListBuffer = convertShortArray(drawOrder);
        // Draw Cube
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex attribute arrays.
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
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
