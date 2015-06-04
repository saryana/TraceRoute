package com.gps.capstone.traceroute.GLFiles;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.clans.fab.FloatingActionButton;
import com.gps.capstone.traceroute.BasicActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.UserInfoActivity;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.Utils.SensorUtil.EventType;
import com.gps.capstone.traceroute.Utils.SharedPrefUtil;
import com.gps.capstone.traceroute.sensors.SensorDataProvider;
import com.gps.capstone.traceroute.sensors.events.NewDataEvent;
import com.gps.capstone.traceroute.sensors.events.NewLocationEvent;
import com.gps.capstone.traceroute.sensors.events.NewPathFromFile;
import com.gps.capstone.traceroute.sensors.events.PathCompletion;
import com.squareup.otto.Subscribe;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;


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
    private ShowcaseView mSV;
    private ImageView mPointer;
    private FloatingActionButton mFabStart;
    private FloatingActionButton mFabStop;
    private FloatingActionButton mFabSave;
    private View mCard;
    int n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        mStepCount = 0;
        SensorManager sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mPointer = (ImageView) findViewById(R.id.compass);
        mFabStart = (FloatingActionButton) findViewById(R.id.fab_start);
        mFabStop = (FloatingActionButton) findViewById(R.id.fab_stop);
        mFabSave = (FloatingActionButton) findViewById(R.id.fab_save);
        mFabStop.hide(false);
        mFabSave.hide(false);
        n = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        BusProvider.getInstance().register(this);

        if (!SharedPrefUtil.getBoolean(this, R.string.pref_key_got_user_info, false)) {
            Intent i = new Intent(this, UserInfoActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
        } else if (SharedPrefUtil.getBoolean(this, R.string.pref_key_first_run, true)) {
            SharedPrefUtil.putBoolean(this, R.string.pref_key_first_run, false);
            firstRun();
        } else if (getIntent().hasExtra("PATH_POS")) {
//            String fileName = getIntent().getStringExtra("PATH_POS");
//            FOLLOW_PATH = true;
            // Currently doesn't work need to go to the overflow menu
            USE_SHAPE = false;
            USE_GYROSCOPE = false;
            USER_CONTROL = true;
            mDataProvider.rotateModeFromGyroscope(false);
            ArrayList<float[]> path = (ArrayList<float[]>) getIntent().getSerializableExtra("PATH_POS");
//            Toast.makeText(this, "Loading path " + fileName, Toast.LENGTH_SHORT).show();
//            FileInputStream fis;
//            try {
//                fis = openFileInput(fileName);
//                ObjectInputStream ois = new ObjectInputStream(fis);
//                // Not sure how to get rid of such warning
//                ArrayList<float[]> path = (ArrayList<float[]>) ois.readObject();
//                Toast.makeText(this, "Loading path " + fileName + " " + path.size(), Toast.LENGTH_SHORT).show();
                int z = 0;
                // Draws the path slowly by doing things on the main thread
                for (int j = 0; j< 90_099_999; j++) {
                    z *= j * 9;
                }
                BusProvider.getInstance().post(new NewLocationEvent(null, null));
                BusProvider.getInstance().post(new NewPathFromFile(path, true));
//                ois.close();
//                fis.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    private void firstRun() {
        mSV = new ShowcaseView.Builder(this)
                .setContentTitle(R.string.showcase_path_start_text)
                .setContentText(R.string.showcase_path_start_description)
                .setTarget(new ViewTarget(mFabStart))
                .doNotBlockTouches()
                .hideOnTouchOutside()
                .setStyle(com.github.amlcurran.showcaseview.R.style.ShowcaseButton)
                .setShowcaseEventListener(this).build();
        mSV.hideButton();
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
        if (!super.onOptionsItemSelected(item) &&
                item.getItemId() == R.id.load_path) {
                loadAction();
            return true;
        }
        return false;
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
            saveAction();
//            // This is where we would alert them if they want to save the path
//            AlertDialog.Builder builder = new Builder(this)
//                    .setTitle(R.string.path_complete_title)
//                    .setPositiveButton(R.string.positive_button_path, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            saveAction();
//                            dialog.dismiss();
//                        }
//                    }).setNegativeButton(R.string.negative_button_path, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//            builder.show();
        } else if (id == R.id.compass) {
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
        USE_SHAPE = false;
        USE_GYROSCOPE = false;
        USER_CONTROL = false;
        mDataProvider.rotateModeFromGyroscope(false);
        mDataProvider.startPath();
    }

    /**
     * Stops the path listening and allows for the 3d moving of the path
     */
    private void stopPath() {
        FOLLOW_PATH = false;
        USE_GYROSCOPE = false;
        USER_CONTROL = true;
        mDataProvider.rotateModeFromGyroscope(false);
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
                USE_SHAPE = false;
                USE_GYROSCOPE = false;
                USER_CONTROL = true;
                mDataProvider.rotateModeFromGyroscope(false);
                String pathName = files.getItem(which);
                Log.d(TAG, "Loading path " + pathName);
                FileInputStream fis;
                try {
                    fis = openFileInput(pathName);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    // Not sure how to get rid of such warning
                    ArrayList<float[]> path = (ArrayList<float[]>) ois.readObject();
                    BusProvider.getInstance().post(new NewPathFromFile(path, true));
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
        builder.setTitle(R.string.save_path_dialog_title);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);
        builder.setPositiveButton(R.string.save_path_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pathName = editText.getText().toString();
                String message;
                if (mDataProvider.saveCurrentPath(pathName)) {
                    message = String.format(getString(R.string.successful_save), pathName);
                } else {
                    message = String.format(getString(R.string.unsuccessful_save), pathName);
                }
                Toast.makeText(OpenGLActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
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
                float end = heading;
                // Special case for when it is switching from greater to smaller
                if (heading < 20 && mHeading > 340) {
                    end = heading + 360;
                } else if (heading > 340 && mHeading < 20) {
                    end = heading - 360;
                }
                ra = new RotateAnimation(
                        mHeading,
                        end,
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
    public void onPathEnd(PathCompletion path) {
        mCard = LayoutInflater.from(this).inflate(R.layout.card, null);
        View.OnTouchListener touchListener = new OnTouchListener() {
            float startX = 0;
            float deltaX = 0;
            float originalX = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        originalX = v.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        deltaX = startX - event.getRawX();
                        v.setX(originalX - deltaX);
                        deltaX = Math.abs(deltaX);
                        v.setAlpha(1 - (deltaX / (v.getWidth()/2)));
                        break;
                    case MotionEvent.ACTION_UP:
                        if (deltaX > ((View)v.getParent()).getWidth() / 3) {
                            ((FrameLayout) findViewById(R.id.frame)).removeView(v);
                        } else {
                            v.setX(originalX);
                            v.setAlpha(1);
                        }
                        break;
                }
                float y = event.getRawY();
                float height = v.getHeight()/2;
                float viewY = v.getY();
                return y >= viewY && y <= viewY + height;
            }
        };
        mCard.setOnTouchListener(touchListener);
        ((TextView) mCard.findViewById(R.id.total_steps)).setText(String.valueOf(path.steps));
        double distanceFT = Math.round(path.distance) / 12;
        double distanceIN = Math.round(path.distance) % 12;
        ((TextView) mCard.findViewById(R.id.total_distance)).setText(distanceFT + " feet " + distanceIN + " inches");
        int roundedInitialAlt = Math.round(path.initialAltitude);
        int roundedFinalAlt = Math.round(path.finalAltitude);
        ((TextView) mCard.findViewById(R.id.init_alt)).setText(roundedInitialAlt + " feet");
        ((TextView) mCard.findViewById(R.id.final_alt)).setText(roundedFinalAlt + " feet");
        ((TextView) mCard.findViewById(R.id.alt_change)).setText(roundedFinalAlt-roundedInitialAlt + " feet");
        ((ViewGroup) findViewById(R.id.frame)).addView(mCard);
    }

    /**
     * This was used for debugging and can probably be removed...
     */
    @Subscribe
    public void onData(NewLocationEvent locationEvent) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prev_step_values);
        if (locationEvent.location != null) {
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
                    .setContentTitle(R.string.showcase_vr_mode)
                    .setContentText(R.string.showcase_vr_mode_description)
                    .setTarget(new ViewTarget(mPointer))
                    .setStyle(com.github.amlcurran.showcaseview.R.style.ShowcaseButton)
                    .hideOnTouchOutside()
                    .doNotBlockTouches()
                    .setShowcaseEventListener(this)
                    .build();
            mSV.hideButton();
        }
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
        // Currently not used
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
        // Currently not used
    }
}
