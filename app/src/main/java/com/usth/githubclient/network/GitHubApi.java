package com.usth.githubclient.network;

import com.usth.githubclient.network.models.UserDto;
import com.usth.githubclient.network.models.UserDetailDto;
import com.usth.githubclient.network.models.SearchUsersResponse; // <— THÊM

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GitHubApi {

    @GET("users/{username}/followers")
    Call<List<UserDto>> getFollowers(
            @Path("username") String username,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage
    );

    @GET("users/{username}")
    Call<UserDetailDto> getUser(@Path("username") String username);

    // search many user following keyword
    @GET("search/users")
    Call<SearchUsersResponse> searchUsers(
            @Query("q") String query,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage
    );
}
