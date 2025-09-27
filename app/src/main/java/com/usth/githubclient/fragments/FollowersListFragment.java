package com.usth.githubclient.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usth.githubclient.R;
import com.usth.githubclient.adapters.FollowersListAdapter;
import com.usth.githubclient.network.RetrofitClient;
import com.usth.githubclient.network.models.UserDto;
import com.usth.githubclient.network.models.UserDetailDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.usth.githubclient.network.models.SearchUsersResponse;

public class FollowersListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FollowersListAdapter adapter;

    public static FollowersListFragment newInstance() {
        return new FollowersListFragment();
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
        showLoading(true);

        // GỌI SEARCH USERS → trả về danh sách users liên quan
        RetrofitClient.api()
                .searchUsers(q, 1, 30)
                .enqueue(new Callback<SearchUsersResponse>() {
                    @Override
                    public void onResponse(Call<SearchUsersResponse> call,
                                           Response<SearchUsersResponse> resp) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (resp.isSuccessful() && resp.body() != null && resp.body().items != null) {
                            List<UserDto> items = resp.body().items;
                            List<FollowersListAdapter.UserRow> ui = new java.util.ArrayList<>();
                            for (UserDto u : items) {
                                ui.add(new FollowersListAdapter.UserRow(u.login, u.avatarUrl));
                            }
                            adapter.submit(ui);

                            if (ui.isEmpty()) showEmpty("No users match: " + q);
                            else showList();

                            // (Tuỳ chọn) Enrich: tải tên thật cho TOP 10 kết quả
                            int limit = Math.min(items.size(), 10);
                            for (int i = 0; i < limit; i++) {
                                final String login = items.get(i).login;
                                RetrofitClient.api().getUser(login)
                                        .enqueue(new Callback<UserDetailDto>() {
                                            @Override
                                            public void onResponse(Call<UserDetailDto> call,
                                                                   Response<UserDetailDto> resp2) {
                                                if (!isAdded()) return;
                                                if (resp2.isSuccessful() && resp2.body() != null) {
                                                    adapter.updateName(login, resp2.body().name);
                                                }
                                            }
                                            @Override public void onFailure(Call<UserDetailDto> call, Throwable t) { }
                                        });
                            }
                        } else {
                            showEmpty("Search failed: " + resp.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchUsersResponse> call, Throwable t) {
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
