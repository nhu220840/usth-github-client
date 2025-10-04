package com.usth.githubclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";

    // Lưu lựa chọn giao diện
    public static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    // Tải lựa chọn giao diện đã lưu
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Mặc định là chế độ của hệ thống
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    // Áp dụng giao diện
    public static void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}