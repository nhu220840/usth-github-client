package com.usth.githubclient.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the storage of the authentication token in SharedPreferences.
 */
public class TokenStore {
    private static final String FILE = "secure_prefs";
    private static final String KEY  = "gh_token";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /**
     * Saves the authentication token.
     * @param ctx The context.
     * @param token The token to save.
     */
    public static void save(Context ctx, String token) {
        prefs(ctx).edit().putString(KEY, token).apply();
    }

    /**
     * Loads the authentication token.
     * @param ctx The context.
     * @return The saved token, or null if not found.
     */
    public static String load(Context ctx) {
        return prefs(ctx).getString(KEY, null);
    }

    /**
     * Clears the stored authentication token.
     * @param ctx The context.
     */
    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY).apply();
    }
}