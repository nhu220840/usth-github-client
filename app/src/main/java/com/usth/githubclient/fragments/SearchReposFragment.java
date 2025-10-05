package com.usth.githubclient.fragments;

// Các import cần thiết.
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider; // Dùng để khởi tạo và lấy ViewModel.
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.SearchReposListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.viewmodel.SearchReposViewModel; // Import ViewModel.

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Kế thừa từ BaseFragment để tái sử dụng logic quản lý UI.
public class SearchReposFragment extends BaseFragment {
    // Adapter cho RecyclerView.
    private SearchReposListAdapter adapter;
    // apiService được giữ lại để dùng cho chức năng tìm kiếm (chức năng này hiện chưa nằm trong ViewModel).
    private GithubApiService apiService;
    // Khai báo ViewModel.
    private SearchReposViewModel viewModel;
    // Lưu lại query tìm kiếm cuối cùng để kiểm tra khi nhận kết quả.
    private String lastSearchQuery;

    // Enum để quản lý trạng thái của danh sách: đang hiển thị repo của người dùng hay kết quả tìm kiếm.
    private enum ListMode { REPOS, SEARCH }
    private ListMode listMode = ListMode.REPOS;

    // Phương thức factory để tạo Fragment.
    public static SearchReposFragment newInstance() {
        return new SearchReposFragment();
    }

    // Được gọi khi Fragment được tạo.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel. Android sẽ tự động quản lý vòng đời của nó,
        // giúp giữ lại dữ liệu khi xoay màn hình.
        viewModel = new ViewModelProvider(this).get(SearchReposViewModel.class);
        // Khởi tạo service API.
        apiService = new ApiClient().createService(GithubApiService.class);
    }

    // Được gọi để tạo View cho Fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Thổi phồng" layout và ánh xạ các view.
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);

        setupRecyclerView();

        // ---- ĐIỂM CỐT LÕI CỦA MVVM ----
        // 1. Lắng nghe (observe) LiveData chứa danh sách repo từ ViewModel.
        // getViewLifecycleOwner() đảm bảo rằng việc lắng nghe sẽ tự động hủy khi Fragment bị hủy.
        viewModel.getMyRepos().observe(getViewLifecycleOwner(), repos -> {
            // Khi có dữ liệu mới từ ViewModel, đoạn mã này sẽ được thực thi.
            if (listMode == ListMode.REPOS) { // Chỉ cập nhật nếu đang ở chế độ xem repo của tôi.
                adapter.submit(repos); // Gửi danh sách mới cho Adapter.
                if (repos.isEmpty()) {
                    showEmpty("You don't have any repositories yet.");
                } else {
                    showList();
                }
            }
        });

        // 2. Lắng nghe LiveData chứa thông báo lỗi từ ViewModel.
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (listMode == ListMode.REPOS) {
                showEmpty(errorMsg);
            }
        });

        // Mặc định, hiển thị danh sách repo của người dùng.
        if (lastSearchQuery == null || lastSearchQuery.isEmpty()) {
            displayMyRepos();
        }

        return v;
    }

    // Phương thức được MainActivity gọi khi người dùng tìm kiếm.
    public void submitQuery(String query) {
        // Nếu query rỗng, quay lại hiển thị danh sách repo của tôi.
        if (query == null || query.trim().isEmpty()) {
            displayMyRepos();
            return;
        }
        final String q = query.trim();
        listMode = ListMode.SEARCH;
        lastSearchQuery = q;
        showLoading(true);

        // VẤN ĐỀ: Logic tìm kiếm này vẫn nằm trong Fragment.
        // Cải tiến: Nên chuyển logic này vào ViewModel để quản lý tập trung.
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

    // Được gọi khi người dùng xóa query tìm kiếm.
    public void showMyRepos() {
        displayMyRepos();
    }

    // Phương thức để chuyển sang chế độ hiển thị repo của người dùng.
    private void displayMyRepos() {
        listMode = ListMode.REPOS;
        lastSearchQuery = null;

        // KIỂM TRA TRƯỚC KHI TẢI
        // Lấy dữ liệu hiện có từ LiveData trong ViewModel.
        List<RepoDto> currentRepos = viewModel.getMyRepos().getValue();

        if (currentRepos != null) {
            // NẾU ĐÃ CÓ DỮ LIỆU:
            // Chỉ cần cập nhật giao diện ngay lập tức mà không hiển thị loading.
            adapter.submit(currentRepos);
            if (currentRepos.isEmpty()) {
                showEmpty("You don't have any repositories yet.");
            } else {
                showList(); // Ẩn vòng xoay và hiển thị danh sách.
            }
        } else {
            // NẾU CHƯA CÓ DỮ LIỆU (lần tải đầu tiên):
            // Hiển thị loading và yêu cầu ViewModel tải dữ liệu.
            showLoading(true);
            viewModel.loadMyRepos();
        }
    }

    // Trả về tiêu đề cho sección dựa trên trạng thái hiện tại.
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

    // Thiết lập RecyclerView.
    @Override
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SearchReposListAdapter(apiService);
        recyclerView.setAdapter(adapter);
    }
}