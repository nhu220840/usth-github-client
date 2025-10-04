package com.usth.githubclient.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.usth.githubclient.R;
import com.usth.githubclient.databinding.ActivityUserProfileBinding;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.model.UserSessionData;
import com.usth.githubclient.fragments.SettingsFragment;
import com.usth.githubclient.fragments.UserProfileFragment;

public class UserProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "com.usth.githubclient.extra.EXTRA_USERNAME";

    private ActivityUserProfileBinding binding;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable String username) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        if (!TextUtils.isEmpty(username)) {
            intent.putExtra(EXTRA_USERNAME, username);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();

        if (savedInstanceState == null) {
            attachProfileFragment(resolveUsername());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.user_profile_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void attachProfileFragment(@Nullable String username) {
        UserProfileFragment fragment = UserProfileFragment.newInstance(username);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, fragment, UserProfileFragment.TAG)
                .commit();
    }

    @Nullable
    private String resolveUsername() {
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra(EXTRA_USERNAME);
            if (!TextUtils.isEmpty(username)) {
                return username.trim();
            }
        }
        UserSessionData session = ServiceLocator.getInstance().authRepository().getCachedSession();
        if (session != null) {
            return session.getUsername();
        }
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gắn menu vào toolbar
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý khi item trên menu được chọn
        if (item.getItemId() == R.id.action_settings) {
            // Khi nhấn nút settings, chuyển sang SettingsFragment
            showSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsFragment() {
        // Thay thế fragment hiện tại bằng SettingsFragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, new SettingsFragment())
                .addToBackStack(null) // Cho phép quay lại fragment trước đó
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}