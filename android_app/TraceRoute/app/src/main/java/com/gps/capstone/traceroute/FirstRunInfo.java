package com.gps.capstone.traceroute;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gps.capstone.traceroute.GLFiles.OpenGLActivity;


public class FirstRunInfo extends Activity implements OnClickListener {

    private EditText mStrideLength;
    private EditText mHeightFt;
    private EditText mHeightIn;
    private Button mContinueButton;
    private Button mCalculateButton;
    private SharedPreferences mSharedPrefs;


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
        mCalculateButton = (Button) findViewById(R.id.calculate_button);

        mStrideLength.setText("0");
        mHeightFt.setText("0");
        mHeightIn.setText("0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContinueButton.setOnClickListener(this);
        mCalculateButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_first_run_info, menu);
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
        if (v.getId() == R.id.calculate_button) {
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
//                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            } else {
                Toast.makeText(this, "Must Enter correct height", Toast.LENGTH_LONG).show();
            }
        }
    }

    private float calculateStride(int totalHeight) {
        // need to determine
        return .41f * totalHeight;
    }
}
