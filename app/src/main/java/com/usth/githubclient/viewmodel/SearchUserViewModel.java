// usth-github-client-develop/app/src/main/java/com/usth/githubclient/viewmodel/SearchUserViewModel.java

package com.usth.githubclient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
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
    // LiveData for error reporting.
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final GithubApiService apiService;
    private String authenticatedUsername;
    private boolean hasLoadedFollowers = false;

    public SearchUserViewModel() {
        this.apiService = new ApiClient().createService(GithubApiService.class);
    }

    // Getters for LiveData
    public LiveData<List<UserDto>> getFollowers() {
        return followers;
    }

    public LiveData<List<UserDto>> getSearchResults() {
        return searchResults;
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
}