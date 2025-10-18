package com.usth.githubclient.data.repository;

import android.annotation.SuppressLint;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.domain.model.ReposDataEntry;
import com.usth.githubclient.domain.model.UserSessionData;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

/**
 * Handles authentication using a personal access token and exposes the resulting session data.
 */
public final class AuthRepository {

    private static final int DEFAULT_REPO_PAGE = 1;
    private static final int DEFAULT_REPO_PER_PAGE = 30;
    private static final String DEFAULT_SORT = "updated";

    private final ApiClient apiClient;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;

    private UserSessionData cachedSession;

    public AuthRepository(
            ApiClient apiClient,
            UserRepository userRepository,
            RepoRepository repoRepository,
            UserMapper userMapper,
            RepoMapper repoMapper
    ) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient == null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository == null");
        this.repoRepository = Objects.requireNonNull(repoRepository, "repoRepository == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.repoMapper = Objects.requireNonNull(repoMapper, "repoMapper == null");
    }

    /**
     * Signs out the user and clears the session data.
     */
    public void signOut() {
        cachedSession = null;
        apiClient.clearAuthToken();
    }

    /**
     * Gets the cached user session data.
     * @return The cached session data.
     */
    public UserSessionData getCachedSession() {
        return cachedSession;
    }

    /**
     * Fetches the authenticated user's profile.
     * @return The user profile data.
     * @throws IOException If a network error occurs.
     */
    private GitHubUserProfileDataEntry fetchAuthenticatedUser() throws IOException {
        Response<UserDto> response = userRepository.authenticate().execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.map(response.body());
        }
        apiClient.clearAuthToken();
        throw buildException("Unable to fetch authenticated user", response);
    }

    /**
     * Fetches the authenticated user's repositories.
     * @return A list of repository data.
     * @throws IOException If a network error occurs.
     */
    private List<ReposDataEntry> fetchAuthenticatedRepositories() throws IOException {
        Response<List<RepoDto>> response = repoRepository
                .getAuthenticatedRepositories(DEFAULT_REPO_PAGE, DEFAULT_REPO_PER_PAGE, DEFAULT_SORT)
                .execute();
        if (response.isSuccessful() && response.body() != null) {
            return repoMapper.mapList(response.body());
        }
        if (response.code() == 404) {
            return Collections.emptyList();
        }
        throw buildException("Unable to fetch repositories for authenticated user", response);
    }

    /**
     * Builds an IOException from a Retrofit response.
     * @param message The error message.
     * @param response The Retrofit response.
     * @return The created IOException.
     */
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
            return new IOException(message + " (Code: " + (response != null ? response.code() : "N/A") + ")");
        }
        return new IOException(message + ": " + errorBody);
    }
}