package com.usth.githubclient.viewmodel;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.ContributionDataEntry;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import retrofit2.Response;

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private final UserMapper userMapper;
    private final MutableLiveData<UserUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<List<ContributionDataEntry>> contributions = new MutableLiveData<>();

    public UserViewModel() {
        this.userRepository = ServiceLocator.getInstance().userRepository();
        this.executorService = ServiceLocator.getInstance().getExecutorService();
        this.userMapper = ServiceLocator.getInstance().userMapper();
    }

    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    public LiveData<List<ContributionDataEntry>> getContributions() {
        return contributions;
    }

    public void loadUserProfile(@Nullable String username) {
        uiState.postValue(new UserUiState(true, null, null));
        executorService.execute(() -> {
            try {
                // Correctly fetch UserDto first
                Response<UserDto> response = (username == null)
                        ? userRepository.authenticate().execute()
                        : userRepository.getUser(username).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Map UserDto to GitHubUserProfileDataEntry
                    GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                    uiState.postValue(new UserUiState(false, null, profile));
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    uiState.postValue(new UserUiState(false, error, null));
                }
            } catch (IOException e) {
                uiState.postValue(new UserUiState(false, "Network error", null));
            }
        });
    }

    public void loadContributions(String username) {
        executorService.execute(() -> {
            try {
                Response<List<EventDto>> response = userRepository.getUserEvents(username, 1, 100).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<ContributionDataEntry> processedContributions = processEvents(response.body());
                    contributions.postValue(processedContributions);
                } else {
                    // Error loading contributions, can post to another LiveData if needed
                }
            } catch (IOException e) {
                // Network error, can post to another LiveData if needed
            }
        });
    }

    @SuppressLint("NewApi")
    private List<ContributionDataEntry> processEvents(List<EventDto> events) {
        Map<Integer, ContributionDataEntry> contributionsMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (EventDto event : events) {
            // Make sure createdAt is not null to avoid NullPointerException
            if (event.getCreatedAt() == null) continue;

            Instant instant = Instant.parse(event.getCreatedAt());
            cal.setTime(Date.from(instant));
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

            ContributionDataEntry entry = contributionsMap.get(dayOfMonth);
            if (entry == null) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(cal.getTime());
                entry = new ContributionDataEntry(dayCal, 1);
                contributionsMap.put(dayOfMonth, entry);
            } else {
                entry.incrementCount();
            }
        }
        return new ArrayList<>(contributionsMap.values());
    }

    public static class UserUiState {
        private final boolean isLoading;
        private final String errorMessage;
        private final GitHubUserProfileDataEntry profile;

        public UserUiState(boolean isLoading, @Nullable String errorMessage, @Nullable GitHubUserProfileDataEntry profile) {
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.profile = profile;
        }

        public boolean isLoading() {
            return isLoading;
        }

        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        @Nullable
        public GitHubUserProfileDataEntry getProfile() {
            return profile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserUiState that = (UserUiState) o;
            return isLoading == that.isLoading &&
                    Objects.equals(errorMessage, that.errorMessage) &&
                    Objects.equals(profile, that.profile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isLoading, errorMessage, profile);
        }

        @NonNull
        @Override
        public String toString() {
            return "UserUiState{" +
                    "isLoading=" + isLoading +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", profile=" + profile +
                    '}';
        }
    }
}