package com.usth.githubclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.usth.githubclient.auth.TokenStore;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.remote.dto.UserEmailDto;

import java.util.List;

import retrofit2.Response;

/**
 * ViewModel for handling authentication logic and state.
 */
public class AuthViewModel extends AndroidViewModel {

    /**
     * Represents the UI state for authentication.
     */
    public static final class AuthUiState {
        public enum Status { IDLE, LOADING, SIGNED_IN, ERROR }
        public final Status status;
        public final String error;
        public final String username;
        public AuthUiState(Status s, String e, String u) { status = s; error = e; username = u; }
        public static AuthUiState idle()     { return new AuthUiState(Status.IDLE, null, null); }
        public static AuthUiState loading()  { return new AuthUiState(Status.LOADING, null, null); }
        public static AuthUiState signedIn(String u){ return new AuthUiState(Status.SIGNED_IN, null, u); }
        public static AuthUiState error(String e)    { return new AuthUiState(Status.ERROR, e, null); }
    }

    private final MutableLiveData<AuthUiState> ui = new MutableLiveData<>(AuthUiState.idle());
    public LiveData<AuthUiState> getUiState() { return ui; }

    public AuthViewModel(@NonNull Application app) {
        super(app);
    }

    // ===== PAT Sign-in =====
    public void signInWithPat(String pat) { signInWithPat(pat, null); }

    /**
     * Signs in the user with a Personal Access Token (PAT).
     * @param pat The PAT.
     * @param identifier The username or email to verify against the PAT.
     */
    public void signInWithPat(String pat, @Nullable String identifier) {
        ui.postValue(AuthUiState.loading());

        new Thread(() -> {
            ApiClient apiClient = new ApiClient();
            try {
                // The AuthInterceptor will automatically add the header to all requests.
                GithubApiService api = apiClient.createService(pat, GithubApiService.class);

                // 1) Verify the PAT by fetching user info.
                Response<UserDto> meRes = api.authenticate().execute();
                if (!meRes.isSuccessful() || meRes.body() == null) {
                    clearStoredToken(apiClient);
                    ui.postValue(AuthUiState.error("PAT invalid (HTTP " + meRes.code() + ")"));
                    return;
                }

                UserDto me = meRes.body();
                String login = me != null ? me.getLogin() : null;
                String publicEmail = me != null ? me.getEmail() : null;

                // 2) If no identifier is required, sign in is successful.
                if (identifier == null || identifier.trim().isEmpty()) {
                    persistTokenAndSignIn(apiClient, pat, login);
                    return;
                }

                String id = identifier.trim();
                boolean isEmail = id.contains("@");

                // 3) Verify the username.
                if (!isEmail) {
                    if (login != null && login.equalsIgnoreCase(id)) {
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Username does not match PAT account"));
                    }
                    return;
                }

                // 4) Verify the public email.
                if (publicEmail != null && !publicEmail.isEmpty()) {
                    if (publicEmail.equalsIgnoreCase(id)) {
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Email (public) does not match PAT account"));
                    }
                    return;
                }

                // 5) Verify private emails (requires user:email scope).
                Response<List<UserEmailDto>> emailsRes = api.getUserEmails().execute();
                if (emailsRes.isSuccessful() && emailsRes.body() != null) {
                    boolean matched = false;
                    for (UserEmailDto item : emailsRes.body()) {
                        String email = item != null ? item.getEmail() : null;
                        if (email != null && id.equalsIgnoreCase(email)) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) {
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Email does not match PAT account"));
                    }
                } else {
                    clearStoredToken(apiClient);
                    ui.postValue(AuthUiState.error(
                            "Unable to verify email. Grant scope 'user:email' to PAT or enter username to confirm."
                    ));
                }
            } catch (Exception e) {
                try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                ui.postValue(AuthUiState.error(e.getMessage()));
            }
        }).start();
    }

    /**
     * Signs out the user.
     */
    public void signOut() {
        ApiClient apiClient = new ApiClient();
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        apiClient.clearAuthToken();
        ui.postValue(AuthUiState.idle());
    }

    private void persistTokenAndSignIn(ApiClient apiClient, String pat, String login) {
        try {
            TokenStore.save(getApplication(), pat);
            apiClient.setAuthToken(pat);
            ui.postValue(AuthUiState.signedIn(login));
        } catch (Exception e) {
            clearStoredToken(apiClient);
            ui.postValue(AuthUiState.error(e.getMessage()));
        }
    }

    private void clearStoredToken(ApiClient apiClient) {
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        apiClient.clearAuthToken();
    }
}