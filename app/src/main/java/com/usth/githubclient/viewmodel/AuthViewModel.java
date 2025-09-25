package com.usth.githubclient.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

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

/*
  ViewModel manages OAuth 2.0 + PKCE (Proof key for Code Exchange) with AppAuth (def...)
  startLogin(activity): opens browser for user to login to GitHub
  handleAuthResponse(intent): changes authorization code -> access token
  signOut(): deletes local token
  fetchCurrentUser(): calls/user to get login name
 */

public class AuthViewModel extends AndroidViewModel {
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
        String authEndpoint = app.getString(R.string.github_auth_endpoint); // https://github.com/login/oauth/authorize
        String tokenEndpoint = app.getString(R.string.github_token_endpoint);  // https://github.com/login/oauth/access_token
        serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(authEndpoint),
                Uri.parse(tokenEndpoint)
        );
        authService = new AuthorizationService(app);

        clientId = app.getString(R.string.github_client_id);
        redirectUri = Uri.parse(app.getString(R.string.oauth_redirect_scheme) + "://" + app.getString(R.string.oauth_redirect_host) + "/callback");

        prefs = PreferenceManager.getDefaultSharedPreferences(app);

        // If already have token from last time -> restore Signed In state (depending on whether you want to auto-verify or not)
        String token = prefs.getString("gh_access_token", null);
        if (token != null && !token.isEmpty()) {
            // May call fetchCurrentUser() to get username, but to be quick, set Signed In first
            ui.postValue(AuthUiState.signedIn(null));
            // Suggest: fetchCurrent(token,...) for username updating
        }
    }

    // Start the login process: open a Custom Tab for user authorization
    public void startLogin(Activity activity) {
        ui.postValue(AuthUiState.loading());

        AuthorizationRequest request = new AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        )
        // Choose minimum scope as needed (e.g. read only user profile and public repo)
        .setScopes("read:user", "repo")
        .build(); //AppAuth automated turn on PKCE (code challenge) default

        Intent authIntent = authService.getAuthorizationRequestIntent(request);
        activity.startActivity(authIntent);
    }

    // Handle intent redirect: change code -> token, then call/user to get username
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

                // TODO: production: using EncryptedSharedPreferences (Jetpack Security)
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

    // Log out: delete token and bring UI back to IDLE
    public void signOut() {
        prefs.edit().remove("gh_access_token").apply();
        ui.postValue(AuthUiState.idle());
    }

    // Call REST Github / user (demo with HttpURLConnection)
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
                conn.setRequestProperty("Authorization", "Bearer " + token);      // Bearer token (OAuth)
                conn.setRequestProperty("Accept", "application/vnd.github+json"); // Recommend present REST
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