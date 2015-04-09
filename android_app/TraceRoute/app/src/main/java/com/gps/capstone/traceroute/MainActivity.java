package com.gps.capstone.traceroute;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gps.capstone.traceroute.listners.AccelerometerListener;
import com.gps.capstone.traceroute.listners.BarometerListener;
import com.gps.capstone.traceroute.listners.GravityListener;
import com.gps.capstone.traceroute.listners.GyroscopeListner;
import com.gps.capstone.traceroute.listners.LinearAccelerationListener;
import com.gps.capstone.traceroute.listners.StepCounterListener;
import com.gps.capstone.traceroute.listners.StepDetectorListener;

import com.gps.capstone.traceroute.GLFiles.OpenGL;

public class MainActivity extends ActionBarActivity {
    // Tag used for logging
    private final String TAG = this.getClass().getSimpleName();

    // The sensor data will provide us data and we can register things with it
    private SensorManager sensorManager;

    // Lets grab each of the sensors we will be using and their corresponding listener
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener;
    private Sensor gyroscope;
    private SensorEventListener gyroscopeListener;
    private Sensor barometer;
    private SensorEventListener barometerListener;
    private Sensor linearAcceleration;
    private SensorEventListener linearAccelerationListener;
    private Sensor gravity;
    private SensorEventListener gravityListener;
    private Sensor stepCounter;
    private SensorEventListener stepCounterListener;
    private Sensor stepDetector;
    private SensorEventListener stepDetectorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, OpenGL.class);
                startActivity(i);
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Grab the accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerListener = new AccelerometerListener((RelativeLayout) findViewById(R.id.acc_values));

        // Grab the gyroscope
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroscopeListener = new GyroscopeListner((RelativeLayout) findViewById(R.id.gyro_values));

        // Grab the barometer
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        barometerListener = new BarometerListener((RelativeLayout) findViewById(R.id.barr_values));

        // Grab the Linear Acceleration (Software sensor I think)
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        linearAccelerationListener = new LinearAccelerationListener((RelativeLayout) findViewById(R.id.lin_acc_values));

        // Grab the gravity sensor
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gravityListener = new GravityListener((RelativeLayout) findViewById(R.id.grav_values));

        // Grab the step counter
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounter == null) {
            Log.e(TAG, "FUCK");
        }
        stepCounterListener = new StepCounterListener((RelativeLayout) findViewById(R.id.step_vals));

        // Grab the step detector
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepDetectorListener = new StepDetectorListener((RelativeLayout) findViewById(R.id.step_detect_vals));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "Registering the listeners", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Registered the listeners");

        // Register all the sensors with the listeners
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(barometerListener, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(linearAccelerationListener, linearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gravityListener, gravity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepCounterListener, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepDetectorListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL, Sensor.REPORTING_MODE_SPECIAL_TRIGGER);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Toast.makeText(this, "Unregister the listeners", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Unregistered the listeners");

        // Unregister the listeners, I'm not sure how this will factor in with the phone going to sleep
        sensorManager.unregisterListener(accelerometerListener);
        sensorManager.unregisterListener(gyroscopeListener);
        sensorManager.unregisterListener(barometerListener);
        sensorManager.unregisterListener(linearAccelerationListener);
        sensorManager.unregisterListener(gravityListener);
        sensorManager.unregisterListener(stepCounterListener);
        sensorManager.unregisterListener(stepDetectorListener);
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

}
