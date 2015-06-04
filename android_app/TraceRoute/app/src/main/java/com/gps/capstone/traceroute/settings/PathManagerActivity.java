package com.gps.capstone.traceroute.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.gps.capstone.traceroute.AdaptingAdapter;
import com.gps.capstone.traceroute.GLFiles.OpenGLActivity;
import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.sensors.events.NewPathFromFile;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;

public class PathManagerActivity extends AppCompatActivity implements OnItemClickListener, OnCheckedChangeListener, OnClickListener {

    // Do we remember list views?
    private ListView mListView;
    private AdaptingAdapter mArrayMulti;
    private Switch mManageModeSwitch;
    private Button mLoadButton;
    private Button mDeleteButton;
    private boolean mIsMulti;
    private int lastSelected;
    private ArrayList<String> mFiles;
    boolean response;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_manager);
        mListView = ((ListView) findViewById(R.id.saved_paths_list));
        mManageModeSwitch = (Switch) findViewById(R.id.manage_paths);
        mLoadButton = (Button) findViewById(R.id.load_path_button);
        mDeleteButton = (Button) findViewById(R.id.delete_path_button);
        lastSelected = -1;
        mFiles = new ArrayList<>();
        Collections.addAll(mFiles, fileList());
        response = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!response) return;
        FileInputStream fis;
        ArrayList<float[]> path = null;
        try {
            String fileName = mFiles.get(lastSelected);
            fis = openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            // Not sure how to get rid of such warning
            path = (ArrayList<float[]>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        OpenGLActivity.USE_SHAPE = false;
        OpenGLActivity.USE_GYROSCOPE = false;
        OpenGLActivity.USER_CONTROL = true;

        BusProvider.getInstance().post(new NewPathFromFile(path, true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManageModeSwitch.setOnCheckedChangeListener(this);
        mArrayMulti = new AdaptingAdapter(this, R.layout.saved_path, mFiles, mIsMulti);
        mListView.setAdapter(mArrayMulti);
        mListView.setOnItemClickListener(this);
        mLoadButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mIsMulti = mManageModeSwitch.isChecked();
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_path_manager, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.delete_check_mark);
        if (mIsMulti) {
            if (checkBox.isChecked()) {
                view.setBackgroundColor(Color.TRANSPARENT);
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.secondaryText));
            }
            checkBox.setChecked(!checkBox.isChecked());
        } else {
            checkBox.setChecked(true);
            view.setBackgroundColor(getResources().getColor(R.color.secondaryText));
            if (lastSelected != -1 && lastSelected != position) {
                View v = parent.getChildAt(lastSelected);
                if (v == null) return;
                v.setBackgroundColor(Color.TRANSPARENT);
                ((CheckBox) v.findViewById(R.id.delete_check_mark)).setChecked(false);
            }
            lastSelected = position;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mArrayMulti.setMultiSelect(isChecked);
        mIsMulti = isChecked;
        Log.d("CHECK", isChecked + "");
        if (mIsMulti) {
            mDeleteButton.setVisibility(View.VISIBLE);
            mLoadButton.setVisibility(View.GONE);
        } else {
            clearChecks();
            lastSelected = -1;
            mLoadButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.GONE);
        }
    }

    private void clearChecks() {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            View v = mListView.getChildAt(i);
            if (v == null){
                continue;
            }
            v.setBackgroundColor(Color.TRANSPARENT);
            ((CheckBox) v.findViewById(R.id.delete_check_mark)).setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.load_path_button) {
            response = true;
            finish();
        } else if (v.getId() == R.id.delete_path_button) {
            int size = mFiles.size();
            for (int i = 0; i < size; i++) {
                View view = mListView.getChildAt(i);
                if (view == null){
                    continue;
                }
                view.setBackgroundColor(Color.TRANSPARENT);
                if (((CheckBox) view.findViewById(R.id.delete_check_mark)).isChecked()) {
                    if (deleteFile(mFiles.get(i))) {
                        mFiles.remove(i);
                        i--;
                        size--;
                        Log.d("IT WORKED", "YES");
                    } else {
                        Log.d("WE FUCKED", "NO");
                    }
                }
            }
            mArrayMulti.notifyDataSetChanged();
        }
    }
}
