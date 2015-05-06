package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;
import com.gps.capstone.traceroute.GLFiles.util.VectorLibrary;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Defines a smart rectangular prism, that doesn't depend on tons
 * of cross products to compute its position. This prism's default position
 * is centered at the origin along the x-axis. It uses a quaternion to get its position.
 */
public class SmartRectangularPrism extends DrawableObject {
    // The thickness of the rectangular prism. I made this an internal object
    // field because it's not going to change very often.
    private static final float SIZE = 0.3f;

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

    private final short[] drawOrder = {0,1,2, 2,3,0, 7,0,3, 3,4,7, 4,3,2, 2,5,4, 5,2,1, 1,6,5, 6,1,0, 0,7,6, 4,5,6, 6,7,4};

    public SmartRectangularPrism(ProgramManager graphicsEnv) {
        super(graphicsEnv);
    }

    public void setDimensions(float[] firstFace, float[] secondFace) {
        // This direction vector can be used to compute a quaternion with a 0 degree rotation.
        // This can be used to move all of the verticies into the right orientation, and I can use the coordinates
        // of the first face to move it into the right position. This method makes it much easier to compute surface normals,
        // as you can just take the standard unit vectors and multiply them by the quaternion to get surface normals.
        float[] directionVector = {secondFace[0] - firstFace[0], secondFace[1] - firstFace[1],
                secondFace[2] - firstFace[2]};
        // get the length of the prism
        float length = VectorLibrary.vectorLength(directionVector);

        // These are the result dimensions.
        // FIRST FACE
        // Top left, Bottom left, Bottom right, Top right
        float[] result = {0, SIZE, -SIZE, 0, -SIZE, -SIZE, 0, -SIZE, SIZE, 0, SIZE, SIZE,
                // SECOND FACE
                length, SIZE, SIZE, length, -SIZE, SIZE, length, -SIZE, -SIZE, length, SIZE, -SIZE};
        // xz angle (y-axis rotation)
        float angleOne = (float)Math.atan(directionVector[2] / directionVector[0]);
        // convert this shit to degrees.
        angleOne = (float)((angleOne / (2 * Math.PI)) * 360);
        // xy angle (z axis rotation)
        float angleTwo = (float)Math.atan(directionVector[1] / directionVector[0]);
        // convert this to degrees.
        angleTwo = (float)((angleTwo / (2 * Math.PI)) * 360);
        Log.d("ANGLE", "Y angle: " + angleOne);
        Log.d("ANGLE", "Z angle: " + angleTwo);

        float[] rotationMat = new float[16];

        // set up a rotation matrix.
        Matrix.setIdentityM(rotationMat, 0);

        // y axis rotation
        Matrix.rotateM(rotationMat, 0, angleOne, 0, 100, 0);

        // z axis rotation
        Matrix.rotateM(rotationMat, 0, angleTwo, 0, 0, 100);

        // loop through and apply the rotation to each vertex.
        for (int i = 0; i < result.length; i += 3) {
            // grab the current vertex in 4-d space.
            float[] curVec = {result[i], result[i+1], result[i+2], 0};
            float[] resultVec = new float[4];

            Matrix.multiplyMV(resultVec, 0, rotationMat, 0, curVec, 0);

            // update the object array with the rotated vertex.
            for (int j = 0; j < DrawableObject.DIMENSIONS; j++) {
                result[i + j] = resultVec[j];
            }
        }

        // transposition into the correct location.
        for (int i = 0 ; i < result.length; i++) {
            result[i] += firstFace[i % 3];
        }

        setVerticies(result);
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
        // Draw the prism
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex attribute arrays.
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mVertexColorHandle);
    }


}
