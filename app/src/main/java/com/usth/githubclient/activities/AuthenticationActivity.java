package com.usth.githubclient.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.usth.githubclient.R;
import com.usth.githubclient.viewmodel.AuthViewModel;

public class AuthenticationActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private Button btnSignIn, btnSignOut;
    private ProgressBar progress;
    private TextView txtStatus;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Bind UI
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        progress = findViewById(R.id.progress);
        txtStatus = findViewById(R.id.txtStatus);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe state to update the UI
        viewModel.getUiState().observe(this, state -> {
            switch (state.status) {
                case IDLE:
                    progress.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progress.setVisibility(View.VISIBLE);
                    btnSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;
                case SIGNED_IN:
                    progress.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Welcome, " + (state.username != null ? state.username : "user") + "!");

                    // Redirect to MainActivity() and close AuthenticationActivity()
                    startActivity(
                            new Intent(this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                    );
                    finish(); // no return to sign in display when click "Back"
                    break;
                case ERROR:
                    progress.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Error: " + (state.error != null ? state.error : "Login fail!!"));
                    break;
            }
        });

        // Actions
        btnSignIn.setOnClickListener(v -> viewModel.startLogin(this));
        btnSignOut.setOnClickListener(v -> viewModel.signOut());

        // IF activity was opened by forward from AuthCallbackActivity -> attempt
        Intent maybeRedirect  = getIntent();
        if (maybeRedirect != null && maybeRedirect.getData() != null) {
            handleRedirectIntent(maybeRedirect);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // When already have instance (singleTop), redirect will land here
        if (intent != null && intent.getData() != null) {
            handleRedirectIntent(intent);
        }
    }

    private void handleRedirectIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            // Authorize ViewModel to change code -> token and update state
            viewModel.handleAuthResponse(intent);
            // Delete data to avoid reprocessing if activity was recreated
            intent.setData(null);
            setIntent(intent);
        }
    }
}