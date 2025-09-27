package com.usth.githubclient.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.usth.githubclient.R;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import retrofit2.Response;

import com.usth.githubclient.auth.TokenStore;
import com.usth.githubclient.net.ServiceLocator;
import com.usth.githubclient.net.GitHubApi;

/*
  ViewModel manages:
  - PAT sign-in (signInWithPat)
  - signOut clears PAT (encrypted)
*/

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AUTH";

    public static final class AuthUiState {
        public enum Status { IDLE, LOADING, SIGNED_IN, ERROR}
        public final Status status;
        public final String error;
        public final String username;
        public AuthUiState(Status s, String e, String u) {status = s; error = e; username = u; }
        public static AuthUiState idle() { return new AuthUiState(Status.IDLE, null, null); }
        public static AuthUiState loading() { return new AuthUiState(Status.LOADING, null, null); }
        public static AuthUiState signedIn(String u) { return new AuthUiState(Status.SIGNED_IN, null, u); }
        public static AuthUiState error(String e) { return new AuthUiState(Status.ERROR, e, null); }
    }

    private final MutableLiveData<AuthUiState> ui = new MutableLiveData<>(AuthUiState.idle());
    public LiveData<AuthUiState> getUiState() { return ui; }

    private final AuthorizationService authService;
    private final AuthorizationServiceConfiguration serviceConfig;
    private final String clientId;
    private final Uri redirectUri;
    private final SharedPreferences prefs;

    public AuthViewModel(@NonNull Application app) {
        super(app);

        // Endpoints Github OAuth
        String authEndpoint = app.getString(R.string.github_auth_endpoint);   // https://github.com/login/oauth/authorize
        String tokenEndpoint = app.getString(R.string.github_token_endpoint); // https://github.com/login/oauth/access_token
        serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(authEndpoint),
                Uri.parse(tokenEndpoint)
        );
        authService = new AuthorizationService(app);

        clientId = app.getString(R.string.github_client_id);
        redirectUri = Uri.parse(
                app.getString(R.string.oauth_redirect_scheme)
                        + "://" + app.getString(R.string.oauth_redirect_host)
                        + app.getString(R.string.oauth_redirect_path)
                // -> com.usth.githubclient://oauth2redirect/callback
        );

        prefs = PreferenceManager.getDefaultSharedPreferences(app);

        // Restore session nếu có:
        // 1) Ưu tiên PAT (mã hoá)
        try {
            String pat = TokenStore.load(app);
            if (pat != null && !pat.isEmpty()) {
                // Có thể gọi /user để lấy username; để nhanh, set SIGNED_IN trước:
                ui.postValue(AuthUiState.signedIn(null));
                return;
            }
        } catch (Exception ignored) {}

        // 2) Token OAuth (legacy)
        String token = prefs.getString("gh_access_token", null);
        if (token != null && !token.isEmpty()) {
            ui.postValue(AuthUiState.signedIn(null));
        }
    }

    // -------- OAuth with AppAuth --------
    public void startLogin(Activity activity) {
        ui.postValue(AuthUiState.loading());

        AuthorizationRequest request = new AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        )
                .setScopes("read:user", "repo")
                .build(); // AppAuth auto PKCE (S256)

        // Intent sau khi user authorize xong -> quay về AuthenticationActivity
        Intent postAuth = new Intent(activity, com.usth.githubclient.activities.AuthenticationActivity.class)
                .setAction(Intent.ACTION_VIEW);

        PendingIntent completePI = PendingIntent.getActivity(
                activity, 0, postAuth,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent cancelPI = PendingIntent.getActivity(
                activity, 1, postAuth,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d(TAG, "redirectUri=" + redirectUri);
        Log.d(TAG, "AUTHZ_URL=" + request.toUri());

        authService.performAuthorizationRequest(request, completePI, cancelPI);
    }

    // Đổi code -> token, sau đó gọi /user để lấy username demo
    public void handleAuthResponse(Intent intent) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);

        if (resp == null) {
            ui.postValue(AuthUiState.error(ex != null ? ex.errorDescription : "Authorization failed"));
            return;
        }

        TokenRequest tokenReq = resp.createTokenExchangeRequest();
        authService.performTokenRequest(tokenReq, (TokenResponse tokenResp, AuthorizationException tokenEx) -> {
            if (tokenResp != null && tokenResp.accessToken != null) {
                String accessToken = tokenResp.accessToken;

                // Lưu tạm token OAuth (không mã hoá). Nếu muốn, có thể chuyển sang EncryptedSharedPreferences.
                prefs.edit().putString("gh_access_token", accessToken).apply();

                fetchCurrentUser(accessToken, new FetchUserCallback() {
                    @Override
                    public void onSuccess(String login) {
                        ui.postValue(AuthUiState.signedIn(login));
                    }
                    @Override
                    public void onError(String msg) {
                        ui.postValue(AuthUiState.error(msg));
                    }
                });
            } else {
                ui.postValue(AuthUiState.error(tokenEx != null ? tokenEx.errorDescription : "Token exchange failed"));
            }
        });
    }

    // -------- PAT Sign-in (Encrypted) --------
    public void signInWithPat(String pat) {
        ui.postValue(AuthUiState.loading());

        new Thread(() -> {
            try {
                // Lưu PAT (mã hoá) để interceptor tự gắn Authorization
                TokenStore.save(getApplication(), pat);

                // Verify bằng Retrofit (đã gắn interceptor)
                GitHubApi api = ServiceLocator.api(getApplication());
                Response<Map<String, Object>> res = api.me().execute();

                if (res.isSuccessful() && res.body() != null) {
                    String login = null;
                    Object v = res.body().get("login");
                    if (v != null) login = String.valueOf(v);

                    // Dọn token OAuth cũ (nếu có) để tránh trạng thái lẫn lộn
                    prefs.edit().remove("gh_access_token").apply();

                    ui.postValue(AuthUiState.signedIn(login));
                } else {
                    // PAT sai/thiếu scope: xoá token và báo lỗi
                    try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                    String msg = "PAT invalid (HTTP " + res.code() + ")";
                    ui.postValue(AuthUiState.error(msg));
                }
            } catch (Exception e) {
                try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                ui.postValue(AuthUiState.error(e.getMessage()));
            }
        }).start();
    }

    // -------- Sign out: xoá cả PAT (encrypted) & token OAuth cũ --------
    public void signOut() {
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        prefs.edit().remove("gh_access_token").apply();
        ui.postValue(AuthUiState.idle());
    }

    // -------- Demo fetch /user cho OAuth token (HttpURLConnection) --------
    private interface FetchUserCallback {
        void onSuccess(String login);
        void onError(String msg);
    }

    private void fetchCurrentUser(String token, FetchUserCallback cb) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api.github.com/user");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);      // OAuth access token
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "usth-github-client");

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line; while((line = br.readLine()) != null) sb.append(line);
                    JSONObject json = new JSONObject(sb.toString());
                    String login = json.optString("login", null);
                    cb.onSuccess(login);
                } else {
                    cb.onError("HTTP " + code + " when calling /user");
                }
            } catch (Exception e) {
                cb.onError(e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
