package com.gps.capstone.traceroute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gps.capstone.traceroute.GLFiles.OpenGLActivity;


public class FirstRunInfo extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener, OnKeyListener {

    private TextView mStrideLengthText;
    private EditText mStrideLengthEdit;
    private EditText mHeightFt;
    private EditText mHeightIn;
    private Button mContinueButton;
    private SharedPreferences mSharedPrefs;
    private RadioGroup mRadioGroup;
    private float mStrideLength;
    private int mHeightFtVal;
    private int mHeightInVal;
    private int mTotalHeightVal;
    private StrideLength mStrideType;

    private enum StrideLength {
        MALE(.415f), FEMALE(.413f), INTERSEX(.414f);

        private float strideLength;

        StrideLength(float n) {
            strideLength = n;
        }

        public float getStrideLength() {
            return strideLength;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mSharedPrefs.getBoolean(getString(R.string.pref_key_first_run), true)) {
            Intent i = new Intent(this, OpenGLActivity.class);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_first_run_info);

        mStrideLength = 0;
        mHeightInVal = 0;
        mHeightFtVal = 0;
        mTotalHeightVal = 0;
        mStrideType = StrideLength.FEMALE;

        mContinueButton = (Button) findViewById(R.id.continue_button);
        mHeightFt = (EditText) findViewById(R.id.info_height_feet);
        mHeightIn = (EditText) findViewById(R.id.info_height_inches);
        mRadioGroup = (RadioGroup) findViewById(R.id.gender);
        mStrideLengthEdit = (EditText) findViewById(R.id.calculated_stride_length_edit);
        mStrideLengthText = (TextView) findViewById(R.id.calculated_stride_length_text);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mContinueButton.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(this);
        mStrideLengthText.setOnClickListener(this);
        // Lets update the value as they are pressing the key
        mHeightFt.setOnKeyListener(this);
        mHeightIn.setOnKeyListener(this);
        mStrideLengthEdit.setOnKeyListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calculated_stride_length_text:
                mStrideLengthEdit.setVisibility(View.VISIBLE);
                mStrideLengthText.setVisibility(View.GONE);
                break;
            case R.id.continue_button:
                if (mStrideLength < 20) {
                    Toast.makeText(FirstRunInfo.this, "Please enter a valid height", Toast.LENGTH_SHORT).show();
                } else {
                    mSharedPrefs.edit()
                            .putString(getString(R.string.pref_key_height_ft), "" + mHeightFtVal)
                            .putString(getString(R.string.pref_key_height_in), "" + mHeightInVal)
                            .putInt(getString(R.string.pref_key_total_height_in), mTotalHeightVal)
                            .putFloat(getString(R.string.pref_key_stride_length), mStrideLength)
                            .apply();
                    Intent i = new Intent(this, OpenGLActivity.class);
                    startActivity(i);
                }
        }
    }

    /**
     * Gets the stride length based off of height and gender
     *
     * https://www.walkingwithattitude.com/articles/features/how-to-measure-stride-or-step-length-for-your-pedometer
     * Men ~ .415 Women ~.413 => this only really matters if we want to display distance traveled
     */
    private void calculateStride() {
        mTotalHeightVal = mHeightFtVal * 12 + mHeightInVal;
        mStrideLength = mTotalHeightVal * mStrideType.getStrideLength();
        mStrideLengthText.setText(String.valueOf(mStrideLength));
        mStrideLengthEdit.setText(String.valueOf(mStrideLength));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.female:
                mStrideType = StrideLength.FEMALE;
                break;
            case R.id.male:
                mStrideType = StrideLength.MALE;
                break;
            case R.id.intersex:
                mStrideType = StrideLength.INTERSEX;
                break;
        }
        calculateStride();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        String val;
        switch (v.getId()) {
            // Update the view on each key press hopefully only once. unless the user is mean.
            case R.id.info_height_feet:
                val = mHeightFt.getText().toString();
                if (val.length() == 0) break;
                mHeightFtVal = Integer.valueOf(val);
                if (mHeightFtVal > 3) {
                    mContinueButton.setEnabled(true);
                }
                calculateStride();
                break;
            case R.id.info_height_inches:
                val = mHeightIn.getText().toString();
                if (val.length() == 0) break;
                mHeightInVal = Integer.valueOf(val);
                calculateStride();
                break;
            // Are we doing a manual override of the stride length
            case R.id.calculated_stride_length_edit:
                val = mStrideLengthEdit.getText().toString();
                if (val.length() == 0) break;
                mStrideLength = Float.valueOf(val);
                if (mStrideLength > 30) {
                    mContinueButton.setEnabled(true);
                }
                break;
        }
        return false;
    }
}
