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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.clans.fab.FloatingActionButton;
import com.gps.capstone.traceroute.BasicActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.gps.capstone.traceroute.sensors.events.NewPathFromFile;
import com.squareup.otto.Subscribe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class OpenGLActivity extends BasicActivity
                                implements OnClickListener,
                                OnShowcaseEventListener {
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    // Defines whether the user is in control of the map or not
    public static boolean USER_CONTROL;
    // Defines whether the camera follows path
    public static boolean FOLLOW_PATH;
    public static boolean USE_SHAPE;
    public static boolean USE_GYROSCOPE;
    // The source of our sensor data
    private SensorDataProvider mDataProvider;
    private int mStepCount;
    private ArrayList<float[]> mPath;
    private ShowcaseView mSV;
    private ImageView mPointer;
    private FloatingActionButton mFabStart;
    private FloatingActionButton mFabStop;
    private FloatingActionButton mFabSave;
    int n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        mStepCount = 0;

        mPointer = (ImageView) findViewById(R.id.pointer);
        mFabStart = (FloatingActionButton) findViewById(R.id.fab_start);
        mFabStop = (FloatingActionButton) findViewById(R.id.fab_stop);
        mFabSave = (FloatingActionButton) findViewById(R.id.fab_save);
        mFabStop.hide(false);
        mFabSave.hide(false);

        mPath = new ArrayList<>();
        n = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFabStart.setOnClickListener(this);
        mFabStop.setOnClickListener(this);
        mFabSave.setOnClickListener(this);

        mDataProvider = new SensorDataProvider(this);
        mPointer.setOnClickListener(this);

        USE_SHAPE = true;
        FOLLOW_PATH = false;
        USER_CONTROL = false;
        USE_GYROSCOPE = true;

        mDataProvider.register();
        mDataProvider.rotateModeFromGyroscope(USE_GYROSCOPE);

        if (sharedPreferences.getBoolean(getString(R.string.pref_key_first_run), true)) {
            firstRun();
            sharedPreferences.edit().putBoolean(getString(R.string.pref_key_first_run), false).apply();
        }

        BusProvider.getInstance().register(this);
    }

    private void firstRun() {
        mSV = new ShowcaseView.Builder(this)
                .setContentTitle("Path Starter")
                .setContentText("Hit play to start recording your path in 3D space! At the end press stop" +
                        " and you can move your path around or save it.")
                .setTarget(new ViewTarget(mFabStart))
                .doNotBlockTouches()
                .setStyle(com.github.amlcurran.showcaseview.R.style.ShowcaseButton)
                .setShowcaseEventListener(this).build();
        n++;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataProvider.unregister();
        BusProvider.getInstance().unregister(this);
        getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            if (item.getItemId() == R.id.remove) {
                for (String file : fileList()) {
                    if (deleteFile(file)) {
                        Log.d(TAG, "removed " + file);
                    } else {
                        Log.d(TAG, "Could not remove " + file);
                    }
                }
            } else if(item.getItemId() == R.id.load_path) {
                loadAction();
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

    @Override
    public void onClick(View v) {
        // Don't accept input when this is shown for now
        if (mSV != null && mSV.isShown()) {
            mSV.hide();
            return;
        }
        int id = v.getId();
        if (id == R.id.fab_start) {
            mDataProvider.rotateModeFromGyroscope(false);
            USE_SHAPE = false;

            mFabStart.hide(true);
            mFabSave.hide(true);
            mFabStop.show(true);
            startPath();
        } else if (id == R.id.fab_stop) {
            mFabStart.show(true);
            mFabSave.show(true);
            mFabStop.hide(true);
            stopPath();
        } else if (id == R.id.fab_save) {
            // This is where we would alert them if they want to save the path
            AlertDialog.Builder builder = new Builder(this);
            builder.setTitle("Path Complete! Save Path?");
            builder.setPositiveButton("Save!!!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveAction();
                    dialog.dismiss();
                }
            }).setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (id == R.id.pointer) {
            // Switch using the gyroscope
            USE_GYROSCOPE = !USE_GYROSCOPE;
            USER_CONTROL = !USER_CONTROL;
            mDataProvider.rotateModeFromGyroscope(USE_GYROSCOPE);
        } else {
            Log.e(TAG, "WHAT THE HELL DID WE CLICK?");
        }
    }

    /**
     * Starts the path drawing and listening
     */
    private void startPath() {
        FOLLOW_PATH = true;
        mDataProvider.startPath();
    }

    /**
     * Stops the path listening and allows for the 3d moving of the path
     */
    private void stopPath() {
        FOLLOW_PATH = false;
        // No longer want to be getting data?
        mDataProvider.stopPath();
    }

    /**
     * Dialog box that will look through our filesystem and find a previously saved path
     */
    private void loadAction() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("Load Path from file:");
        final ArrayAdapter<String> files = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        files.addAll(fileList());
        builder.setAdapter(files, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    /* Data change listeners */

    private float mHeading = 0;
    private float mAltitude;

    @Subscribe
    public void onDataChange(NewDataEvent newDataEvent) {
        if (newDataEvent.type == EventType.DIRECTION_CHANGE) {
            float heading = ((float) (Math.round(Math.toDegrees(newDataEvent.values[0]) + 360) %360));
            if (Math.abs(heading - mHeading) > 1) {
                RotateAnimation ra;
                ra = new RotateAnimation(
                        mHeading,
                        heading,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                );
                ra.setInterpolator(new AccelerateDecelerateInterpolator());
                ra.setDuration(250);
                ra.setFillAfter(true);
                mPointer.startAnimation(ra);
                mHeading = heading;
                ((TextView) findViewById(R.id.heading_direction)).setText("Heading Direction : " + mHeading);

            }
        } else if (newDataEvent.type == EventType.ALTITUDE_CHANGE) {
            mAltitude = newDataEvent.values[0];
        }
    }

    @Subscribe
    public void onData(NewLocationEvent locationEvent) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prev_step_values);
        if (locationEvent.location == null) {
            mPath.clear();
            mPath = new ArrayList<>();
            linearLayout.removeAllViewsInLayout();
            mStepCount = 0;
        } else {
            mPath.add(locationEvent.location.clone());
            TextView tv = new TextView(this);

            // Another reference issue
            tv.setText(String.format("Step %d at <%f, %f, %f> XY diff (%f, %f) with heading at the moment %f and altitude of %f",
                    mStepCount,
                    locationEvent.otherLocation[0], locationEvent.otherLocation[1], locationEvent.otherLocation[2],
                    Math.sin(mHeading), Math.cos(mHeading),
                    mHeading,
                    mAltitude));
            linearLayout.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mStepCount++;
        }
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        if (n == 1) {
            n = 0;
            mSV = new ShowcaseView.Builder(this)
                    .setContentTitle("VR Mode")
                    .setContentText("VR Mode allows you to observe your path in 3D space. User mode will allow you to " +
                            "pan around the map.")
                    .setTarget(new ViewTarget(mPointer))
                    .setStyle(com.github.amlcurran.showcaseview.R.style.ShowcaseButton)
                    .setShowcaseEventListener(this).build();
        }
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
    }
}
