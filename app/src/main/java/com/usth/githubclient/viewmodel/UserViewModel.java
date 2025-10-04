package com.usth.githubclient.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.ApiClient;
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
    private final UserMapper userMapper;

    private String currentUsername;
    private boolean displayingAuthenticatedProfile;


    private static final String GENERIC_ERROR_MESSAGE = "Unable to load this profile right now.";
    private static final String AUTH_REQUIRED_ERROR_MESSAGE =
            "Sign in with a personal access token to view your profile.";

    public UserViewModel() {
        this(ServiceLocator.getInstance().authRepository(), buildDefaultUserRepository(), ServiceLocator.getInstance().userMapper());
    }

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
        return new UserRepositoryImpl(apiClient);
    }

    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    public void loadUserProfile(@Nullable String username) {
        String normalized = username == null ? "" : username.trim();
        boolean viewingAuthenticatedUser = normalized.isEmpty();

        if (viewingAuthenticatedUser) {
            if (displayingAuthenticatedProfile && hasExistingProfile()) {
                return;
            }
            if (tryLoadProfileFromSession()) {
                return;
            }
        } else if (normalized.equalsIgnoreCase(currentUsername) && hasExistingProfile()) {
            return;
        }

        displayingAuthenticatedProfile = viewingAuthenticatedUser;
        currentUsername = viewingAuthenticatedUser ? null : normalized;
        uiState.setValue(UserUiState.loading());

        final String requestedUsername = normalized;
        executorService.execute(() -> {
            // FIX: Replaced the undefined 'fetchAuthenticatedUser' variable
            // with the correctly scoped 'viewingAuthenticatedUser' boolean.
            if (viewingAuthenticatedUser) {
                fetchAuthenticatedProfile();
            } else {
                fetchUserProfileByUsername(requestedUsername);
            }
        });
    }

    private boolean hasExistingProfile() {
        UserUiState state = uiState.getValue();
        return state != null && state.getProfile() != null;
    }

    private boolean tryLoadProfileFromSession() {
        UserSessionData session = authRepository.getCachedSession();
        if (session == null) {
            return false;
        }
        Optional<GitHubUserProfileDataEntry> profile = session.getUserProfile();
        if (profile.isEmpty()) {
            return false;
        }
        GitHubUserProfileDataEntry cachedProfile = profile.get();
        currentUsername = cachedProfile.getUsername();
        displayingAuthenticatedProfile = true;
        uiState.setValue(UserUiState.success(cachedProfile));
        return true;
    }

    private void fetchAuthenticatedProfile() {
        try {
            Response<UserDto> response = userRepository.authenticate().execute();
            if (response.isSuccessful() && response.body() != null) {
                GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                currentUsername = profile.getUsername();
                displayingAuthenticatedProfile = true;
                uiState.postValue(UserUiState.success(profile));
                return;
            }

            displayingAuthenticatedProfile = true;
            if (response.code() == 401 || response.code() == 403) {
                uiState.postValue(UserUiState.error(AUTH_REQUIRED_ERROR_MESSAGE));
            } else {
                String errorMsg = "Unable to load this profile right now. (Code: " + response.code() + ")";
                uiState.postValue(UserUiState.error(errorMsg));
            }
        } catch (IOException exception) {
            displayingAuthenticatedProfile = true;
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = GENERIC_ERROR_MESSAGE;
            }
            uiState.postValue(UserUiState.error(message));
        }
    }

    private void fetchUserProfileByUsername(@NonNull String username) {
        try {
            Response<UserDto> response = userRepository.getUser(username).execute();
            if (response.isSuccessful() && response.body() != null) {
                GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                currentUsername = profile.getUsername();
                displayingAuthenticatedProfile = false;
                uiState.postValue(UserUiState.success(profile));
                return;
            }

            displayingAuthenticatedProfile = false;
            String errorMsg = "Unable to load this profile right now. (Code: " + response.code() + ")";
            uiState.postValue(UserUiState.error(errorMsg));
        } catch (IOException exception) {
            displayingAuthenticatedProfile = false;
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = GENERIC_ERROR_MESSAGE;
            }
            uiState.postValue(UserUiState.error(message));
        }
    }

    public void retry() {
        if (displayingAuthenticatedProfile || currentUsername == null) {
            loadUserProfile(null);
        } else {
            loadUserProfile(currentUsername);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    // The UserUiState class remains unchanged.
    public static final class UserUiState {
        private final boolean loading;
        private final GitHubUserProfileDataEntry profile;
        private final String errorMessage;

        private UserUiState(boolean loading,
                            GitHubUserProfileDataEntry profile,
                            String errorMessage) {
            this.loading = loading;
            this.profile = profile;
            this.errorMessage = errorMessage;
        }

        public static UserUiState idle() {
            return new UserUiState(false, null, null);
        }

        public static UserUiState loading() {
            return new UserUiState(true, null, null);
        }

        public static UserUiState success(@NonNull GitHubUserProfileDataEntry profile) {
            return new UserUiState(false, Objects.requireNonNull(profile, "profile == null"), null);
        }

        public static UserUiState error(@NonNull String message) {
            return new UserUiState(false, null, Objects.requireNonNull(message, "message == null"));
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
    }
}
