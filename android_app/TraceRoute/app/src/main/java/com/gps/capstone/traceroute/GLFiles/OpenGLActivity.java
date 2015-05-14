package com.gps.capstone.traceroute.GLFiles;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gps.capstone.traceroute.BasicActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;
import com.gps.capstone.traceroute.sensors.events.NewPathFromFile;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.squareup.otto.Subscribe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class OpenGLActivity extends BasicActivity implements OnClickListener {
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
    private Button mSaveButton;
    private Button mLoadButton;
    private Button mStartButton;
    private Button mStopButton;
    private ArrayList<float[]> mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        GLSurfaceView mGLSurface = new MySurfaceView(this);
        setContentView(R.layout.activity_open_gl);
        mDataProvider = null;
        mStepCount = 0;
        mSaveButton = (Button) findViewById(R.id.save_button);
        mLoadButton = (Button) findViewById(R.id.load_button);
        mStartButton = (Button) findViewById(R.id.start_path_button);
        mStopButton = (Button) findViewById(R.id.stop_path_button);
        mPath = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        USER_CONTROL = sharedPreferences.getBoolean(getString(R.string.pref_key_user_control), false);
        USE_CUBE = sharedPreferences.getBoolean(getString(R.string.pref_key_use_cube), true);
        USE_GYROSCOPE = sharedPreferences.getBoolean(getString(R.string.pref_key_use_gyroscope), true);
        USE_SHAPE = sharedPreferences.getBoolean(getString(R.string.pref_key_render_shape), true);

        mSaveButton.setOnClickListener(this);
        mLoadButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        Log.d(TAG, "User control: " + USER_CONTROL);
        Log.d(TAG, "Use gyroscope: " + USE_GYROSCOPE);
        BusProvider.getInstance().register(this);
        if (mDataProvider != null) {
            mDataProvider.register(USER_CONTROL, USE_GYROSCOPE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDataProvider != null) {
            mDataProvider.unregister();
        }
        BusProvider.getInstance().unregister(this);
        getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item) && item.getItemId() == R.id.remove) {
            for (String file: fileList()) {
                if (deleteFile(file)) {
                    Log.d(TAG, "removed " + file);
                } else {
                    Log.d(TAG, "Could not remove " + file);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_gl, menu);
        return true;
    }
    private float mHeading;
    private float mAltitude;
    @Subscribe
    public void onDataChange(NewDataEvent newDataEvent) {
        if (newDataEvent.type == EventType.DIRECTION_CHANGE) {
            float heading = (float) (newDataEvent.values[0] * 180f / Math.PI);
            if (heading < 0) {
                heading += 360;
            }
            mHeading = heading;
            ((TextView) findViewById(R.id.heading_direction)).setText("Heading Direction : " + heading);
        } else if (newDataEvent.type == EventType.ALTITUDE_CHANGE) {
            mAltitude = newDataEvent.values[0];
        }
    }

    @Subscribe
    public void onData(NewLocationEvent locationEvent) {
        // Another reference issue
        mPath.add(locationEvent.location.clone());
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prev_step_values);
        TextView tv = new TextView(this);
        tv.setText(String.format("Step %d at <%f, %f, %f> XY diff (%f, %f) with heading at the moment %f and altitude of %f",
                mStepCount,
                locationEvent.location[0], locationEvent.location[1], locationEvent.location[2],
                Math.sin(mHeading), Math.cos(mHeading),
                mHeading,
                mAltitude));
        linearLayout.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mStepCount++;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.save_button) {
            saveAction();
        } else if (id == R.id.load_button) {
            loadAction();
        } else if (id == R.id.start_path_button) {
            startPath();
        } else if (id == R.id.stop_path_button) {
            stopPath();
        } else {
            Log.e(TAG, "WHAT THE HELL DID WE CLICK?");
        }
    }

    /**
     * Starts the path drawing and listening
     */
    private void startPath() {
        // We can no longer start the path
        mStartButton.setEnabled(false);
        // They can now stop it
        mStopButton.setEnabled(true);
        mDataProvider = new SensorDataProvider(this);
        mDataProvider.register(USER_CONTROL, USE_GYROSCOPE);
    }

    /**
     * Stops the path listening and allows for the 3d moving of the path
     */
    private void stopPath() {
        // After they have stopped lets allow them to save it
        mSaveButton.setEnabled(true);
        mStopButton.setEnabled(false);
        // No longer want to be getting data?
        mDataProvider.unregister();
        mDataProvider = null;
        // This is where we would alert them if they want to save the path
    }

    /**
     * Dialog box that will look through our filesystem and find a previously saved path
     *
     */
    private void loadAction() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("Load Path from file:");
        final ArrayAdapter<String> files = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        files.addAll(fileList());
        builder.setAdapter(files, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pathName = files.getItem(which);
                Log.d(TAG, "Loading path " + pathName);
                FileInputStream fis;
                try {
                    fis = openFileInput(pathName);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    // Not sure how to get rid of such warning
                    ArrayList<float[]> path = (ArrayList<float[]>) ois.readObject();
                    BusProvider.getInstance().post(new NewPathFromFile(path));
                    ois.close();
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Dialog interface that will get the path name and save it to file
     */
    private void saveAction() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("Save Path to File Name:");
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);
        builder.setPositiveButton("Save Path!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pathName = editText.getText().toString();
                FileOutputStream fos;
                try {
                    // Don't need no snitches stealing our files
                    fos = openFileOutput(pathName, MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    // Ideally we would use parcelable, but lists are already serializable
                    oos.writeObject(mPath);
                    oos.close();
                    fos.close();
                    Log.d(TAG, "Wrote path to file with name " + pathName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
