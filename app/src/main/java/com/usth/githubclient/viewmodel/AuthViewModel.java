package com.usth.githubclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.usth.githubclient.auth.TokenStore;
import com.usth.githubclient.net.GitHubApi;
import com.usth.githubclient.net.ServiceLocator;

import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

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
        // Khôi phục phiên từ PAT (mã hoá)
        try {
            String pat = TokenStore.load(app);
            if (pat != null && !pat.isEmpty()) {
                ui.postValue(AuthUiState.signedIn(null));
            }
        } catch (Exception ignored) {}
    }

    // ===== PAT Sign-in (có thể kèm username/email để xác nhận) =====
    public void signInWithPat(String pat) { signInWithPat(pat, null); }

    public void signInWithPat(String pat, @Nullable String identifier) {
        ui.postValue(AuthUiState.loading());

        new Thread(() -> {
            try {
                // Lưu PAT mã hoá -> AuthInterceptor sẽ tự gắn header cho mọi request
                TokenStore.save(getApplication(), pat);

                GitHubApi api = ServiceLocator.api(getApplication());

                // 1) /user
                Response<Map<String, Object>> meRes = api.me().execute();
                if (!meRes.isSuccessful() || meRes.body() == null) {
                    try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                    ui.postValue(AuthUiState.error("PAT invalid (HTTP " + meRes.code() + ")"));
                    return;
                }

                Map<String, Object> me = meRes.body();
                String login = me.get("login") != null ? String.valueOf(me.get("login")) : null;
                String publicEmail = me.get("email") != null ? String.valueOf(me.get("email")) : null;

                // 2) Không yêu cầu định danh -> thành công
                if (identifier == null || identifier.trim().isEmpty()) {
                    ui.postValue(AuthUiState.signedIn(login));
                    return;
                }

                String id = identifier.trim();
                boolean isEmail = id.contains("@");

                // 3) Xác minh username
                if (!isEmail) {
                    if (login != null && login.equalsIgnoreCase(id)) {
                        ui.postValue(AuthUiState.signedIn(login));
                    } else {
                        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                        ui.postValue(AuthUiState.error("Username không khớp với tài khoản PAT"));
                    }
                    return;
                }

                // 4) Xác minh email (public trước)
                if (publicEmail != null && !publicEmail.isEmpty()) {
                    if (publicEmail.equalsIgnoreCase(id)) {
                        ui.postValue(AuthUiState.signedIn(login));
                    } else {
                        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                        ui.postValue(AuthUiState.error("Email (public) does not match PAT account"));
                    }
                    return;
                }

                // 5) Email private -> /user/emails (cần scope user:email)
                Response<List<Map<String, Object>>> emailsRes = api.emails().execute();
                if (emailsRes.isSuccessful() && emailsRes.body() != null) {
                    boolean matched = false;
                    for (Map<String, Object> item : emailsRes.body()) {
                        Object ev = item.get("email");
                        if (ev != null && id.equalsIgnoreCase(String.valueOf(ev))) {
                            matched = true; break;
                        }
                    }
                    if (matched) {
                        ui.postValue(AuthUiState.signedIn(login));
                    } else {
                        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                        ui.postValue(AuthUiState.error("Email does not match PAT account"));
                    }
                } else {
                    try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
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

    public void signOut() {
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        ui.postValue(AuthUiState.idle());
    }
}
