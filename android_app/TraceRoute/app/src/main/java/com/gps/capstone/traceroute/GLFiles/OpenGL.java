package com.gps.capstone.traceroute.GLFiles;

import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gps.capstone.traceroute.NewRotationVectorEvent;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.SensorDataManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;


public class OpenGL extends ActionBarActivity {

    private GLSurfaceView mGLSurface;
    public static Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurface = new MySurfaceView(this);
        setContentView(mGLSurface);
        mBus = new Bus();
        mBus.register(this);
        SensorDataManager.createInstance(this, mBus);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onNewData(NewRotationVectorEvent e) {
        Log.d("DATA2", Arrays.toString(e.data));
    }
}
