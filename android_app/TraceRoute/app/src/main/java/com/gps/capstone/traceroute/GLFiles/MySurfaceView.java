package com.gps.capstone.traceroute.GLFiles;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by saryana on 4/9/15.
 */
public class MySurfaceView extends GLSurfaceView {

    private MyGLRenderer mRenderer;

    public MySurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}
