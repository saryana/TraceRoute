package com.gps.capstone.traceroute.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.gps.capstone.traceroute.R;

public class PathManagerActivity extends AppCompatActivity implements OnCheckedChangeListener {

    // Do we remember list views?
    private ListView mListView;
    private ArrayAdapter<String> mArraySingle;
    private ArrayAdapter<String> mArrayMulti;
    private RadioGroup mRadioGroup;
    private boolean mIsLoadSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_manager);
        mListView = ((ListView) findViewById(R.id.saved_paths_list));
        mRadioGroup = (RadioGroup) findViewById(R.id.list_view_action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRadioGroup.setOnCheckedChangeListener(this);
        mIsLoadSelected = true;

        mArrayMulti = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, fileList());
        mArraySingle = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList());
        if (mIsLoadSelected) {
            mListView.setAdapter(mArraySingle);
        } else {
            mListView.setAdapter(mArrayMulti);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_path_manager, menu);
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
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mIsLoadSelected = checkedId == R.id.load_path;
        if (mIsLoadSelected) {
            mListView.setAdapter(mArraySingle);
        } else {
            mListView.setAdapter(mArrayMulti);
        }
    }
}
