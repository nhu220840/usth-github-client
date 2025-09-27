package com.usth.githubclient.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://api.github.com/";
    private static Retrofit retrofit = null;
    private static String authToken = null;

    public static Retrofit getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "usth-github-client");
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public void setAuthToken(String token) {
        authToken = token;
        // Rebuild Retrofit client to use the new token
        retrofit = null;
        getClient();
    }

    public void clearAuthToken() {
        authToken = null;
        retrofit = null;
    }


    /**
     * THÊM PHƯƠNG THỨC NÀY VÀO
     * Creates a service with a specific token, useful for one-time calls like authentication.
     */
    public <T> T createService(String token, Class<T> serviceClass) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "usth-github-client")
                    .header("Authorization", "Bearer " + token);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        Retrofit tempRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return tempRetrofit.create(serviceClass);
    }

    // Phương thức createService cũ (nếu có) giữ nguyên
    public <T> T createService(Class<T> serviceClass) {
        return getClient().create(serviceClass);
    }
}