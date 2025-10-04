package com.usth.githubclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

// Lớp tiện ích để quản lý việc chuyển đổi giao diện (Theme).
public class ThemeManager {

    // Hằng số để định danh tệp SharedPreferences và key lưu trữ.
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";

    /**
     * Lưu lựa chọn giao diện của người dùng vào SharedPreferences.
     * @param context Context cần thiết để truy cập SharedPreferences.
     * @param mode Chế độ cần lưu (ví dụ: AppCompatDelegate.MODE_NIGHT_YES).
     */
    public static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    /**
     * Tải lựa chọn giao diện đã được lưu.
     * @return Chế độ đã lưu, hoặc chế độ mặc định của hệ thống nếu chưa có gì được lưu.
     */
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Mặc định là chế độ của hệ thống (MODE_NIGHT_FOLLOW_SYSTEM).
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Áp dụng một chế độ giao diện cho toàn bộ ứng dụng.
     * @param mode Chế độ cần áp dụng.
     */
    public static void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}