package com.usth.githubclient.activities;

import android.content.Intent;
import android.os.Bundle;
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
        // Giữ nguyên logic cho nút đăng nhập thật
        binding.signInButton.setOnClickListener(v -> {
            String passwordAsToken = getPasswordInput();
            viewModel.authenticate(passwordAsToken);
        });

        // Thêm listener cho nút mock data
        binding.mockDataButton.setOnClickListener(v -> viewModel.useMockSession());
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, this::renderState);
    }

    private void renderState(@Nullable AuthUiState state) {
        if (state == null) {
            return;
        }

        boolean loading = state.isLoading();
        // Vô hiệu hóa các trường và nút khi đang tải
        binding.userId.setEnabled(!loading);
        binding.userPassword.setEnabled(!loading);
        binding.signInButton.setEnabled(!loading);
        binding.mockDataButton.setEnabled(!loading); // Vô hiệu hóa cả nút mock

        // Hiển thị lỗi
        if (state.getErrorMessage() != null) {
            binding.userPassword.setError(state.getErrorMessage());
        } else {
            binding.userPassword.setError(null);
        }

        // Xử lý khi đăng nhập thành công (cho cả mock và thật)
        if (state.getSession() != null && !loading) {
            // Chuyển sang MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng AuthenticationActivity
        }
    }

    private String getPasswordInput() {
        CharSequence text = binding.userPassword.getText();
        return text == null ? "" : text.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}