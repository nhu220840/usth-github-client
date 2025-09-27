package com.usth.githubclient.net;

import android.content.Context;

import androidx.annotation.NonNull;

import com.usth.githubclient.auth.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context app;

    public AuthInterceptor(Context app) {
        this.app = app.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = null;
        try { token = TokenStore.load(app); } catch (Exception ignored) {}
        Request req = chain.request();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    // Fine-grained PAT dùng 'Bearer'; PAT classic cũng chấp nhận
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "usth-github-client")
                    .build();
        }
        return chain.proceed(req);
    }
}
