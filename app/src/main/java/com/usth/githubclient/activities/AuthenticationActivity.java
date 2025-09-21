package com.usth.githubclient.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.usth.githubclient.R;
import com.usth.githubclient.databinding.ActivityAuthenticationBinding;
import com.usth.githubclient.viewmodel.AuthViewModel;
import com.usth.githubclient.viewmodel.AuthViewModel.AuthUiState;

/**
 * Presents the authentication screen and reacts to login events.
 */
public class AuthenticationActivity extends AppCompatActivity {

    private ActivityAuthenticationBinding binding;
    private AuthViewModel viewModel;
    private TextWatcher tokenWatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.signInButton.setOnClickListener(v -> viewModel.authenticate(getTokenInput()));
        binding.mockDataButton.setOnClickListener(v -> viewModel.useMockSession());

        tokenWatcher = new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tokenInputLayout.setError(null);
                binding.errorMessage.setVisibility(View.GONE);
                viewModel.clearError();
            }
        };
        binding.personalAccessTokenInput.addTextChangedListener(tokenWatcher);
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, this::renderState);
    }

    private void renderState(@Nullable AuthUiState state) {
        if (state == null) {
            return;
        }

        boolean loading = state.isLoading();
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.personalAccessTokenInput.setEnabled(!loading);
        binding.signInButton.setEnabled(!loading);
        binding.mockDataButton.setEnabled(!loading);

        if (state.getErrorMessage() != null) {
            binding.tokenInputLayout.setError(state.getErrorMessage());
            binding.errorMessage.setText(state.getErrorMessage());
            binding.errorMessage.setVisibility(View.VISIBLE);
        } else {
            binding.tokenInputLayout.setError(null);
            binding.errorMessage.setVisibility(View.GONE);
        }

        if (state.getSession() != null && !loading) {
            binding.successMessage.setText(getString(R.string.auth_success_message,
                    state.getSession().getUsername()));
            binding.successMessage.setVisibility(View.VISIBLE);
        } else {
            binding.successMessage.setVisibility(View.GONE);
        }
    }

    private String getTokenInput() {
        CharSequence text = binding.personalAccessTokenInput.getText();
        return text == null ? "" : text.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null && tokenWatcher != null) {
            binding.personalAccessTokenInput.removeTextChangedListener(tokenWatcher);
        }
        binding = null;
        tokenWatcher = null;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
