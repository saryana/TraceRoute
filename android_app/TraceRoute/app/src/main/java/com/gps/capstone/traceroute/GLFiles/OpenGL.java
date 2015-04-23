package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager.LayoutParams;

import com.gps.capstone.traceroute.BasicActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;


public class OpenGL extends BasicActivity {
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    // Defines whether the user is in control of the map or not
    public static boolean USER_CONTROL;
    // Defines whether to use the gyro scope or the rotation matrix
    public static boolean USE_GYROSCOPE;
    // The source of our sensor data
    private SensorDataProvider mDataProvider;
    // The view we are rendering
    private GLSurfaceView mGLSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurface = new MySurfaceView(this);
        setContentView(mGLSurface);

        mDataProvider = new SensorDataProvider(this);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        USER_CONTROL = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_user_control), false);
        USE_GYROSCOPE = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_use_gyroscope), true);
        Log.d(TAG, "User control: " + USER_CONTROL);
        Log.d(TAG, "Use gyroscope: " + USE_GYROSCOPE);

        mDataProvider.register(USER_CONTROL, USE_GYROSCOPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataProvider.unregister();
        getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_gl, menu);
        return true;
    }
}
