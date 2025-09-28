package com.usth.githubclient.data.remote;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service definition for the GitHub REST API endpoints used by the app.
 */
public interface GithubApiService {
    @GET("users/{username}")
    Call<UserDto> getUser(@Path("username") String username);

    @GET("users/{username}/followers")
    Call<List<UserDto>> getFollowers(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    @GET("users/{username}/following")
    Call<List<UserDto>> getFollowing(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    @GET("users/{username}/repos")
    Call<List<RepoDto>> getUserRepositories(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );

    @GET("repos/{owner}/{repo}")
    Call<RepoDto> getRepository(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    @GET("user")
    Call<UserDto> authenticate();

    @GET("user/repos")
    Call<List<RepoDto>> getAuthenticatedRepositories(
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );

    @GET("search/users")
    Call<SearchUsersResponseDto> searchUsers(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("search/repositories")
    Call<SearchRepoResponseDto> searchRepos(@Query("q") String query,
                                            @Query("page") int page,
                                            @Query("per_page") int perPage);
}