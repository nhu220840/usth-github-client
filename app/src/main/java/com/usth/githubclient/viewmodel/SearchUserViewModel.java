package com.usth.githubclient.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.ContributionDataEntry;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for the SearchUsersFragment.
 */
public class SearchUserViewModel extends ViewModel {

    // LiveData to hold the list of followers and notify the Fragment of changes.
    private final MutableLiveData<List<UserDto>> followers = new MutableLiveData<>();
    // LiveData to hold the user search results.
    private final MutableLiveData<List<UserDto>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<List<ContributionDataEntry>> contributions = new MutableLiveData<>();

    // LiveData for error reporting.
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final GithubApiService apiService;
    private final ExecutorService executorService;
    private String authenticatedUsername;
    private boolean hasLoadedFollowers = false;

    public SearchUserViewModel() {
        this.apiService = new ApiClient().createService(GithubApiService.class);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Getters for LiveData
    public LiveData<List<UserDto>> getFollowers() {
        return followers;
    }

    public LiveData<List<UserDto>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<ContributionDataEntry>> getContributions() {
        return contributions;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Loads the authenticated user's followers, only once.
     */
    public void loadFollowers() {
        if (hasLoadedFollowers) {
            return; // Don't load again if already loaded.
        }

        // Get the username of the logged-in user
        apiService.authenticate().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    authenticatedUsername = response.body().getLogin();
                    fetchFollowers(authenticatedUsername);
                } else {
                    error.postValue("Failed to get authenticated user.");
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    private void fetchFollowers(String username) {
        apiService.getFollowers(username, 30, 1).enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    followers.postValue(response.body());
                    hasLoadedFollowers = true;
                } else {
                    error.postValue("Failed to load your followers.");
                }
            }

            @Override
            public void onFailure(Call<List<UserDto>> call, Throwable t) {
                error.postValue("Network error while loading your followers.");
            }
        });
    }

    /**
     * Performs a user search.
     * @param query The search query.
     */
    public void searchUsers(String query) {
        apiService.searchUsers(query, 1, 30).enqueue(new Callback<SearchUsersResponseDto>() {
            @Override
            public void onResponse(Call<SearchUsersResponseDto> call, Response<SearchUsersResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.postValue(response.body().getItems());
                } else {
                    error.postValue("Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SearchUsersResponseDto> call, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void loadContributions(String username) {
        executorService.execute(() -> {
            try {
                // Changed from userRepository to apiService
                Response<List<EventDto>> response = apiService.getUserEvents(username, 1, 100).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<ContributionDataEntry> processedContributions = processEvents(response.body());
                    contributions.postValue(processedContributions);
                } else {
                    contributions.postValue(processEvents(new ArrayList<>()));
                    error.postValue("Failed to load contributions.");
                }
            } catch (IOException e) {
                contributions.postValue(processEvents(new ArrayList<>()));
                error.postValue("Network error loading contributions: " + e.getMessage());
            }
        });
    }

    @SuppressLint("NewApi")
    private List<ContributionDataEntry> processEvents(List<EventDto> events) {
        if (events == null) {
            events = new ArrayList<>();
        }

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Map<Integer, ContributionDataEntry> contributionsByDay = new HashMap<>();
        List<ContributionDataEntry> contributionsForMonth = new ArrayList<>(daysInMonth);

        for (int day = 1; day <= daysInMonth; day++) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.set(Calendar.YEAR, currentYear);
            dayCal.set(Calendar.MONTH, currentMonth);
            dayCal.set(Calendar.DAY_OF_MONTH, day);
            dayCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.MINUTE, 0);
            dayCal.set(Calendar.SECOND, 0);
            dayCal.set(Calendar.MILLISECOND, 0);

            ContributionDataEntry emptyEntry = new ContributionDataEntry(dayCal, 0);
            contributionsByDay.put(day, emptyEntry);
            contributionsForMonth.add(emptyEntry);
        }

        for (EventDto event : events) {
            if (event.getCreatedAt() == null) {
                continue;
            }
            Instant instant = Instant.parse(event.getCreatedAt());
                Date date = Date.from(instant);

                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(date);

                if (eventCal.get(Calendar.MONTH) != currentMonth || eventCal.get(Calendar.YEAR) != currentYear) {
                    continue;
                }

                int dayOfMonth = eventCal.get(Calendar.DAY_OF_MONTH);
                ContributionDataEntry entry = contributionsByDay.get(dayOfMonth);
                if (entry != null) {
                entry.incrementCount();
            }
        }
            return contributionsForMonth;
    }
}