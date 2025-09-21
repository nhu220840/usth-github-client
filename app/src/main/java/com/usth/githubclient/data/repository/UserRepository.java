package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import retrofit2.Response;

/**
 * Repository that handles remote user related requests.
 */
public final class UserRepository {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PER_PAGE = 30;

    private final GithubApiService apiService;
    private final UserMapper userMapper;

    public UserRepository(GithubApiService apiService, UserMapper userMapper) {
        this.apiService = Objects.requireNonNull(apiService, "apiService == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
    }

    public GitHubUserProfileDataEntry fetchUserProfile(String username) throws IOException {
        return executeCall(apiService.getUser(username));
    }

    public List<GitHubUserProfileDataEntry> fetchFollowers(String username) throws IOException {
        return fetchFollowers(username, DEFAULT_PER_PAGE, DEFAULT_PAGE);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowers(String username, int perPage, int page) throws IOException {
        return executeListCall(apiService.getFollowers(username, perPage, page));
    }

    public List<GitHubUserProfileDataEntry> fetchFollowing(String username) throws IOException {
        return fetchFollowing(username, DEFAULT_PER_PAGE, DEFAULT_PAGE);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowing(String username, int perPage, int page) throws IOException {
        return executeListCall(apiService.getFollowing(username, perPage, page));
    }

    // === Helper Methods ===
    private GitHubUserProfileDataEntry executeCall(retrofit2.Call<UserDto> call) throws IOException {
        Response<UserDto> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.map(response.body());
        }
        throw buildException("API call failed", response);
    }

    private List<GitHubUserProfileDataEntry> executeListCall(retrofit2.Call<List<UserDto>> call) throws IOException {
        Response<List<UserDto>> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.mapList(response.body());
        }
        throw buildException("API list call failed", response);
    }

    private IOException buildException(String message, Response<?> response) {
        String errorBody = null;
        try {
            if (response != null && response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException ignored) { }
        return (errorBody == null || errorBody.isEmpty())
                ? new IOException(message)
                : new IOException(message + ": " + errorBody);
    }
}
