package com.gps.capstone.traceroute;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    // Tag used for logging
    private final String TAG = this.getClass().getSimpleName();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyAllSensors();

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        sensorManager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL, Sensor.REPORTING_MODE_SPECIAL_TRIGGER);

        Toast.makeText(this, "This is our trace route application", Toast.LENGTH_LONG).show();
    }

    /**
     * Prints all the sensors that it has on device
     */
    private void verifyAllSensors() {
        // Grab the sensor manager
        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        // Go through all the sensors on the device and log them
        for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                Log.i(TAG, "Sensor: " + s.getName() + "\nIs wake up?: " + s.isWakeUpSensor());
            } else {
                Log.i(TAG, "Sensor: " + s.getName());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    double max = Double.MIN_VALUE;
    double min = Double.MAX_VALUE;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getName().equals("SAMSUNG Step Counter Sensor")) {
            Toast.makeText(MainActivity.this, String.valueOf(event.values[0]), Toast.LENGTH_SHORT).show();
        } else if (event.sensor.getName().equals("Barometer Sensor")) {
            max = Math.max(event.values[0], max);
            min = Math.min(event.values[0], min);
            Log.i(TAG, "Barometer values Max:min " + max + ":" + min);
        } else {
            Log.i(TAG, event.sensor.getName() + " onSensorChange " + Arrays.toString(event.values));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, sensor.getName() + " onAccuracyChanged " + accuracy);
    }
}
