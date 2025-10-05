package com.usth.githubclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Import this class
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.AuthenticationActivity;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.util.ThemeManager; // Import the theme utility class.

/**
 * This Fragment is responsible for displaying the Settings screen, allowing the user
 * to change the theme and sign out.
 */
public class SettingsFragment extends Fragment {

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment from the fragment_settings.xml file.
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * This is where you should set up listeners and initial view states.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- CHANGE: Update the Toolbar's title ---
        // Get the hosting Activity and update its Toolbar title.
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                // Set the new title to "Settings" (from strings.xml).
                activity.getSupportActionBar().setTitle(getString(R.string.title_settings));
                // Clear the subtitle (e.g., /@username) that might be left over from the Profile screen.
                activity.getSupportActionBar().setSubtitle(null);
            }
        }
        // --- END OF CHANGE ---

        // --- Handle the Sign Out button ---
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout()); // Assign the click listener.

        // --- Handle the Dark Mode Switch ---
        MaterialSwitch darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        // Get the currently saved theme mode from SharedPreferences.
        int currentMode = ThemeManager.getSavedThemeMode(requireContext());

        // Update the "checked" state of the Switch to match the current mode.
        // If the current mode is Dark Mode (MODE_NIGHT_YES), the switch will be "on".
        darkModeSwitch.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Listen for changes when the user toggles the switch.
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Determine the new mode based on the "checked" state of the switch.
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            // Save the user's new preference.
            ThemeManager.saveThemeMode(requireContext(), newMode);
            // Immediately apply the new theme to the entire application.
            ThemeManager.applyTheme(newMode);
        });
    }

    /**
     * Performs the sign-out process.
     */
    private void logout() {
        // A safety check to ensure the Fragment is still attached to an Activity.
        if (getActivity() == null) return;

        // 1. Call the AuthRepository to clear the cached session and the token from ApiClient.
        ServiceLocator.getInstance().authRepository().signOut();

        // 2. Create an Intent to navigate back to the login screen (AuthenticationActivity).
        Intent intent = new Intent(getActivity(), AuthenticationActivity.class);

        // 3. These flags will clear all other Activities from the "history" (back stack),
        // ensuring the user cannot press the Back button to return to the main screen after signing out.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 4. Start the login screen.
        startActivity(intent);

        // 5. Close the current Activity (UserProfileActivity).
        getActivity().finish();
    }
}