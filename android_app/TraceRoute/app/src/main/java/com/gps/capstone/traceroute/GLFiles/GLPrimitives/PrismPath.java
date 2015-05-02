package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import org.apache.http.impl.conn.ProxySelectorRoutePlanner;

import java.util.ArrayList;

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

    public PrismPath(ProgramManager graphicsEnv) {
        this.graphicsEnv = graphicsEnv;
        previousPoint = new float[3];
        path = new ArrayList<RectangularPrism>();
    }

    /**
     * Add a point to this path, which will
     * be drawn on the next frame.
     * @param coords
     */
    public void addPoint(float[] coords) {
        RectangularPrism end = new RectangularPrism(graphicsEnv);
        end.setDimensions(previousPoint, coords, SIZE, SIZE);
        path.add(end);
        previousPoint = coords;
    }

    /**
     * Draw the path using the given mvpMatrix.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        for (RectangularPrism cur : path) {
            cur.draw(mvpMatrix);
        }
    }
}
