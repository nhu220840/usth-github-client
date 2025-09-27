package com.usth.githubclient.auth;

import android.content.Context;

import android.content.SharedPreferences;


public class TokenStore {
    private static final String FILE = "secure_prefs";
    private static final String KEY  = "gh_token";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static void save(Context ctx, String token) {
        prefs(ctx).edit().putString(KEY, token).apply();
    }

    public static String load(Context ctx) {
        return prefs(ctx).getString(KEY, null);
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY).apply();
    }
}
