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
        Response<UserDto> response = apiService.getUser(username).execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.map(response.body());
        }
        throw buildException("Unable to fetch profile for " + username, response);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowers(String username) throws IOException {
        return fetchFollowers(username, DEFAULT_PER_PAGE, DEFAULT_PAGE);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowers(String username, int perPage, int page)
            throws IOException {
        Response<List<UserDto>> response = apiService.getFollowers(username, perPage, page).execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.mapList(response.body());
        }
        throw buildException("Unable to fetch followers for " + username, response);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowing(String username) throws IOException {
        return fetchFollowing(username, DEFAULT_PER_PAGE, DEFAULT_PAGE);
    }

    public List<GitHubUserProfileDataEntry> fetchFollowing(String username, int perPage, int page)
            throws IOException {
        Response<List<UserDto>> response = apiService.getFollowing(username, perPage, page).execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.mapList(response.body());
        }
        throw buildException("Unable to fetch following for " + username, response);
    }

    private IOException buildException(String message, Response<?> response) {
        String errorBody;
        try {
            errorBody = response != null && response.errorBody() != null
                    ? response.errorBody().string()
                    : null;
        } catch (IOException ignored) {
            errorBody = null;
        }
        if (errorBody == null || errorBody.isEmpty()) {
            return new IOException(message);
        }
        return new IOException(message + ": " + errorBody);
    }
}

