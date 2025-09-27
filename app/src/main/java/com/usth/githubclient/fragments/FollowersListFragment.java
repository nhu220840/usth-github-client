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
import com.usth.githubclient.adapters.FollowersListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FollowersListAdapter adapter;
    private final ApiClient apiClient = new ApiClient();
    private GithubApiService apiService;

    public static FollowersListFragment newInstance() {
        return new FollowersListFragment();
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
        progressBar  = v.findViewById(R.id.progress);
        emptyView    = v.findViewById(R.id.empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        adapter = new FollowersListAdapter();
        recyclerView.setAdapter(adapter);

        showEmpty("Type a username and press search.");
        return v;
    }

    /** Public API for MainActivity */
    public void submitQuery(String username) {
        if (username == null || username.trim().isEmpty()) {
            showEmpty("Please enter a keyword.");
            return;
        }
        final String q = username.trim();
        if (apiService == null) {
            showEmpty("Unable to initialize network client.");
            return;
        }
        // GỌI SEARCH USERS → trả về danh sách users liên quan
        apiService
                .searchUsers(q, 1, 30)
                .enqueue(new Callback<SearchUsersResponseDto>() {
                    @Override
                    public void onResponse(Call<SearchUsersResponseDto> call,
                                           Response<SearchUsersResponseDto> resp) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (resp.isSuccessful() && resp.body() != null) {
                            List<UserDto> items = resp.body().getItems();
                            List<FollowersListAdapter.UserRow> ui = new ArrayList<>();
                            if (items != null) {
                                for (UserDto u : items) {
                                    if (u == null || u.getLogin() == null || u.getAvatarUrl() == null) {
                                        continue;
                                    }
                                    ui.add(new FollowersListAdapter.UserRow(u.getLogin(), u.getAvatarUrl()));
                                }
                            }
                            adapter.submit(ui);

                            if (ui.isEmpty()) showEmpty("No users match: " + q);
                            else showList();

                            // (Tuỳ chọn) Enrich: tải tên thật cho TOP 10 kết quả
                            int limit = items != null ? Math.min(items.size(), 10) : 0;
                            for (int i = 0; i < limit; i++) {
                                final String login = items.get(i).getLogin();
                                if (login == null || login.trim().isEmpty()) {
                                    continue;
                                }
                                apiService.getUser(login)
                                        .enqueue(new Callback<UserDto>() {
                                            @Override
                                            public void onResponse(Call<UserDto> call,
                                                                   Response<UserDto> resp2) {
                                                if (!isAdded()) return;
                                                if (resp2.isSuccessful() && resp2.body() != null) {
                                                    UserDto detail = resp2.body();
                                                    adapter.updateDetails(
                                                            login,
                                                            detail.getName(),
                                                            detail.getBio(),
                                                            detail.getPublicRepos(),
                                                            detail.getFollowers());                                            }
                                            }
                                            @Override public void onFailure(Call<UserDto> call, Throwable t) { }
                                        });
                            }
                        } else {
                            showEmpty("Search failed: " + resp.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchUsersResponseDto> call, Throwable t) {
                        if (!isAdded()) return;
                        showLoading(false);
                        showEmpty("Network error: " + t.getMessage());
                    }
                });
    }


    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
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
    }
}
