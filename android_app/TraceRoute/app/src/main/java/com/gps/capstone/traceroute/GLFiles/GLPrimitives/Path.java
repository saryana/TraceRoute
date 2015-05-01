package com.gps.capstone.traceroute.GLFiles.GLPrimitives;

import com.gps.capstone.traceroute.GLFiles.util.ProgramManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keith619 on 4/19/15.
 */
@Deprecated
public class Path {

    // List of all points on the path
    private List<GLPoint> mPoints;

    private ProgramManager mGraphicsEnv;

    public Path(ProgramManager graphicsEnv) {
        mPoints = new ArrayList<GLPoint>();
        mGraphicsEnv = graphicsEnv;

        // The first point on the path is the origin
        this.addPoint(0,0,0);
        addPoint(0.7f, 0.7f, 0.7f);
    }


    /**
     * Adds a point to the path
     * @param x x value of coordinate
     * @param y y value of coordinate
     * @param z z value of coordinate
     */
    public void addPoint (float x, float y, float z) {
        GLPoint p = new GLPoint(mGraphicsEnv, x, y, z);
        mPoints.add(p);
    }

    /**
     * Get the most recently added point
     * @return the last added point
     */
    public GLPoint getLastPoint() {
        return mPoints.get(mPoints.size()-1);
    }

    /**
     * Draw the path.
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix) {
        for (GLPoint p: mPoints) {
            p.draw(mvpMatrix);
        }
    }
}
