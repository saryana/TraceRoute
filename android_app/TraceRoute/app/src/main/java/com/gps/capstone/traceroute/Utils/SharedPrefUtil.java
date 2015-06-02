package com.gps.capstone.traceroute.Utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by saryana on 6/2/15.
 */
public class SharedPrefUtil {

    /**
     * Gets an integer from the shared preferences
     * @param context Context we are getting called in
     * @param key Preference Key value
     * @param defaultValue Default value
     * @return Value fo the key
     */
    public static int getInt(Context context, int key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(key), defaultValue);
    }

    /**
     * Gets a float form the shared preference for that context
     * @param context Context we are getting called in
     * @param key Preference Key value
     * @param defaultValue Default value
     * @return Value of key
     */
    public static float getFloat(Context context, int key, float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getFloat(context.getString(key), defaultValue);
    }

    public static void putInt(Context context, int key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(context.getString(key), value)
                .apply();
    }

    public static void putFloat(Context context, int key, float value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(context.getString(key), value)
                .apply();
    }

    public static boolean getBoolean(Context context, int key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(key), defaultValue);
    }

    public static void putBoolean(Context context, int key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(key), value)
                .apply();
    }
}
