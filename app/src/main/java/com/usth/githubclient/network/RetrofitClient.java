package com.usth.githubclient.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public final class RetrofitClient {
    private static final String BASE_URL = "https://api.github.com/";
    private static GitHubApi api;

    public static GitHubApi api() {
        if (api == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            Interceptor headers = chain -> {
                Request original = chain.request();
                Request req = original.newBuilder()
                        .header("User-Agent", "usth-github-client")
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .build();
                return chain.proceed(req);
            };

            OkHttpClient ok = new OkHttpClient.Builder()
                    .addInterceptor(headers)
                    .addInterceptor(logging)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(ok)
                    .build();

            api = retrofit.create(GitHubApi.class);
        }
        return api;
    }

    private RetrofitClient() {}
}
