package com.usth.githubclient.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usth.githubclient.R;
import com.usth.githubclient.adapters.SearchReposListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchReposFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView sectionTitle;

    private SearchReposListAdapter adapter;
    private ApiClient apiClient = new ApiClient();
    private GithubApiService apiService;
    private String authenticatedUsername;
    private String lastSearchQuery;
    private List<RepoDto> cachedRepos = new ArrayList<>();

    private enum ListMode { REPOS, SEARCH }
    private ListMode listMode = ListMode.REPOS;


    public static SearchReposFragment newInstance() {
        return new SearchReposFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = apiClient.createService(GithubApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SearchReposListAdapter();
        recyclerView.setAdapter(adapter);

        displayMyRepos();
        return v;
    }

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

    public void showMyRepos() {
        displayMyRepos();
    }

    private void displayMyRepos() {
        listMode = ListMode.REPOS;
        lastSearchQuery = null;

        if (!cachedRepos.isEmpty()) {
            adapter.submit(cachedRepos);
            showList();
            return;
        }

        showLoading(true);
        apiService.getAuthenticatedRepositories(30, 1, "updated").enqueue(new Callback<List<RepoDto>>() {
            @Override
            public void onResponse(Call<List<RepoDto>> call, Response<List<RepoDto>> response) {
                if (!isAdded() || listMode != ListMode.REPOS) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    cachedRepos.clear();
                    cachedRepos.addAll(response.body());
                    adapter.submit(cachedRepos);
                    if (cachedRepos.isEmpty()) {
                        showEmpty("You don't have any repositories yet.");
                    } else {
                        showList();
                    }
                } else {
                    showEmpty("Failed to load your repositories.");
                }
            }

            @Override
            public void onFailure(Call<List<RepoDto>> call, Throwable t) {
                if (!isAdded() || listMode != ListMode.REPOS) return;
                showLoading(false);
                showEmpty("Network error while loading your repositories.");
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        updateSectionTitle();
    }

    private void showEmpty(String msg) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(msg);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        updateSectionTitle();
    }

    private void updateSectionTitle() {
        if (sectionTitle == null) return;
        if (listMode == ListMode.REPOS) {
            sectionTitle.setText("Your Repositories");
            sectionTitle.setVisibility(View.VISIBLE);
        } else if (listMode == ListMode.SEARCH) {
            if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
                sectionTitle.setText("Search results for \"" + lastSearchQuery + "\"");
            } else {
                sectionTitle.setText("Search results");
            }
            sectionTitle.setVisibility(View.VISIBLE);
        } else {
            sectionTitle.setVisibility(View.GONE);
        }
    }
}