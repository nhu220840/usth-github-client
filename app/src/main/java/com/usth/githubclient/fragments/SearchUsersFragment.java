package com.usth.githubclient.fragments;

// Các import cần thiết cho Fragment, View, và các thành phần Android.
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

// Các import cho các lớp của dự án.
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.SearchUsersListAdapter;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;

// Các import thư viện và Java.
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// SearchUsersFragment kế thừa từ BaseFragment, lớp cơ sở mà bạn đã tạo
// để tái sử dụng logic chung cho việc quản lý RecyclerView, ProgressBar, và EmptyView.
public class SearchUsersFragment extends BaseFragment {
    // Adapter để cung cấp dữ liệu cho RecyclerView.
    private SearchUsersListAdapter adapter;
    // Khởi tạo ApiClient để tạo service.
    private final ApiClient apiClient = new ApiClient();
    // Service của Retrofit để thực hiện các cuộc gọi API.
    private GithubApiService apiService;
    // VẤN ĐỀ: Dữ liệu cache này sẽ bị mất khi Fragment được tạo lại (ví dụ: xoay màn hình).
    private final List<SearchUsersListAdapter.UserRow> cachedFollowers = new ArrayList<>();
    private List<UserDto> lastFollowersResponse = new ArrayList<>();
    private boolean hasLoadedFollowers;
    private String authenticatedUsername;
    private String lastSearchQuery;

    // Enum để quản lý trạng thái của danh sách: đang hiển thị followers hay kết quả tìm kiếm.
    private enum ListMode { FOLLOWERS, SEARCH }
    private ListMode listMode = ListMode.FOLLOWERS;

    // Phương thức factory tĩnh để tạo một thực thể mới của Fragment.
    public static SearchUsersFragment newInstance() {
        return new SearchUsersFragment();
    }

    // Được gọi khi Fragment được tạo lần đầu.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo apiService để sẵn sàng cho các cuộc gọi mạng.
        apiService = apiClient.createService(GithubApiService.class);
    }

    // Được gọi để tạo và trả về View cho Fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // "Thổi phồng" (inflate) layout từ tệp XML.
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        // Ánh xạ các view từ layout vào các biến đã khai báo.
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);

        // Thiết lập RecyclerView và Adapter.
        setupRecyclerView();

        // Mặc định, hiển thị danh sách followers khi Fragment được tạo.
        displayFollowers();
        return v;
    }

    /**
     * API công khai (Public API) để MainActivity có thể gọi khi người dùng thực hiện tìm kiếm.
     */
    public void submitQuery(String username) {
        // Nếu query rỗng, quay lại hiển thị danh sách followers.
        if (username == null || username.trim().isEmpty()) {
            displayFollowers();
            return;
        }
        if (apiService == null) {
            showEmpty("Unable to initialize network client.");
            return;
        }
        final String q = username.trim();
        // Chuyển sang chế độ tìm kiếm và lưu lại query.
        listMode = ListMode.SEARCH;
        lastSearchQuery = q;
        showLoading(true); // Hiển thị ProgressBar.

        // Thực hiện cuộc gọi API để tìm kiếm người dùng.
        apiService
                .searchUsers(q, 1, 30) // Tìm trang 1, 30 kết quả mỗi trang.
                .enqueue(new Callback<SearchUsersResponseDto>() { // enqueue() để chạy bất đồng bộ.
                    @Override
                    public void onResponse(Call<SearchUsersResponseDto> call,
                                           Response<SearchUsersResponseDto> resp) {
                        // Kiểm tra xem Fragment còn được đính kèm vào Activity không.
                        if (!isAdded()) return;
                        // Đảm bảo rằng kết quả này là dành cho query tìm kiếm mới nhất.
                        if (listMode != ListMode.SEARCH
                                || lastSearchQuery == null
                                || !lastSearchQuery.equals(q)) {
                            return;
                        }
                        showLoading(false); // Ẩn ProgressBar.

                        if (resp.isSuccessful() && resp.body() != null) {
                            // Chuyển đổi từ UserDto (dữ liệu thô từ API)
                            // sang UserRow (dữ liệu mà Adapter cần).
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
                            adapter.submit(ui); // Cập nhật dữ liệu cho Adapter.

                            if (ui.isEmpty()) showEmpty("No users match: " + q);
                            else showList();

                            // Lấy thêm thông tin chi tiết cho từng người dùng.
                            enrichUserDetails(items);

                        } else {
                            showEmpty("Search failed: " + resp.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchUsersResponseDto> call, Throwable t) {
                        if (!isAdded() || listMode != ListMode.SEARCH || !lastSearchQuery.equals(q)) return;
                        showLoading(false);
                        showEmpty("Network error: " + t.getMessage());
                    }
                });
    }

    /**
     * API công khai được MainActivity sử dụng khi người dùng xóa nội dung tìm kiếm.
     */
    public void showFollowers() {
        displayFollowers();
    }

    // Phương thức để hiển thị danh sách những người theo dõi (followers).
    private void displayFollowers() {
        listMode = ListMode.FOLLOWERS;
        lastSearchQuery = null;
        if (apiService == null) {
            showEmpty(getString(R.string.followers_load_failed));
            return;
        }

        // Nếu đã tải followers trước đó, sử dụng dữ liệu cache.
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

        // Nếu đã biết username của người dùng đã xác thực, trực tiếp fetch followers.
        if (authenticatedUsername != null && !authenticatedUsername.trim().isEmpty()) {
            fetchFollowers(authenticatedUsername);
        } else {
            // Nếu chưa, gọi API /user để lấy username trước.
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

    // Lấy danh sách followers cho một username cụ thể.
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
                            if (u == null || u.getLogin() == null || u.getAvatarUrl() == null) continue;
                            ui.add(new SearchUsersListAdapter.UserRow(u.getLogin(), u.getAvatarUrl()));
                            detailSource.add(u);
                        }
                    }

                    // Lưu dữ liệu vào cache.
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

    // Lấy thêm thông tin chi tiết (tên, bio,...) cho mỗi người dùng trong danh sách.
    private void enrichUserDetails(List<UserDto> items) {
        if (apiService == null || items == null) return;

        // Giới hạn chỉ enrich 100 người dùng đầu tiên để tránh quá nhiều cuộc gọi API.
        int limit = Math.min(items.size(), 100);
        for (int i = 0; i < limit; i++) {
            final UserDto dto = items.get(i);
            if (dto == null) continue;
            final String login = dto.getLogin();
            if (login == null || login.trim().isEmpty()) continue;

            // Gọi API /users/{username} cho từng người.
            apiService.getUser(login)
                    .enqueue(new Callback<UserDto>() {
                        @Override
                        public void onResponse(Call<UserDto> call, Response<UserDto> resp) {
                            if (!isAdded()) return;
                            if (resp.isSuccessful() && resp.body() != null) {
                                UserDto detail = resp.body();
                                // Cập nhật lại mục tương ứng trong Adapter với thông tin mới.
                                adapter.updateDetails(
                                        login,
                                        detail.getName(),
                                        detail.getBio(),
                                        detail.getPublicRepos(),
                                        detail.getFollowers());
                            }
                        }

                        @Override public void onFailure(Call<UserDto> call, Throwable t) { /* Lỗi có thể bỏ qua */ }
                    });
        }
    }

    // Trả về tiêu đề cho sección dựa trên trạng thái hiện tại.
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
        return null;
    }

    // Thiết lập RecyclerView và Adapter.
    @Override
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Thêm một đường kẻ phân cách giữa các mục.
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SearchUsersListAdapter();
        recyclerView.setAdapter(adapter);
    }
}