package com.usth.githubclient.fragments;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.ContributionsListAdapter;
import com.usth.githubclient.databinding.FragmentUserProfileBinding;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.viewmodel.UserViewModel;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment to display a user's profile.
 */
public class UserProfileFragment extends Fragment {

    public static final String TAG = "UserProfileFragment";
    private static final String ARG_USERNAME = "arg_username";

    private FragmentUserProfileBinding binding;
    private UserViewModel viewModel;
    private String currentUsername;
    private int lastFetchedMonth = -1;
    private int lastFetchedYear = -1;
    private boolean isDateReceiverRegistered;

    private final BroadcastReceiver dateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAdded()) {
                return;
            }
            refreshContributionsForCurrentDate();
        }
    };

    /**
     * Creates a new instance of UserProfileFragment.
     * @param username The username of the profile to display.
     * @return A new instance of UserProfileFragment.
     */
    public static UserProfileFragment newInstance(@Nullable String username) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(username)) {
            args.putString(ARG_USERNAME, username);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);

        // Observer for contributions data
        viewModel.getContributions().observe(getViewLifecycleOwner(), contributions -> {
            if (contributions != null) {
                ContributionsListAdapter adapter = new ContributionsListAdapter(requireContext(), contributions);
                binding.contributionsGrid.setAdapter(adapter);
            }
        });

        updateContributionsTitle(Calendar.getInstance());

        String username = getArguments() == null ? null : getArguments().getString(ARG_USERNAME);
        viewModel.loadUserProfile(username);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerDateChangeReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContributionsForCurrentDate();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterDateChangeReceiver();
    }

    /**
     * Renders the UI based on the ViewModel's state.
     * @param state The current UI state.
     */
    private void renderState(@Nullable UserViewModel.UserUiState state) {
        if (binding == null || state == null) {
            return;
        }

        binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

        if (state.getErrorMessage() != null) {
            binding.contentScroll.setVisibility(View.GONE);
            binding.errorContainer.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(state.getErrorMessage());
        } else {
            binding.errorContainer.setVisibility(View.GONE);
        }

        GitHubUserProfileDataEntry profile = state.getProfile();
        if (profile != null) {
            binding.contentScroll.setVisibility(View.VISIBLE);
            bindProfile(profile);
        } else if (state.getErrorMessage() == null) {
            binding.contentScroll.setVisibility(View.GONE);
        }
    }

    /**
     * Binds the user profile data to the views.
     * @param profile The user profile data.
     */
    private void bindProfile(@NonNull GitHubUserProfileDataEntry profile) {
        String displayName = profile.getDisplayName().orElse(profile.getUsername());
        binding.displayName.setText(displayName);
        binding.username.setText(getString(R.string.user_profile_username_format, profile.getUsername()));

        currentUsername = profile.getUsername();
        lastFetchedMonth = -1;
        lastFetchedYear = -1;

        updateTextOrHide(binding.bio, profile.getBio().orElse(null));

        Glide.with(binding.avatar)
                .load(profile.getAvatarUrl().orElse(null))
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(binding.avatar);

        binding.repositoriesValue.setText(String.valueOf(profile.getPublicReposCount()));
        binding.followersValue.setText(String.valueOf(profile.getFollowersCount()));
        binding.followingValue.setText(String.valueOf(profile.getFollowingCount()));

        updateRow(binding.locationGroup, binding.locationValue, profile.getLocation().orElse(null));
        updateRow(binding.companyGroup, binding.companyValue, profile.getCompany().orElse(null));
        updateRow(binding.emailGroup, binding.emailValue, profile.getEmail().orElse(null));
        updateLinkRow(binding.blogGroup, binding.blogValue, profile.getBlogUrl().orElse(null));
        updateLinkRow(binding.profileGroup, binding.profileValue, profile.getProfileUrl().orElse(null));

        updateRow(binding.createdAtGroup, binding.createdAtValue, formatDate(profile.getCreatedAt().orElse(null)));
        updateRow(binding.updatedAtGroup, binding.updatedAtValue, formatDate(profile.getUpdatedAt().orElse(null)));

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(displayName);
            activity.getSupportActionBar().setSubtitle(
                    getString(R.string.user_profile_username_format, profile.getUsername()));
        }

        // Load contributions data
        refreshContributionsForCurrentDate();
    }

    private void updateTextOrHide(@NonNull TextView view, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            view.setVisibility(View.GONE);
            view.setText(null);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(value);
        }
    }

    private void updateRow(@NonNull View group, @NonNull TextView valueView, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            group.setVisibility(View.GONE);
            valueView.setText(null);
        } else {
            group.setVisibility(View.VISIBLE);
            valueView.setText(value);
        }
    }

    private void updateLinkRow(@NonNull View group, @NonNull TextView valueView, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            group.setVisibility(View.GONE);
            valueView.setText(null);
            valueView.setOnClickListener(null);
            return;
        }
        group.setVisibility(View.VISIBLE);
        valueView.setText(value);
        String preparedUrl = prepareUrl(value);
        valueView.setOnClickListener(v -> openLink(preparedUrl));
    }

    private void updateContributionsTitle(@NonNull Calendar calendar) {
        if (binding == null) {
            return;
        }
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        if (monthName == null) {
            monthName = "";
        }
        String title = getString(R.string.contributions_title_format, monthName, calendar.get(Calendar.YEAR));
        binding.contributionsTitle.setText(title);
    }

    private void refreshContributionsForCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        updateContributionsTitle(calendar);

        if (TextUtils.isEmpty(currentUsername)) {
            return;
        }

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        if (month == lastFetchedMonth && year == lastFetchedYear) {
            return;
        }

        lastFetchedMonth = month;
        lastFetchedYear = year;

        viewModel.loadContributions(currentUsername);
    }

    private void registerDateChangeReceiver() {
        if (isDateReceiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        ContextCompat.registerReceiver(requireContext(), dateChangeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        isDateReceiverRegistered = true;
    }

    private void unregisterDateChangeReceiver() {
        if (!isDateReceiverRegistered) {
            return;
        }
        requireContext().unregisterReceiver(dateChangeReceiver);
        isDateReceiverRegistered = false;
    }

    @Nullable
    private String formatDate(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        return dateFormat.format(Date.from(instant));
    }

    @NonNull
    private String prepareUrl(@NonNull String raw) {
        String trimmed = raw.trim();
        if (TextUtils.isEmpty(trimmed)) {
            return raw;
        }
        Uri uri = Uri.parse(trimmed);
        if (uri.getScheme() == null) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    private void openLink(@NonNull String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException ignored) {
            // Silently swallow if no browser is available.
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}