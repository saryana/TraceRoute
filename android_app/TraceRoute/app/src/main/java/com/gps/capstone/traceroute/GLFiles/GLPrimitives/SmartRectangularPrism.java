package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.opengl.Matrix;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;
import com.gps.capstone.traceroute.GLFiles.util.VectorLibrary;

/**
 * Defines a smart rectangular prism, that doesn't depend on tons
 * of cross products to compute its position. This prism's default position
 * is centered at the origin along the x-axis. It uses a quaternion to get its position.
 */
public class SmartRectangularPrism extends DrawableObject {
    // The thickness of the rectangular prism. I made this an internal object
    //
    private static final float SIZE = 0.1f;

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
        float angleOne = (float)Math.atan(directionVector[2] / directionVector[0]);
        float angleTwo = (float)Math.atan(directionVector[1] / directionVector[1]);
        
    }


}
