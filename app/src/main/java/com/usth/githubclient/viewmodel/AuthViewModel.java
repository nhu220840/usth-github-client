package com.usth.githubclient.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.model.MockDataFactory;
import com.usth.githubclient.domain.model.UserSessionData;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsible for handling authentication flows and exposing UI friendly state.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final ExecutorService executorService;
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>(AuthUiState.idle());

    public AuthViewModel() {
        this(ServiceLocator.getInstance().authRepository());
    }

    public AuthViewModel(@NonNull AuthRepository authRepository) {
        this.authRepository = Objects.requireNonNull(authRepository, "authRepository == null");
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }

    public void authenticate(String personalAccessToken) {
        final String token = personalAccessToken == null ? "" : personalAccessToken.trim();
        if (token.isEmpty()) {
            uiState.setValue(AuthUiState.error("Please enter a personal access token."));
            return;
        }

        uiState.setValue(AuthUiState.loading());
        executorService.execute(() -> {
            try {
                UserSessionData session = authRepository.authenticate(token);
                uiState.postValue(AuthUiState.success(session));
            } catch (IOException | RuntimeException exception) {
                String message = exception.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Authentication failed. Please try again.";
                }
                uiState.postValue(AuthUiState.error(message));
            }
        });
    }

    public void useMockSession() {
        uiState.setValue(AuthUiState.success(MockDataFactory.mockUserSession()));
    }

    public void clearError() {
        AuthUiState current = uiState.getValue();
        if (current != null && current.getErrorMessage() != null) {
            uiState.setValue(new AuthUiState(current.isLoading(), current.getSession(), null));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    /**
     * Represents the immutable state of the authentication screen.
     */
    public static final class AuthUiState {
        private final boolean loading;
        private final UserSessionData session;
        private final String errorMessage;

        private AuthUiState(boolean loading, UserSessionData session, String errorMessage) {
            this.loading = loading;
            this.session = session;
            this.errorMessage = errorMessage;
        }

        public static AuthUiState idle() {
            return new AuthUiState(false, null, null);
        }

        public static AuthUiState loading() {
            return new AuthUiState(true, null, null);
        }

        public static AuthUiState success(UserSessionData session) {
            return new AuthUiState(false, session, null);
        }

        public static AuthUiState error(String message) {
            return new AuthUiState(false, null, message);
        }

        public boolean isLoading() {
            return loading;
        }

        public UserSessionData getSession() {
            return session;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}