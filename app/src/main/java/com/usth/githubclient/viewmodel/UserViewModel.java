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

// ViewModel for managing and providing user profile data to the UI.
public class UserViewModel extends ViewModel {

    // LiveData to hold the UI state.
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

    // Default constructor using ServiceLocator for dependencies.
    public UserViewModel() {
        this(ServiceLocator.getInstance().authRepository(), buildDefaultUserRepository(), ServiceLocator.getInstance().userMapper());
    }

    // Constructor for dependency injection.
    public UserViewModel(@NonNull AuthRepository authRepository,
                         @NonNull UserRepository userRepository,
                         @NonNull UserMapper userMapper) {
        this.authRepository = Objects.requireNonNull(authRepository, "authRepository == null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Helper to create a default UserRepository instance.
    private static UserRepository buildDefaultUserRepository() {
        ApiClient apiClient = new ApiClient();
        return new UserRepositoryImpl(apiClient);
    }

    // Exposes the UI state as LiveData to the UI.
    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    // Loads a user profile. If username is null or empty, it loads the authenticated user's profile.
    public void loadUserProfile(@Nullable String username) {
        String normalized = username == null ? "" : username.trim();
        boolean viewingAuthenticatedUser = normalized.isEmpty();

        // Avoid reloading if the same profile is already loaded.
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

        // Fetch data on a background thread.
        final String requestedUsername = normalized;
        executorService.execute(() -> {
            if (viewingAuthenticatedUser) {
                fetchAuthenticatedProfile();
            } else {
                fetchUserProfileByUsername(requestedUsername);
            }
        });
    }

    // Checks if there's already a profile in the current UI state.
    private boolean hasExistingProfile() {
        UserUiState state = uiState.getValue();
        return state != null && state.getProfile() != null;
    }

    // Tries to load the user profile from the cached session data.
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

    // Fetches the profile of the currently authenticated user.
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

    // Fetches a user's profile by their username.
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

    // Retries the last failed profile load operation.
    public void retry() {
        if (displayingAuthenticatedProfile || currentUsername == null) {
            loadUserProfile(null);
        } else {
            loadUserProfile(currentUsername);
        }
    }

    // Cleans up resources when the ViewModel is destroyed.
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    // Represents the different states of the user profile UI.
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