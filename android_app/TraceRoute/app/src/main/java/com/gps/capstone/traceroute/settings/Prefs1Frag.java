package com.gps.capstone.traceroute.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gps.capstone.traceroute.DebugConsole;
import com.gps.capstone.traceroute.GLFiles.OpenGL;
import com.gps.capstone.traceroute.R;

public class Prefs1Frag extends PreferenceFragment {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure default values are applied.  In a real app, you would
        // want this in a shared function that is used to retrieve the
        // SharedPreferences wherever they are needed.
//        PreferenceManager.setDefaultValues(getActivity(),
//                R.xml.advanced_preferences, false);
        Log.e(TAG, "On create");
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fragmented_preferences);
    }

    public Prefs1Frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e(TAG, "MENU");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_debug_console, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Depending on what the user clicks lets start that activity
        Intent i = null;
        switch (id) {
            case R.id.open_gl_view:
                i = new Intent(getActivity(), OpenGL.class);
                break;
            case R.id.debug_console:
                i = new Intent(getActivity(), DebugConsole.class);
                break;
        }
        if (i != null)
            startActivity(i);
        return super.onOptionsItemSelected(item);
    }

}
