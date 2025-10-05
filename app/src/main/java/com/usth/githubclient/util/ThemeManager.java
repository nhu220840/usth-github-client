package com.usth.githubclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing the application's theme.
 */
public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";

    /**
     * Saves the selected theme mode to SharedPreferences.
     * @param context The context.
     * @param mode The theme mode to save (e.g., AppCompatDelegate.MODE_NIGHT_YES).
     */
    public static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    /**
     * Retrieves the saved theme mode from SharedPreferences.
     * @param context The context.
     * @return The saved theme mode, or the system default if none is saved.
     */
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Default to the system's theme setting.
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Applies the specified theme mode to the application.
     * @param mode The theme mode to apply.
     */
    public static void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}