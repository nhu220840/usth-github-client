package com.usth.githubclient.net;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GitHubApi {
    @GET("user")
    Call<Map<String, Object>> me();

    // thêm: lấy danh sách email của user (cần scope user:email để thấy private emails)
    @GET("user/emails")
    Call<List<Map<String, Object>>> emails();
}
