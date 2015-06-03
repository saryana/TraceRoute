package com.gps.capstone.traceroute.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.gps.capstone.traceroute.R;

public class Prefs1Frag extends PreferenceFragment {
    public Prefs1Frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fragmented_preferences);
    }
}
