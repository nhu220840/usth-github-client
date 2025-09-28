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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usth.githubclient.R;
import com.usth.githubclient.adapters.SearchUsersListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView sectionTitle;

    private SearchUsersListAdapter adapter;
    private final ApiClient apiClient = new ApiClient();
    private GithubApiService apiService;
    private final List<SearchUsersListAdapter.UserRow> cachedFollowers = new ArrayList<>();
    private List<UserDto> lastFollowersResponse = new ArrayList<>();
    private boolean hasLoadedFollowers;
    private String authenticatedUsername;
    private String lastSearchQuery;

    private enum ListMode { FOLLOWERS, SEARCH }

    private ListMode listMode = ListMode.FOLLOWERS;

    public static SearchUsersFragment newInstance() {
        return new SearchUsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = apiClient.createService(GithubApiService.class);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        adapter = new SearchUsersListAdapter();
        recyclerView.setAdapter(adapter);

        displayFollowers();
        return v;
    }

    /** Public API for MainActivity */
    public void submitQuery(String username) {
        if (username == null || username.trim().isEmpty()) {
            displayFollowers();
            return;
        }
        if (apiService == null) {
            showEmpty("Unable to initialize network client.");
            return;
        }
        final String q = username.trim();
        listMode = ListMode.SEARCH;
        lastSearchQuery = q;
        showLoading(true);

        // GỌI SEARCH USERS → trả về danh sách users liên quan
        apiService
                .searchUsers(q, 1, 30)
                .enqueue(new Callback<SearchUsersResponseDto>() {
                    @Override
                    public void onResponse(Call<SearchUsersResponseDto> call,
                                           Response<SearchUsersResponseDto> resp) {
                        if (!isAdded()) return;
                        if (listMode != ListMode.SEARCH
                                || lastSearchQuery == null
                                || !lastSearchQuery.equals(q)) {
                            return;
                        }
                        showLoading(false);

                        if (resp.isSuccessful() && resp.body() != null) {
                            List<UserDto> items = resp.body().getItems();
                            List<SearchUsersListAdapter.UserRow> ui = new ArrayList<>();
                            if (items != null) {
                                for (UserDto u : items) {
                                    if (u == null || u.getLogin() == null || u.getAvatarUrl() == null) {
                                        continue;
                                    }
                                    ui.add(new SearchUsersListAdapter.UserRow(u.getLogin(), u.getAvatarUrl()));
                                }
                            }
                            adapter.submit(ui);

                            if (ui.isEmpty()) showEmpty("No users match: " + q);
                            else showList();

                            enrichUserDetails(items);

                        } else {
                            showEmpty("Search failed: " + resp.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchUsersResponseDto> call, Throwable t) {
                        if (!isAdded()) return;
                        if (listMode != ListMode.SEARCH
                                || lastSearchQuery == null
                                || !lastSearchQuery.equals(q)) {
                            return;
                        }
                        showLoading(false);
                        showEmpty("Network error: " + t.getMessage());
                    }
                });
    }

    /** Public API used when the search query is cleared. */
    public void showFollowers() {
        displayFollowers();
    }

    private void displayFollowers() {
        listMode = ListMode.FOLLOWERS;
        lastSearchQuery = null;
        if (apiService == null) {
            showEmpty(getString(R.string.followers_load_failed));
            return;
        }

        if (hasLoadedFollowers) {
            adapter.submit(new ArrayList<>(cachedFollowers));
            if (cachedFollowers.isEmpty()) {
                showEmpty(getString(R.string.followers_empty_state));
            } else {
                showList();
                enrichUserDetails(lastFollowersResponse);
            }
            return;
        }

        showLoading(true);

        if (authenticatedUsername != null && !authenticatedUsername.trim().isEmpty()) {
            fetchFollowers(authenticatedUsername);
        } else {
            apiService.authenticate().enqueue(new Callback<UserDto>() {
                @Override
                public void onResponse(Call<UserDto> call, Response<UserDto> resp) {
                    if (!isAdded()) return;
                    if (resp.isSuccessful() && resp.body() != null && resp.body().getLogin() != null) {
                        authenticatedUsername = resp.body().getLogin();
                        if (listMode == ListMode.FOLLOWERS) {
                            fetchFollowers(authenticatedUsername);
                        }
                    } else if (listMode == ListMode.FOLLOWERS) {
                        showLoading(false);
                        showEmpty(getString(R.string.followers_load_failed));
                    }
                }

                @Override
                public void onFailure(Call<UserDto> call, Throwable t) {
                    if (!isAdded()) return;
                    if (listMode == ListMode.FOLLOWERS) {
                        showLoading(false);
                        showEmpty(getString(R.string.followers_load_failed));
                    }
                }
            });
        }
    }

    private void fetchFollowers(final String username) {
        apiService.getFollowers(username, 30, 1).enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> resp) {
                if (!isAdded()) return;
                boolean isFollowerMode = listMode == ListMode.FOLLOWERS;
                if (isFollowerMode) {
                    showLoading(false);
                }

                if (resp.isSuccessful() && resp.body() != null) {
                    List<UserDto> items = resp.body();
                    List<SearchUsersListAdapter.UserRow> ui = new ArrayList<>();
                    List<UserDto> detailSource = new ArrayList<>();
                    if (items != null) {
                        for (UserDto u : items) {
                            if (u == null || u.getLogin() == null || u.getAvatarUrl() == null) {
                                continue;
                            }
                            ui.add(new SearchUsersListAdapter.UserRow(u.getLogin(), u.getAvatarUrl()));
                            detailSource.add(u);
                        }
                    }

                    cachedFollowers.clear();
                    cachedFollowers.addAll(ui);
                    lastFollowersResponse = detailSource;
                    hasLoadedFollowers = true;

                    if (isFollowerMode) {
                        adapter.submit(new ArrayList<>(cachedFollowers));

                        if (cachedFollowers.isEmpty()) {
                            showEmpty(getString(R.string.followers_empty_state));
                        } else {
                            showList();
                            enrichUserDetails(lastFollowersResponse);
                        }
                    }
                } else if (isFollowerMode) {
                    showEmpty(getString(R.string.followers_load_failed));
                }
            }

            @Override
            public void onFailure(Call<List<UserDto>> call, Throwable t) {
                if (!isAdded()) return;
                if (listMode == ListMode.FOLLOWERS) {
                    showLoading(false);
                    showEmpty(getString(R.string.followers_load_failed));
                }
            }
        });
    }

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
        if (sectionTitle == null) {
            return;
        }
        if (listMode == ListMode.FOLLOWERS) {
            sectionTitle.setText(R.string.section_followers);
            sectionTitle.setVisibility(View.VISIBLE);
        } else if (listMode == ListMode.SEARCH) {
            if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
                sectionTitle.setText(getString(R.string.section_search_results_for, lastSearchQuery));
            } else {
                sectionTitle.setText(R.string.section_search_results);
            }
            sectionTitle.setVisibility(View.VISIBLE);
        } else {
            sectionTitle.setVisibility(View.GONE);
        }
    }
}
