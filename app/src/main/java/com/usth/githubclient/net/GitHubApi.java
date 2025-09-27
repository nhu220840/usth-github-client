package com.usth.githubclient.net;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GitHubApi {
    @GET("user")
    Call<Map<String, Object>> me();
}
