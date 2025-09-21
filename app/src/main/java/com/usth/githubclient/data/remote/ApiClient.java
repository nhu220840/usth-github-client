package com.usth.githubclient.data.remote;

import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds Retrofit clients used to talk to the GitHub REST API.
 */
public final class ApiClient {

    private static final String BASE_URL = "https://api.github.com/";

    private final Retrofit retrofit;
    private volatile String authToken;

    public ApiClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(this::applyDefaultHeaders)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private Response applyDefaultHeaders(Interceptor.Chain chain) throws java.io.IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28");

        String token = authToken;
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }

    public GithubApiService createGithubApiService() {
        return retrofit.create(GithubApiService.class);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void clearAuthToken() {
        authToken = null;
    }
}

