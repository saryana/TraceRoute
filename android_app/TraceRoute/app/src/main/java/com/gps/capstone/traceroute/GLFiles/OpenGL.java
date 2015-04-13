package com.gps.capstone.traceroute.GLFiles;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.gps.capstone.traceroute.DebugConsole;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.UserSettings;
import com.gps.capstone.traceroute.sensors.SensorDataManager;


public class OpenGL extends ActionBarActivity {

    private GLSurfaceView mGLSurface;
    // Sensor data manager
    private SensorDataManager mSensorDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurface = new MySurfaceView(this);
        setContentView(mGLSurface);
        mSensorDataManager = new SensorDataManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorDataManager.register();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorDataManager.unregister();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_gl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Depending on what the user clicks lets start that activity
        Intent i = null;
        switch (id) {
            case R.id.user_settings:
                i = new Intent(this, UserSettings.class);
                break;
            case R.id.debug_console:
                i = new Intent(this, DebugConsole.class);
                break;
        }
        if (i != null)
            startActivity(i);

        return super.onOptionsItemSelected(item);
    }
}
