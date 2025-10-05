package com.usth.githubclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.AuthenticationActivity;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.util.ThemeManager; // Import the new class

/**
 * Fragment for managing application settings.
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle the logout button.
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout());

        // Handle the Dark Mode switch.
        MaterialSwitch darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        int currentMode = ThemeManager.getSavedThemeMode(requireContext());

        // Update the switch state based on the current theme.
        darkModeSwitch.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Listen for changes to the switch.
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            ThemeManager.saveThemeMode(requireContext(), newMode);
            ThemeManager.applyTheme(newMode);
        });
    }

    /**
     * Logs out the user and navigates to the AuthenticationActivity.
     */
    private void logout() {
        if (getActivity() == null) return;
        ServiceLocator.getInstance().authRepository().signOut();
        Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}