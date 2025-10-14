package com.usth.githubclient.data.remote;

import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.remote.dto.UserEmailDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service definition for the GitHub REST API endpoints used by the app.
 */
public interface GithubApiService {

    // Get user details.
    @GET("users/{username}")
    Call<UserDto> getUser(@Path("username") String username);

    // Get a user's followers.
    @GET("users/{username}/followers")
    Call<List<UserDto>> getFollowers(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    // Get a user's following list.
    @GET("users/{username}/following")
    Call<List<UserDto>> getFollowing(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    // Get a user's repositories.
    @GET("users/{username}/repos")
    Call<List<RepoDto>> getUserRepositories(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );

    // Get a specific repository.
    @GET("repos/{owner}/{repo}")
    Call<RepoDto> getRepository(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    // Authenticate the user.
    @GET("user")
    Call<UserDto> authenticate();

    // Get the authenticated user's emails.
    @GET("user/emails")
    Call<List<UserEmailDto>> getUserEmails();

    // Get the authenticated user's repositories.
    @GET("user/repos")
    Call<List<RepoDto>> getAuthenticatedRepositories(
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );

    // Search for users.
    @GET("search/users")
    Call<SearchUsersResponseDto> searchUsers(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    // Search for repositories.
    @GET("search/repositories")
    Call<SearchRepoResponseDto> searchRepos(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int perPage);

    // Check if a repository is starred.
    @GET("user/starred/{owner}/{repo}")
    Call<Void> isRepoStarred(
            @Path("owner") String owner,
            @Path("repo") String repo);

    // Star a repository.
    @PUT("user/starred/{owner}/{repo}")
    Call<Void> starRepo(
            @Path("owner") String owner,
            @Path("repo") String repo);

    // Unstar a repository.
    @DELETE("user/starred/{owner}/{repo}")
    Call<Void> unstarRepo(
            @Path("owner") String owner,
            @Path("repo") String repo);

    // Get user events.
    @GET("users/{username}/events")
    Call<List<EventDto>> getUserEvents(
            @Path("username") String username,
            @Query("page") int page,
            @Query("per_page") int perPage
    );
}