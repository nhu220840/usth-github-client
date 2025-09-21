package com.usth.githubclient.di;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.data.repository.RepoRepository;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;

/**
 * Simple service locator used during the early stages of the project before a full DI solution
 * is introduced.
 */
public final class ServiceLocator {

    private static volatile ServiceLocator instance;

    private final ApiClient apiClient;
    private final GithubApiService githubApiService;
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final AuthRepository authRepository;

    private ServiceLocator() {
        apiClient = new ApiClient();
        githubApiService = apiClient.createGithubApiService();
        userMapper = new UserMapper();
        repoMapper = new RepoMapper(userMapper);
        userRepository = new UserRepository(githubApiService, userMapper);
        repoRepository = new RepoRepository(githubApiService, repoMapper);
        authRepository = new AuthRepository(apiClient, githubApiService, userMapper, repoMapper);
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

    public ApiClient apiClient() {
        return apiClient;
    }

    public GithubApiService githubApiService() {
        return githubApiService;
    }

    public UserMapper userMapper() {
        return userMapper;
    }

    public RepoMapper repoMapper() {
        return repoMapper;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public RepoRepository repoRepository() {
        return repoRepository;
    }

    public AuthRepository authRepository() {
        return authRepository;
    }

    public static void reset() {
        synchronized (ServiceLocator.class) {
            instance = null;
        }
    }
}

