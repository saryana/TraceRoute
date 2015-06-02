package com.gps.capstone.traceroute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.gps.capstone.traceroute.GLFiles.OpenGLActivity;


public class FirstRunInfo extends ActionBarActivity implements OnClickListener, OnCheckedChangeListener {

    private EditText mStrideLength;
    private EditText mHeightFt;
    private EditText mHeightIn;
    private Button mContinueButton;
//    private Button mCalculateButton;
    private SharedPreferences mSharedPrefs;
    // True signifies a male, false a female
//    private Switch mGenderSwitch;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mSharedPrefs.getBoolean(getString(R.string.pref_key_first_run), true)) {
            Intent i = new Intent(this, OpenGLActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.activity_first_run_info);
        mStrideLength = (EditText) findViewById(R.id.calculated_stride_length);
        mHeightFt = (EditText) findViewById(R.id.info_height_feet);
        mHeightIn = (EditText) findViewById(R.id.info_height_inches);
        mContinueButton = (Button) findViewById(R.id.continue_button);
        mRadioGroup = (RadioGroup) findViewById(R.id.gender);
//        mCalculateButton = (Button) findViewById(R.id.calculate_button);
//        mGenderSwitch = (Switch) findViewById(R.id.gender_switch);
        mRadioGroup.setOnCheckedChangeListener(this);
        mStrideLength.setText("0");
        mHeightFt.setText("0");
        mHeightIn.setText("0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContinueButton.setOnClickListener(this);
//        mCalculateButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_run_info, menu);
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

    @Override
    public void onClick(View v) {
        int heightFt = Integer.valueOf(mHeightFt.getText().toString());
        int heightIn = Integer.valueOf(mHeightIn.getText().toString());
        float strideLength = Float.valueOf(mStrideLength.getText().toString());
        int totalHeight = heightFt * 12 + heightIn;

        // Are we calculating the stride?
        if (false) {
            strideLength = calculateStride(totalHeight);
            mStrideLength.setText(String.valueOf(strideLength));

        } else {
            // Has the user entered in the data?
            if (heightFt != 0) {
                if (strideLength == 0) {
                    strideLength = calculateStride(totalHeight);
                }
                mSharedPrefs.edit()
                        .putString(getString(R.string.pref_key_height_ft), "" + heightFt)
                        .putString(getString(R.string.pref_key_height_in), "" + heightIn)
                        .putInt(getString(R.string.pref_key_total_height_in), totalHeight)
                        .putFloat(getString(R.string.pref_key_stride_length), strideLength)
                        .apply();
                Intent i = new Intent(this, OpenGLActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(this, "Must Enter correct height", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Gets the stride length based off of height and gender
     *
     * https://www.walkingwithattitude.com/articles/features/how-to-measure-stride-or-step-length-for-your-pedometer
     * Men ~ .415 Women ~.413 => this only really matters if we want to display distance traveled
     *
     * @param totalHeight Height of user in inches
     * @return Stride length in inches
     */
    private float calculateStride(int totalHeight) {
        float strideLength;
        // Is this a male?
//        if (mGenderSwitch.isChecked()) {
            strideLength = .415f * totalHeight;
//        } else {
//            strideLength = .413f * totalHeight;
//        }
//        Log.d("STRIDE LEN", "Male true Female false: " + mGenderSwitch.isChecked() + " stride length " + strideLength);
        return strideLength;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.female:
                break;
            case R.id.male:
                break;
            case R.id.other:
                break;
        }
    }
}
