package com.usth.githubclient.auth;

import android.content.Context;
import android.content.SharedPreferences;

// Lớp này đóng gói logic để lưu trữ và truy xuất token từ SharedPreferences.
// SharedPreferences là một cơ chế lưu trữ đơn giản của Android, phù hợp để
// lưu các cặp key-value nhỏ như token, cài đặt,...
public class TokenStore {
    // Tên của tệp SharedPreferences.
    private static final String FILE = "secure_prefs";
    // Key để lưu trữ token.
    private static final String KEY  = "gh_token";

    // Phương thức trợ giúp để lấy đối tượng SharedPreferences.
    // Context.MODE_PRIVATE đảm bảo rằng chỉ ứng dụng của bạn mới có thể
    // truy cập vào tệp này.
    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // Lưu token vào SharedPreferences.
    public static void save(Context ctx, String token) {
        // .edit() để bắt đầu chỉnh sửa.
        // .putString(KEY, token) để thêm hoặc cập nhật giá trị.
        // .apply() để lưu thay đổi một cách bất đồng bộ (không chặn luồng chính).
        prefs(ctx).edit().putString(KEY, token).apply();
    }

    // Tải token từ SharedPreferences.
    public static String load(Context ctx) {
        // .getString(KEY, null) để lấy giá trị.
        // 'null' là giá trị mặc định sẽ được trả về nếu không tìm thấy key.
        return prefs(ctx).getString(KEY, null);
    }

    // Xóa token khỏi SharedPreferences.
    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY).apply();
    }
}