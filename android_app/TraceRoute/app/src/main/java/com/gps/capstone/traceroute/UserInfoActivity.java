package com.gps.capstone.traceroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gps.capstone.traceroute.GLFiles.OpenGLActivity;
import com.gps.capstone.traceroute.Utils.SharedPrefUtil;


public class UserInfoActivity extends BasicActivity implements
                        OnClickListener,
                        OnCheckedChangeListener,
                        OnKeyListener, OnFocusChangeListener {

    private static final float STRIDE_LENGTH_MIN = 20f;
    private TextView mStrideLengthText;
    private EditText mStrideLengthEdit;
    private EditText mHeightFt;
    private EditText mHeightIn;
    private Button mContinueButton;
    private RadioGroup mRadioGroup;
    private float mStrideLength;
    private int mHeightFtVal;
    private int mHeightInVal;
    private int mTotalHeightVal;
    private StrideLength mStrideType;

    private enum StrideLength {
        MALE(.415f), FEMALE(.413f), INTERSEX(.413f);

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
        setContentView(R.layout.activity_first_run_info);

        mContinueButton = (Button) findViewById(R.id.continue_button);
        mHeightFt = (EditText) findViewById(R.id.info_height_feet);
        mHeightIn = (EditText) findViewById(R.id.info_height_inches);
        mRadioGroup = (RadioGroup) findViewById(R.id.gender);
        mStrideLengthEdit = (EditText) findViewById(R.id.calculated_stride_length_edit);
        mStrideLengthText = (TextView) findViewById(R.id.calculated_stride_length_text);

        boolean gotUserInfo = SharedPrefUtil.getBoolean(this, R.string.pref_key_got_user_info, false);

        if (gotUserInfo) {
            mStrideLength = SharedPrefUtil.getFloat(this, R.string.pref_key_stride_length, 0f);
            mHeightFtVal = SharedPrefUtil.getInt(this, R.string.pref_key_height_ft, 0);
            mHeightInVal = SharedPrefUtil.getInt(this, R.string.pref_key_height_in, 0);
            mTotalHeightVal = SharedPrefUtil.getInt(this, R.string.pref_key_total_height_in, 0);
            mStrideType = StrideLength.values()[SharedPrefUtil.getInt(this, R.string.pref_key_stride_type, 0)];
            mContinueButton.setEnabled(true);

            mHeightFt.setText(String.valueOf(mHeightFtVal));
            mHeightIn.setText(String.valueOf(mHeightInVal));
            mStrideLengthEdit.setText(String.valueOf(mStrideLength));
            mStrideLengthText.setText(String.valueOf(mStrideLength));
            int id = R.id.female;
            if (mStrideType == StrideLength.INTERSEX) {
                id = R.id.intersex;
            } else if (mStrideType == StrideLength.MALE) {
                id = R.id.male;
            }
            mRadioGroup.check(id);
        } else {
            mStrideLength = 0;
            mHeightInVal = 0;
            mHeightFtVal = 0;
            mTotalHeightVal = 0;
            mStrideType = StrideLength.FEMALE;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
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
        // The reason for this is that lollipop won't always
        // register the key presses like it does on sdk < 21
        mHeightIn.setOnFocusChangeListener(this);
        mHeightFt.setOnFocusChangeListener(this);
        mStrideLengthEdit.setOnKeyListener(this);
        // ideally block out the up key when the user hasn't entered in the info
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calculated_stride_length_text:
                mStrideLengthEdit.setVisibility(View.VISIBLE);
                mStrideLengthText.setVisibility(View.GONE);
                break;
            case R.id.continue_button:
                if (mStrideLength < STRIDE_LENGTH_MIN) {
                    Toast.makeText(UserInfoActivity.this, R.string.error_text_height, Toast.LENGTH_SHORT).show();
                } else {
                    saveValues();
                    Intent i = new Intent(this, OpenGLActivity.class);
                    startActivity(i);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mStrideLength > STRIDE_LENGTH_MIN) {
                saveValues();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Save all the values into the preferences
     */
    private void saveValues() {
        SharedPrefUtil.putInt(this, R.string.pref_key_height_ft, mHeightFtVal);
        SharedPrefUtil.putInt(this, R.string.pref_key_height_in, mHeightInVal);
        SharedPrefUtil.putInt(this, R.string.pref_key_total_height_in, mHeightFtVal);
        SharedPrefUtil.putFloat(this, R.string.pref_key_stride_length, mStrideLength);
        SharedPrefUtil.putInt(this, R.string.pref_key_stride_type, mStrideType.ordinal());
        SharedPrefUtil.putBoolean(this, R.string.pref_key_got_user_info, true);
    }

    /**
     * Gets the stride length based off of height and gender
     *
     * https://www.walkingwithattitude.com/articles/features/how-to-measure-stride-or-step-length-for-your-pedometer
     * Men ~ .415 Women ~.413 => this only really matters if we want to display distance traveled
     */
    private void calculateStride() {
        mHeightInVal = Integer.valueOf(mHeightIn.getText().toString());
        mHeightFtVal = Integer.valueOf(mHeightFt.getText().toString());

        mTotalHeightVal = mHeightFtVal * 12 + mHeightInVal;
        mStrideLength = mTotalHeightVal * mStrideType.getStrideLength();
        mStrideLengthText.setText(String.valueOf(mStrideLength));
        mStrideLengthEdit.setText(String.valueOf(mStrideLength));
        if (mStrideLength > STRIDE_LENGTH_MIN) {
            mContinueButton.setEnabled(true);
        } else {
            mContinueButton.setEnabled(false);
        }
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
                // Did we get input?
                if (val.length() != 0) {
                    calculateStride();
                }
                break;
            case R.id.info_height_inches:
                val = mHeightIn.getText().toString();
                if (val.length() != 0) {
                    calculateStride();
                }
                break;
            // Are we doing a manual override of the stride length
            case R.id.calculated_stride_length_edit:
                val = mStrideLengthEdit.getText().toString();
                if (val.length() == 0) break;
                mStrideLength = Float.valueOf(val);
                if (mStrideLength > STRIDE_LENGTH_MIN) {
                    mContinueButton.setEnabled(true);
                }
                break;
        }
        return false;
    }


    // Only needed because of lollipop fault
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String val;
        switch (v.getId()) {
            // Update the view on each key press hopefully only once. unless the user is mean.
            case R.id.info_height_feet:
                val = mHeightFt.getText().toString();
                // Did we get input?
                if (val.length() != 0) {
                    calculateStride();
                }
                break;
            case R.id.info_height_inches:
                val = mHeightIn.getText().toString();
                if (val.length() != 0) {
                    calculateStride();
                }
                break;
        }
    }

}
