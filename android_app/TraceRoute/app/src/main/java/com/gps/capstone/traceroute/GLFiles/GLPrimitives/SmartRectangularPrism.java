package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.gps.capstone.traceroute.GLFiles.util.VectorLibrary;
import com.gps.capstone.traceroute.Utils.SensorUtil;

import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Defines a smart rectangular prism, that doesn't depend on tons
 * of cross products to compute its position. This prism's default position
 * is centered at the origin along the x-axis. It uses a quaternion to get its position.
 */
public class SmartRectangularPrism extends DiffuseLightingObject {
    // The thickness of the rectangular prism. I made this an internal object
    // field because it's not going to change very often.

    private static final float SIZE = 0.1f;

    // colors for the face.
    private float[] colors = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    //                                FRONT          TOP           RIGHT         BOTTOM        LEFT          BACK
    private final short[] drawOrder = {0,1,2, 2,3,0, 7,0,3, 3,4,7, 4,3,2, 2,5,4, 5,2,1, 1,6,5, 6,1,0, 0,7,6, 4,5,6, 6,7,4};

    // The vetex normals, when the prism is in the default position. THIS ARRAY DEPENDS ON THE DRAWORDER. MAKE SURE YOU CHANGE
    // THIS WHEN YOU MESS WITH THE DRAW ORDER.
    private float[] normals = {
            // FRONT
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            // TOP
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,

            0, 1, 0,
            0, 1, 0,
            0, 1, 0,

            // RIGHT
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            // BOTTOM
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,

            0, -1, 0,
            0, -1, 0,
            0, -1, 0,

            // LEFT
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            // BACK
            0, 0, 0,
            0, 0, 0,
            0, 0, 0,

            1, 0, 0,
            1, 0, 0,
            1, 0, 0
    };

    // Stores the length of the straigtened vertex array.
    private int vertexArrayLength;

    /**
     * Constructs a rectangular prism.
     * TODO: Add the first face and second face parameters back into the constructor.
     */
    public SmartRectangularPrism() {
        setColors(toStraightArray(colors, 4));
    }



    /**
     * Sets the coordinates of the rectangular prism based on SIZE and the position of the first
     * and second face.
     *
     * @param firstFace
     * @param secondFace
     */
    public void setDimensions(float[] firstFace, float[] secondFace) {
        // This direction vector can be used to compute a quaternion with a 0 degree rotation.
        // This can be used to move all of the verticies into the right orientation, and I can use the coordinates
        // of the first face to move it into the right position. This method makes it much easier to compute surface normals,
        // as you can just take the standard unit vectors and multiply them by the quaternion to get surface normals.
        float[] directionVector = {secondFace[0] - firstFace[0], secondFace[1] - firstFace[1],
                secondFace[2] - firstFace[2]};

        // get the length of the prism
        float length = VectorLibrary.vectorLength(directionVector);
        Log.d("DATA", Arrays.toString(directionVector));

        // FIRST FACE
        // Top left, Bottom left, Bottom right, Top right
        // FRONT
        float[] initialVerticies = {0, SIZE, -SIZE, 0, -SIZE, -SIZE, 0, -SIZE, SIZE, 0, SIZE, SIZE,
                // BACK
                length, SIZE, SIZE, length, -SIZE, SIZE, length, -SIZE, -SIZE, length, SIZE, -SIZE};

        float[] verticies = toStraightArray(initialVerticies, DrawableObject.DIMENSIONS);
        vertexArrayLength = verticies.length;

        // xz angle (y-axis rotation)
        float angleOne;
        // xy angle (z axis rotation)
        float angleTwo;


        angleOne = (float) Math.atan2(directionVector[2], directionVector[0]);
        // convert this shit to degrees.
        angleOne = SensorUtil.radianToDegree(angleOne);
        angleTwo = (float) Math.atan2(directionVector[1], directionVector[0]);
        // convert this to degrees.
        angleTwo = SensorUtil.radianToDegree(angleTwo);

        // Need to confirm this. We may have our coordinates confused.

        // This is the nasty case.
        if (directionVector[0] < 0) {
            angleOne += 180;
        }

//        Log.d("ANGLE", "Y angle: " + angleOne);
//        Log.d("ANGLE", "Z angle: " + angleTwo);

        float[] rotationMat = new float[16];

        // set up a rotation matrix.
        Matrix.setIdentityM(rotationMat, 0);

        // apply y axis rotation
        Matrix.rotateM(rotationMat, 0, angleOne, 0, 100, 0);

        // apply z axis rotation
        Matrix.rotateM(rotationMat, 0, angleTwo, 0, 0, 100);

        // apply the rotation to each vertex and each normal.
        for (int i = 0; i < verticies.length; i += 3) {
            // grab the current vertex in 4-d space.
            float[] curVec = {verticies[i], verticies[i + 1], verticies[i + 2], 0};
            float[] resultVec = new float[4];

            // grab the current vertex normal.
            float[] curNormalVec = {normals[i], normals[i + 1], normals[i + 2], 0};
            float[] resultNormalVec = new float[4];

            Matrix.multiplyMV(resultVec, 0, rotationMat, 0, curVec, 0);
            Matrix.multiplyMV(resultNormalVec, 0, rotationMat, 0, curNormalVec, 0);

            // update the object array with the rotated vertex.
            for (int j = 0; j < DrawableObject.DIMENSIONS; j++) {
                verticies[i + j] = resultVec[j];
                normals[i + j] = resultNormalVec[j];
            }
        }

        // transposition into the correct location.
        for (int i = 0; i < verticies.length; i++) {
            verticies[i] += firstFace[i % 3];
        }

        setVerticies(verticies);
        setNormals(normals);
    }

    /**
     * Draws the object to the openGL pane.
     *
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix, float[] mvMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programHandle);

        // Prepare the triangle coordinate values
        GLES20.glVertexAttribPointer(mVertexPositionHandle, DrawableObject.DIMENSIONS,
                GLES20.GL_FLOAT, false,
                DrawableObject.FLOAT_SIZE * DrawableObject.DIMENSIONS, vertexData);

        // Enable a handle to the axis vertices
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle);

        // Colors?!
        GLES20.glVertexAttribPointer(mVertexColorHandle, 4, GLES20.GL_FLOAT, false,
                DrawableObject.FLOAT_SIZE * 4, colorData);

        GLES20.glEnableVertexAttribArray(mVertexColorHandle);



        GLES20.glVertexAttribPointer(mVertexNormalHandle, DrawableObject.DIMENSIONS,
                GLES20.GL_FLOAT, false,
                DrawableObject.FLOAT_SIZE * DrawableObject.DIMENSIONS, normalData);

        GLES20.glEnableVertexAttribArray(mVertexNormalHandle);

        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniform3f(mPointLightPos, lightPos[0], lightPos[1], lightPos[2]);


        // Draw the prism
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexArrayLength / DrawableObject.DIMENSIONS);


        // Disable vertex attribute arrays.
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
        GLES20.glDisableVertexAttribArray(mVertexNormalHandle);
    }


    // PRIVATES

    /*
     * Converts an array of indicies into an array that can be drawn by a call
     * to glDrawArrays. The order of the verticies in the array that's returned by
     * this is dependent upon the draw order.
     */
    private float[] toStraightArray(float[] target, int strideLength) {
        // create the new
        float[] result = new float[drawOrder.length * strideLength];
        int resultCurIndex = 0;
        for (int i = 0; i < drawOrder.length; i++) {
            // grab the next vertex in target to copy over to result.
            int targetIndex = drawOrder[i] * strideLength;

            // append the next 3 values in target onto the end of result.
            for (int j = 0; j < strideLength; j++) {
                result[resultCurIndex + j] = target[targetIndex + j];
            }
            resultCurIndex += strideLength;
        }

        return result;
    }
}