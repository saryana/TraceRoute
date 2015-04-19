package com.gps.capstone.traceroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;

import com.gps.capstone.traceroute.GLFiles.OpenGL;
import com.gps.capstone.traceroute.settings.UserSettings;

/**
 * This will hopefully help with code reduction and make it so we can
 * have common code for activities here
 */
public abstract class BasicActivity extends ActionBarActivity {
    // Tag for debugging
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get status bar color somehow
        getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    /**
     * The menu is changing all the time so lets make it so we can have
     * all the possible cases here (Shouldn't be too many)
     */
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
                i = new Intent(this, OpenGL.class);
                break;
            case R.id.debug_console:
                i = new Intent(this, DebugConsole.class);
                break;
            case R.id.user_settings:
                i = new Intent(this, UserSettings.class);
                break;
            default:
                Log.d(TAG, "Unknown menu action");
        }
        if (i != null) {
            // This disallows back press
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
