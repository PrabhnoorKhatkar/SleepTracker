package edu.sjsu.android.sleeptracker;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    private static final String PREFS_NAME = "settings";
    private static final String DARK_MODE_KEY = "dark_mode";

    // Toggle the dark mode and save it to SharedPreferences
    public static void toggleDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DARK_MODE_KEY, !isDarkMode);
        editor.apply();

        applyTheme(context);
    }

    // Apply the theme based on the current preference
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);

        int desiredMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(desiredMode);
    }
}
