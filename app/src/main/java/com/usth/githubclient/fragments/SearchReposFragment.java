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
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.viewmodel.SearchReposViewModel; // Import ViewModel
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchReposFragment extends BaseFragment {
    private SearchReposListAdapter adapter;
    private GithubApiService apiService; // Giữ lại để dùng cho chức năng search
    private SearchReposViewModel viewModel; // Thêm ViewModel
    private String lastSearchQuery;

    private enum ListMode { REPOS, SEARCH }
    private ListMode listMode = ListMode.REPOS;

    public static SearchReposFragment newInstance() {
        return new SearchReposFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel
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

        // Lắng nghe dữ liệu từ ViewModel
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

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (listMode == ListMode.REPOS) {
                showEmpty(errorMsg);
            }
        });

        // Chỉ hiển thị repo của tôi khi không có query tìm kiếm
        if (lastSearchQuery == null || lastSearchQuery.isEmpty()) {
            displayMyRepos();
        }

        return v;
    }

    // Phương thức search không thay đổi nhiều
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
        showLoading(true);
        // Yêu cầu ViewModel tải dữ liệu.
        // ViewModel sẽ tự biết nếu đã có dữ liệu rồi thì sẽ không tải lại
        viewModel.loadMyRepos();
    }

    @Nullable
    @Override
    protected String getSectionTitle() {
        // ... giữ nguyên
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
        // ... giữ nguyên
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SearchReposListAdapter();
        recyclerView.setAdapter(adapter);
    }
}