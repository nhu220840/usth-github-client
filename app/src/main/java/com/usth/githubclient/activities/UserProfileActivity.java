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

/**
 * Activity to display a user's profile.
 */
public class UserProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "com.usth.githubclient.extra.EXTRA_USERNAME";

    private ActivityUserProfileBinding binding;
    // --- START OF CHANGE ---
    // A flag to determine if the current profile being viewed belongs to the authenticated user.
    private boolean isViewingAuthenticatedUserProfile = false;
    // --- END OF CHANGE ---

    /**
     * Creates an Intent to start this activity.
     * @param context The context.
     * @param username The username of the profile to display. If null, displays the authenticated user's profile.
     * @return The created Intent.
     */
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

        // --- START OF CHANGE ---
        // Determine if the user is viewing their own profile. This is true if no username
        // is passed via the Intent, meaning the activity was likely launched from the main
        // profile tab.
        isViewingAuthenticatedUserProfile = getIntent().getStringExtra(EXTRA_USERNAME) == null;
        // --- END OF CHANGE ---

        if (savedInstanceState == null) {
            attachProfileFragment(resolveUsername());
        }
    }

    /**
     * Sets up the toolbar.
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.user_profile_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Attaches the UserProfileFragment to the activity.
     * @param username The username to display in the fragment.
     */
    private void attachProfileFragment(@Nullable String username) {
        UserProfileFragment fragment = UserProfileFragment.newInstance(username);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, fragment, UserProfileFragment.TAG)
                .commit();
    }

    /**
     * Resolves the username from the intent or the cached session.
     * @return The username to display.
     */
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
        // Inflate the menu for the toolbar.
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);

        // --- START OF CHANGE ---
        // Find the settings item in the menu.
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (settingsItem != null) {
            // Set the visibility of the settings item based on whether the user
            // is viewing their own profile.
            settingsItem.setVisible(isViewingAuthenticatedUserProfile);
        }
        // --- END OF CHANGE ---

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item selections.
        if (item.getItemId() == R.id.action_settings) {
            // Switch to SettingsFragment when the settings button is pressed.
            showSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Replaces the current fragment with SettingsFragment.
     */
    private void showSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, new SettingsFragment())
                .addToBackStack(null) // Allows returning to the previous fragment.
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}