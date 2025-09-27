package com.usth.githubclient.data.repository;

import android.annotation.SuppressLint;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
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
    // Bỏ apiService khỏi constructor vì chúng ta sẽ tạo nó khi cần
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;

    private UserSessionData cachedSession;

    // Sửa constructor: Bỏ GithubApiService ra
    public AuthRepository(
            ApiClient apiClient,
            UserMapper userMapper,
            RepoMapper repoMapper
    ) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.repoMapper = Objects.requireNonNull(repoMapper, "repoMapper == null");
    }

    @SuppressLint("NewApi")
    public UserSessionData authenticate(String personalAccessToken) throws IOException {
        if (personalAccessToken == null || personalAccessToken.isEmpty()) {
            throw new IllegalArgumentException("personalAccessToken cannot be null or empty");
        }

        // 1. Tạo một service mới với token được cung cấp
        // Điều này đảm bảo request chắc chắn có header xác thực
        GithubApiService service = apiClient.createService(personalAccessToken, GithubApiService.class);

        // 2. Dùng service vừa tạo để gọi API
        GitHubUserProfileDataEntry profile = fetchAuthenticatedUser(service);
        List<ReposDataEntry> repositories = fetchAuthenticatedRepositories(service);

        cachedSession = UserSessionData.builder(profile.getUsername(), personalAccessToken)
                .tokenType("Bearer")
                .userProfile(profile)
                .repositories(repositories)
                .lastSyncedAt(Instant.now())
                .build();

        // Sau khi xác thực thành công, đặt token vào apiClient chung để các repository khác có thể dùng
        apiClient.setAuthToken(personalAccessToken);

        return cachedSession;
    }

    public void signOut() {
        cachedSession = null;
        apiClient.clearAuthToken();
    }

    public UserSessionData getCachedSession() {
        return cachedSession;
    }

    // Sửa các phương thức fetch để nhận vào GithubApiService
    private GitHubUserProfileDataEntry fetchAuthenticatedUser(GithubApiService service) throws IOException {
        Response<UserDto> response = service.authenticate().execute();
        if (response.isSuccessful() && response.body() != null) {
            return userMapper.map(response.body());
        }
        apiClient.clearAuthToken();
        throw buildException("Unable to fetch authenticated user", response);
    }

    private List<ReposDataEntry> fetchAuthenticatedRepositories(GithubApiService service) throws IOException {
        Response<List<RepoDto>> response = service
                .getAuthenticatedRepositories(DEFAULT_REPO_PER_PAGE, DEFAULT_REPO_PAGE, DEFAULT_SORT)
                .execute();
        if (response.isSuccessful() && response.body() != null) {
            return repoMapper.mapList(response.body());
        }
        if (response.code() == 404) {
            return Collections.emptyList();
        }
        throw buildException("Unable to fetch repositories for authenticated user", response);
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
            return new IOException(message + " (Code: " + (response != null ? response.code() : "N/A") + ")");
        }
        return new IOException(message + ": " + errorBody);
    }
}