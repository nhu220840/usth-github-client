package com.usth.githubclient.di;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;

/**
 * Very small dependency container to make mapper & repository instances
 * available across the app without pulling in a full DI framework just yet.
 */
public final class ServiceLocator {

    private static volatile ServiceLocator instance;

    private final UserMapper userMapper;
    private final RepoMapper repoMapper;
    private final AuthRepository authRepository;

    private ServiceLocator() {
        userMapper = new UserMapper();
        repoMapper = new RepoMapper(userMapper);

        // Khởi tạo ApiClient
        ApiClient apiClient = new ApiClient();

        // Sửa lại dòng khởi tạo AuthRepository cho đúng với constructor mới
        // Bỏ apiService ra khỏi đây
        authRepository = new AuthRepository(apiClient, userMapper, repoMapper);
    }

    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    public UserMapper userMapper() {
        return userMapper;
    }

    public RepoMapper repoMapper() {
        return repoMapper;
    }

    public AuthRepository authRepository() {
        return authRepository;
    }

    /** Clears the singleton instance to make room for a brand new graph (mainly for tests). */
    public static void reset() {
        synchronized (ServiceLocator.class) {
            instance = null;
        }
    }
}