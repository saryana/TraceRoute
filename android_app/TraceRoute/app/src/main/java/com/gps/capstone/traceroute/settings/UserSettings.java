package com.gps.capstone.traceroute.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.gps.capstone.traceroute.DebugConsole;
import com.gps.capstone.traceroute.GLFiles.OpenGL;
import com.gps.capstone.traceroute.R;

import java.util.List;

public class UserSettings extends PreferenceActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getFragmentManager().beginTransaction().replace(android.R.id.content, new Prefs1Frag()).commit();

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        Log.d(TAG, "Checking validity of " + fragmentName);
        return true;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug_console, menu);
        return true;
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_dependencies);
        }
    }

}
