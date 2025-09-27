package com.usth.githubclient.net;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceLocator {
    private static GitHubApi cached;

    public static GitHubApi api(Context ctx) {
        if (cached != null) return cached;

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(ctx))
                .build();

        cached = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApi.class);

        return cached;
    }
}
