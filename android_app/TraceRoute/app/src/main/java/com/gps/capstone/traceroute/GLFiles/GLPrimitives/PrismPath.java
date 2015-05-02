package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import android.util.Log;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by gammoa on 5/2/15.
 */
public class PrismPath {
    // Stores the graphics environment
    private ProgramManager graphicsEnv;
    // stores the head of the path.
    private float[] previousPoint;

    // The size of the path.
    private static final float SIZE = 0.3f;

    private ArrayList<RectangularPrism> path;
    private RectangularPrism p;

    public PrismPath(ProgramManager graphicsEnv) {
        this.graphicsEnv = graphicsEnv;
        previousPoint = new float[3];
        path = new ArrayList<RectangularPrism>();
        p = new RectangularPrism(graphicsEnv);
    }

    /**
     * Add a point to this path, which will
     * be drawn on the next frame.
     * @param coords
     */
    public void addPoint(float[] coords) {
        Log.d("AH", "ADDING POINT " + Arrays.toString(coords));
        RectangularPrism end = new RectangularPrism(graphicsEnv);
        end.setDimensions(previousPoint, coords, SIZE, SIZE);
        path.add(end);
        previousPoint = coords;
        p = end;
    }

    /**
     * Draw the path using the given mvpMatrix.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
//        for (int i = 0; i < path.size(); i++) {
//            path.get(i).draw(mvpMatrix);
//        }
        p.draw(mvpMatrix);
    }
}
