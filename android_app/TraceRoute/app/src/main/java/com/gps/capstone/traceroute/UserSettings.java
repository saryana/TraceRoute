package com.gps.capstone.traceroute;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.gps.capstone.traceroute.GLFiles.OpenGL;


public class UserSettings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i = null;
        switch (id) {
            case R.id.open_gl_view:
                i = new Intent(this, OpenGL.class);
                break;
            case R.id.debug_console:
                i = new Intent(this, DebugConsole.class);
                break;
        }

        if (i != null) {
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
