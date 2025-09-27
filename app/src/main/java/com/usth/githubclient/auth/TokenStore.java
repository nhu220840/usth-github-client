package com.usth.githubclient.auth;

import android.content.Context;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenStore {
    private static final String FILE = "secure_prefs";
    private static final String KEY  = "gh_token";

    private static EncryptedSharedPreferences prefs(Context ctx) throws Exception {
        MasterKey mk = new MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                ctx,
                FILE,
                mk,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static void save(Context ctx, String token) throws Exception {
        prefs(ctx).edit().putString(KEY, token).apply();
    }

    public static String load(Context ctx) throws Exception {
        return prefs(ctx).getString(KEY, null);
    }

    public static void clear(Context ctx) throws Exception {
        prefs(ctx).edit().remove(KEY).apply();
    }
}
