package com.usth.githubclient.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.usth.githubclient.R;
import com.usth.githubclient.viewmodel.AuthViewModel;

public class AuthenticationActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private Button btnPatSignIn, btnSignOut;
    private ProgressBar progress;
    private TextView txtStatus;
    private EditText inputPat;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Bind UI
        btnPatSignIn = findViewById(R.id.btnPatSignIn);
        btnSignOut   = findViewById(R.id.btnSignOut);
        inputPat     = findViewById(R.id.inputPat);
        progress     = findViewById(R.id.progress);
        txtStatus    = findViewById(R.id.txtStatus);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe state để cập nhật UI
        viewModel.getUiState().observe(this, state -> {
            switch (state.status) {
                case IDLE:
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case LOADING:
                    progress.setVisibility(View.VISIBLE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case SIGNED_IN:
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Welcome, " + (state.username != null ? state.username : "user") + "!");

                    // Điều hướng sang MainActivity và đóng màn đăng nhập
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                    break;

                case ERROR:
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Error: " + (state.error != null ? state.error : "Login fail!!"));
                    break;
            }
        });

        // Đăng nhập bằng PAT
        btnPatSignIn.setOnClickListener(v -> {
            String pat = inputPat.getText().toString().trim();
            if (pat.isEmpty()) {
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Vui lòng dán PAT");
                return;
            }
            viewModel.signInWithPat(pat);
        });

        // Đăng xuất
        btnSignOut.setOnClickListener(v -> viewModel.signOut());
    }
}
