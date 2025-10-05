package com.usth.githubclient.di;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.data.repository.RepoRepository;
import com.usth.githubclient.data.repository.RepoRepositoryImpl;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.data.repository.UserRepositoryImpl;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;

/**
 * A simple dependency container to make mapper & repository instances
 * available across the app without using a full DI framework.
 */
public final class ServiceLocator {

    private static volatile ServiceLocator instance;

    private final ApiClient apiClient;
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final AuthRepository authRepository;


    private ServiceLocator() {
        // Initialize all dependencies.
        apiClient = new ApiClient();
        userMapper = new UserMapper();
        repoMapper = new RepoMapper(userMapper);

        userRepository = new UserRepositoryImpl(apiClient);
        repoRepository = new RepoRepositoryImpl(apiClient);
        authRepository = new AuthRepository(apiClient, userRepository, repoRepository, userMapper, repoMapper);
    }

    /**
     * Gets the singleton instance of the ServiceLocator.
     * @return The ServiceLocator instance.
     */
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

    /** Clears the singleton instance to make room for a brand new graph (mainly for tests). */
    public static void reset() {
        synchronized (ServiceLocator.class) {
            instance = null;
        }
    }
}