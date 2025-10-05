package com.usth.githubclient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for the SearchReposFragment.
 */
public class SearchReposViewModel extends ViewModel {

    // LiveData to hold the list of repositories and notify the Fragment of changes.
    private final MutableLiveData<List<RepoDto>> myRepos = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private boolean hasLoaded = false;

    public LiveData<List<RepoDto>> getMyRepos() {
        return myRepos;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Loads the authenticated user's repositories, only once.
     */
    public void loadMyRepos() {
        if (hasLoaded) {
            return; // Don't load again if already loaded.
        }

        GithubApiService apiService = new ApiClient().createService(GithubApiService.class);
        apiService.getAuthenticatedRepositories(30, 1, "updated").enqueue(new Callback<List<RepoDto>>() {
            @Override
            public void onResponse(Call<List<RepoDto>> call, Response<List<RepoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myRepos.postValue(response.body());
                    hasLoaded = true;
                } else {
                    error.postValue("Failed to load your repositories.");
                }
            }

            @Override
            public void onFailure(Call<List<RepoDto>> call, Throwable t) {
                error.postValue("Network error while loading your repositories.");
            }
        });
    }
}