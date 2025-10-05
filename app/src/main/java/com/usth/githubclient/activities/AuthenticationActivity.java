package com.usth.githubclient.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.usth.githubclient.R;
import com.usth.githubclient.util.ThemeManager;
import com.usth.githubclient.viewmodel.AuthViewModel;

/**
 * Handles user authentication via Personal Access Token (PAT).
 */
public class AuthenticationActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private Button btnPatSignIn, btnSignOut;
    private ProgressBar progress;
    private TextView txtStatus;
    private EditText inputPat;
    private EditText inputUserNameOrEmail;
    private CheckBox checkboxShowPassword;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply the saved theme before creating the view.
        int savedTheme = ThemeManager.getSavedThemeMode(this);
        ThemeManager.applyTheme(savedTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        // Bind UI components.
        btnPatSignIn = findViewById(R.id.btnPatSignIn);
        btnSignOut   = findViewById(R.id.btnSignOut);
        inputPat     = findViewById(R.id.inputPat);
        inputUserNameOrEmail = findViewById(R.id.inputUserNameOrEmail);
        progress     = findViewById(R.id.progress);
        txtStatus    = findViewById(R.id.txtStatus);
        checkboxShowPassword = findViewById(R.id.checkboxShowPassword);

        // Toggle password visibility.
        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                inputPat.setTransformationMethod(null);
            } else {
                inputPat.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            inputPat.setSelection(inputPat.getText().length());
        });

        // Initialize the ViewModel.
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe the UI state from the ViewModel to update the UI accordingly.
        viewModel.getUiState().observe(this, state -> {
            switch (state.status) {
                case IDLE:
                    // Show sign-in button, hide progress and sign-out button.
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case LOADING:
                    // Show progress bar while loading.
                    progress.setVisibility(View.VISIBLE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case SIGNED_IN:
                    // On successful sign-in, navigate to MainActivity.
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Welcome, " + (state.username != null ? state.username : "user") + "!");

                    // Navigate to MainActivity and clear the task stack.
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                    break;

                case ERROR:
                    // Display an error message on failure.
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Error: " + (state.error != null ? state.error : "Login fail!!"));
                    break;
            }
        });

        // Handle PAT sign-in button click.
        btnPatSignIn.setOnClickListener(v -> {
            String pat = inputPat.getText().toString().trim();
            String identifier = inputUserNameOrEmail.getText().toString().trim(); // Read username/email

            if (pat.isEmpty()) {
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Please enter your PAT");
                return;
            }
            viewModel.signInWithPat(pat, identifier);
        });

        // Handle sign-out button click.
        btnSignOut.setOnClickListener(v -> viewModel.signOut());
    }
}