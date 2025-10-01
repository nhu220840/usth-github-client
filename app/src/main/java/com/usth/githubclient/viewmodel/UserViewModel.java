package com.usth.githubclient.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.data.repository.UserRepositoryImpl;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.domain.model.UserSessionData;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class UserViewModel extends ViewModel {

    private final MutableLiveData<UserUiState> uiState = new MutableLiveData<>(UserUiState.idle());
    private final ExecutorService executorService;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper; // Thêm UserMapper

    private String currentUsername;

    private static final String FALLBACK_USERNAME = "octocat";


    public UserViewModel() {
        this(ServiceLocator.getInstance().authRepository(), buildDefaultUserRepository(), ServiceLocator.getInstance().userMapper());
    }

    // Sửa constructor để nhận UserMapper
    public UserViewModel(@NonNull AuthRepository authRepository,
                         @NonNull UserRepository userRepository,
                         @NonNull UserMapper userMapper) {
        this.authRepository = Objects.requireNonNull(authRepository, "authRepository == null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.executorService = Executors.newSingleThreadExecutor();
    }

    private static UserRepository buildDefaultUserRepository() {
        ApiClient apiClient = new ApiClient();
        GithubApiService service = apiClient.createService(GithubApiService.class);
        return new UserRepositoryImpl(service);
    }

    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    public void loadUserProfile(@Nullable String username) {
        String normalized = username == null ? "" : username.trim();

        if (normalized.isEmpty()) {
            UserSessionData session = authRepository.getCachedSession();
            if (session != null) {
                Optional<GitHubUserProfileDataEntry> profile = session.getUserProfile();
                if (profile.isPresent()) {
                    GitHubUserProfileDataEntry cachedProfile = profile.get();
                    currentUsername = cachedProfile.getUsername();
                    uiState.setValue(UserUiState.success(cachedProfile, false));
                    return;
                }
                normalized = session.getUsername();
            }
            if (normalized == null || normalized.trim().isEmpty()) {
                normalized = FALLBACK_USERNAME;
            }
        }

        if (normalized.isEmpty()) {
            normalized = FALLBACK_USERNAME;
        }

        if (normalized.equalsIgnoreCase(currentUsername)
                && uiState.getValue() != null
                && uiState.getValue().getProfile() != null) {
            return;
        }

        currentUsername = normalized;
        uiState.setValue(UserUiState.loading());

        final String requestedUsername = normalized;
        executorService.execute(() -> {
            try {
                // Sửa đổi phần logic gọi API
                Response<UserDto> response = userRepository.getUser(requestedUsername).execute();
                if (response.isSuccessful() && response.body() != null) {
                    GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                    uiState.postValue(UserUiState.success(profile, false));
                } else {
                    String errorMsg = "Unable to load this profile right now. (Code: " + response.code() + ")";
                    uiState.postValue(UserUiState.error(errorMsg));
                }
            } catch (IOException exception) {
                String message = exception.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Unable to load this profile right now.";
                }
                uiState.postValue(UserUiState.error(message));
            }
        });
    }

    public void retry() {
        if (currentUsername == null && uiState.getValue() != null
                && uiState.getValue().getProfile() != null) {
            return;
        }
        loadUserProfile(currentUsername);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    // Lớp UserUiState giữ nguyên không đổi
    public static final class UserUiState {
        private final boolean loading;
        private final GitHubUserProfileDataEntry profile;
        private final String errorMessage;
        private final boolean usingMockData;

        private UserUiState(boolean loading,
                            GitHubUserProfileDataEntry profile,
                            String errorMessage,
                            boolean usingMockData) {
            this.loading = loading;
            this.profile = profile;
            this.errorMessage = errorMessage;
            this.usingMockData = usingMockData;
        }

        public static UserUiState idle() {
            return new UserUiState(false, null, null, false);
        }

        public static UserUiState loading() {
            return new UserUiState(true, null, null, false);
        }

        public static UserUiState success(@NonNull GitHubUserProfileDataEntry profile, boolean usingMockData) {
            return new UserUiState(false, Objects.requireNonNull(profile, "profile == null"), null, usingMockData);
        }

        public static UserUiState error(@NonNull String message) {
            return new UserUiState(false, null, Objects.requireNonNull(message, "message == null"), false);
        }

        public boolean isLoading() {
            return loading;
        }

        @Nullable
        public GitHubUserProfileDataEntry getProfile() {
            return profile;
        }

        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isUsingMockData() {
            return usingMockData;
        }
    }
}