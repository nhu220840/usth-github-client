package com.usth.githubclient.data.repository;

import androidx.annotation.NonNull;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;

import java.util.List;

import retrofit2.Call;
/**
 * Implementation of UserRepository that fetches data from the remote GitHub API.
 */
public class UserRepositoryImpl implements UserRepository {

    private final ApiClient apiClient;

    // Dependency Injection: The ApiClient is provided via the constructor, which makes testing easier.
    public UserRepositoryImpl(@NonNull ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private GithubApiService apiService() {
        return apiClient.createService(GithubApiService.class);
    }

    @Override
    public Call<UserDto> getUser(String username) {
        // Delegate the network call to the ApiService instance created from ApiClient.
        return apiService().getUser(username);
    }

    @Override
    public Call<List<UserDto>> getFollowers(String username, int perPage, int page) {
        return apiService().getFollowers(username, perPage, page);
    }

    @Override
    public Call<List<UserDto>> getFollowing(String username, int perPage, int page) {
        return apiService().getFollowing(username, perPage, page);
    }

    @Override
    public Call<UserDto> authenticate() {
        return apiService().authenticate();
    }

    @Override
    public Call<SearchUsersResponseDto> searchUsers(String query, int page, int perPage) {
        return apiService().searchUsers(query, page, perPage);
    }

    @Override
    public Call<List<EventDto>> getUserEvents(String username, int page, int perPage) {
        return apiService().getUserEvents(username, page, perPage);
    }
}