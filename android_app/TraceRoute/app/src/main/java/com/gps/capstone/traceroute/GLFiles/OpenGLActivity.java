package com.gps.capstone.traceroute.GLFiles;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.BasicActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewStepEvent;
import com.squareup.otto.Subscribe;

import java.util.Arrays;


public class OpenGLActivity extends BasicActivity {
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    // Defines whether the user is in control of the map or not
    public static boolean USER_CONTROL;
    // Defines whether the camera follows path
    public static boolean FOLLOW_PATH;
    // Defines whether to use the gyro scope or the rotation matrix
    public static boolean USE_GYROSCOPE;
    // Flag to use cube for the render
    public static boolean USE_CUBE;
    public static boolean USE_SHAPE;
    // The source of our sensor data
    private SensorDataProvider mDataProvider;
    private int mStepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        GLSurfaceView mGLSurface = new MySurfaceView(this);
        setContentView(R.layout.activity_open_gl);
        mDataProvider = new SensorDataProvider(this);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStepCount = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        USER_CONTROL = sharedPreferences.getBoolean(getString(R.string.pref_key_user_control), false);
        USE_CUBE = sharedPreferences.getBoolean(getString(R.string.pref_key_use_cube), true);
        USE_GYROSCOPE = sharedPreferences.getBoolean(getString(R.string.pref_key_use_gyroscope), true);
        USE_SHAPE = sharedPreferences.getBoolean(getString(R.string.pref_key_render_shape), true);

        Log.d(TAG, "User control: " + USER_CONTROL);
        Log.d(TAG, "Use gyroscope: " + USE_GYROSCOPE);
        BusProvider.getInstance().register(this);
        mDataProvider.register(USER_CONTROL, USE_GYROSCOPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataProvider.unregister();
        BusProvider.getInstance().unregister(this);
        getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_gl, menu);
        return true;
    }

    @Subscribe
    public void onDataChange(NewDataEvent newDataEvent) {
        if (newDataEvent.type == EventType.DIRECTION_CHANGE) {
            float heading = (float) (newDataEvent.values[0] * 180f / Math.PI);
            if (heading < 0) {
                heading += 360;
            }
            ((TextView) findViewById(R.id.heading_direction)).setText("Heading Direction : " + heading);
        }
    }

    @Subscribe
    public void onStepDetected(NewStepEvent newStepEvent) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prev_step_values);
        TextView tv = new TextView(this);
        tv.setText(String.format("Step %d at <%f, %f, %f>", mStepCount,
                newStepEvent.newFace[0], newStepEvent.newFace[1], newStepEvent.newFace[2]));
        linearLayout.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mStepCount++;
    }
}
