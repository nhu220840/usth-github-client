// usth-github-client-develop/app/src/main/java/com/usth/githubclient/fragments/SearchUsersFragment.java

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
import com.usth.githubclient.adapters.SearchUsersListAdapter;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.viewmodel.SearchUserViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for searching and displaying users.
 */
public class SearchUsersFragment extends BaseFragment {
    private SearchUsersListAdapter adapter;
    private SearchUserViewModel viewModel; // Add ViewModel
    private String lastSearchQuery;
    private GithubApiService apiService; // Keep for enriching details

    private enum ListMode { FOLLOWERS, SEARCH }
    private ListMode listMode = ListMode.FOLLOWERS;

    public static SearchUsersFragment newInstance() {
        return new SearchUsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SearchUserViewModel.class);
        apiService = new ApiClient().createService(GithubApiService.class); // Keep for enriching
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);

        setupRecyclerView();

        // Observe data from the ViewModel
        observeViewModel();

        // Load initial data
        if (lastSearchQuery == null || lastSearchQuery.isEmpty()) {
            displayFollowers();
        }

        return v;
    }

    private void observeViewModel() {
        // Observe the list of followers
        viewModel.getFollowers().observe(getViewLifecycleOwner(), followers -> {
            if (listMode == ListMode.FOLLOWERS) {
                updateUserList(followers);
                if (followers.isEmpty()) {
                    showEmpty(getString(R.string.followers_empty_state));
                } else {
                    showList();
                }
            }
        });

        // Observe search results
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), users -> {
            if (listMode == ListMode.SEARCH) {
                updateUserList(users);
                if (users.isEmpty()) {
                    showEmpty("No users match: " + lastSearchQuery);
                } else {
                    showList();
                }
            }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            showEmpty(errorMsg);
        });
    }

    /** Public API for MainActivity */
    public void submitQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayFollowers();
            return;
        }
        final String q = query.trim();
        listMode = ListMode.SEARCH;
        lastSearchQuery = q;
        showLoading(true);
        viewModel.searchUsers(q); // Call ViewModel to perform search
    }

    /** Public API used when the search query is cleared. */
    public void showFollowers() {
        displayFollowers();
    }

    private void displayFollowers() {
        listMode = ListMode.FOLLOWERS;
        lastSearchQuery = null;

        // Check if data already exists in the ViewModel
        List<UserDto> currentFollowers = viewModel.getFollowers().getValue();
        if (currentFollowers != null) {
            updateUserList(currentFollowers);
            if(currentFollowers.isEmpty()) {
                showEmpty(getString(R.string.followers_empty_state));
            } else {
                showList();
            }
        } else {
            showLoading(true);
            viewModel.loadFollowers(); // Request ViewModel to load data
        }
    }

    // New method to update the adapter
    private void updateUserList(List<UserDto> users) {
        if (users == null) return;
        List<SearchUsersListAdapter.UserRow> uiRows = new ArrayList<>();
        for (UserDto u : users) {
            if (u != null && u.getLogin() != null && u.getAvatarUrl() != null) {
                uiRows.add(new SearchUsersListAdapter.UserRow(u.getLogin(), u.getAvatarUrl()));
            }
        }
        adapter.submit(uiRows);
        showList();
        enrichUserDetails(users); // Still call enrich to get more details
    }

    // Keep the enrichUserDetails method unchanged
    private void enrichUserDetails(List<UserDto> items) {
        if (apiService == null || items == null) {
            return;
        }
        int limit = Math.min(items.size(), 100);
        for (int i = 0; i < limit; i++) {
            final UserDto dto = items.get(i);
            if (dto == null) {
                continue;
            }
            final String login = dto.getLogin();
            if (login == null || login.trim().isEmpty()) {
                continue;
            }
            apiService.getUser(login)
                    .enqueue(new Callback<UserDto>() {
                        @Override
                        public void onResponse(Call<UserDto> call, Response<UserDto> resp) {
                            if (!isAdded()) return;
                            if (resp.isSuccessful() && resp.body() != null) {
                                UserDto detail = resp.body();
                                adapter.updateDetails(
                                        login,
                                        detail.getName(),
                                        detail.getBio(),
                                        detail.getPublicRepos(),
                                        detail.getFollowers());
                            }
                        }

                        @Override public void onFailure(Call<UserDto> call, Throwable t) { }
                    });
        }
    }

    @Nullable
    @Override
    protected String getSectionTitle() {
        if (listMode == ListMode.FOLLOWERS) {
            return getString(R.string.section_followers);
        } else if (listMode == ListMode.SEARCH) {
            if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
                return getString(R.string.section_search_results_for, lastSearchQuery);
            } else {
                return getString(R.string.section_search_results);
            }
        }
        return null; // Return null to hide the title.
    }

    @Override
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SearchUsersListAdapter();
        recyclerView.setAdapter(adapter);
    }
}