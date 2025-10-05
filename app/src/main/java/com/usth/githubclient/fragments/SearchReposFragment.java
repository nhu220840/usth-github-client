package com.usth.githubclient.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.SearchReposListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.viewmodel.SearchReposViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for searching and displaying repositories.
 */
public class SearchReposFragment extends BaseFragment {
    private SearchReposListAdapter adapter;
    private GithubApiService apiService;
    private SearchReposViewModel viewModel;
    private String lastSearchQuery;

    private enum ListMode { REPOS, SEARCH }
    private ListMode listMode = ListMode.REPOS;

    public static SearchReposFragment newInstance() {
        return new SearchReposFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchReposViewModel.class);
        apiService = new ApiClient().createService(GithubApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);

        setupRecyclerView();

        // Observe the list of repositories from the ViewModel.
        viewModel.getMyRepos().observe(getViewLifecycleOwner(), repos -> {
            if (listMode == ListMode.REPOS) {
                adapter.submit(repos);
                if (repos.isEmpty()) {
                    showEmpty("You don't have any repositories yet.");
                } else {
                    showList();
                }
            }
        });

        // Observe errors from the ViewModel.
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (listMode == ListMode.REPOS) {
                showEmpty(errorMsg);
            }
        });

        if (lastSearchQuery == null || lastSearchQuery.isEmpty()) {
            displayMyRepos();
        }

        return v;
    }

    /**
     * Submits a search query.
     * @param query The search query.
     */
    public void submitQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayMyRepos();
            return;
        }
        final String q = query.trim();
        listMode = ListMode.SEARCH;
        lastSearchQuery = q;
        showLoading(true);

        apiService.searchRepos(q, 1, 30).enqueue(new Callback<SearchRepoResponseDto>() {
            @Override
            public void onResponse(Call<SearchRepoResponseDto> call, Response<SearchRepoResponseDto> response) {
                if (!isAdded() || listMode != ListMode.SEARCH || !q.equals(lastSearchQuery)) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submit(response.body().getItems());
                    if (response.body().getItems().isEmpty()) {
                        showEmpty("No repositories match: " + q);
                    } else {
                        showList();
                    }
                } else {
                    showEmpty("Search failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<SearchRepoResponseDto> call, Throwable t) {
                if (!isAdded() || listMode != ListMode.SEARCH || !q.equals(lastSearchQuery)) return;
                showLoading(false);
                showEmpty("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Displays the authenticated user's repositories.
     */
    public void showMyRepos() {
        displayMyRepos();
    }

    private void displayMyRepos() {
        listMode = ListMode.REPOS;
        lastSearchQuery = null;

        // CHECK BEFORE LOADING
        // Get the current data from the LiveData in the ViewModel.
        List<RepoDto> currentRepos = viewModel.getMyRepos().getValue();

        if (currentRepos != null) {
            // IF DATA ALREADY EXISTS:
            // Just update the UI immediately without showing the loading indicator.
            adapter.submit(currentRepos);
            if (currentRepos.isEmpty()) {
                showEmpty("You don't have any repositories yet.");
            } else {
                showList(); // Hide the spinner and show the list.
            }
        } else {
            // IF NO DATA EXISTS (first load):
            // Show the loading indicator and request the ViewModel to load data.
            showLoading(true);
            viewModel.loadMyRepos();
        }
    }

    @Nullable
    @Override
    protected String getSectionTitle() {
        switch (listMode) {
            case REPOS:
                return getString(R.string.section_repositories);
            case SEARCH:
                if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
                    return getString(R.string.section_search_results_for, lastSearchQuery);
                } else {
                    return getString(R.string.section_search_results);
                }
            default:
                return null;
        }
    }

    @Override
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        // Pass the apiService to the adapter's constructor.
        adapter = new SearchReposListAdapter(apiService);
        recyclerView.setAdapter(adapter);
    }
}