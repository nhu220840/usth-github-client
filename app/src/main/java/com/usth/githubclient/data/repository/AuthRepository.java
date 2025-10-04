package com.usth.githubclient.data.repository;

import android.annotation.SuppressLint;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.domain.model.ReposDataEntry;
import com.usth.githubclient.domain.model.UserSessionData;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

/**
 * Xử lý việc xác thực bằng personal access token và cung cấp dữ liệu phiên đăng nhập.
 * Lớp này điều phối nhiều nguồn dữ liệu khác (UserRepository, RepoRepository) để
 * hoàn thành một tác vụ nghiệp vụ phức tạp.
 */
public final class AuthRepository {

    // Các hằng số mặc định cho việc tải danh sách repositories.
    private static final int DEFAULT_REPO_PAGE = 1;
    private static final int DEFAULT_REPO_PER_PAGE = 30;
    private static final String DEFAULT_SORT = "updated"; // Sắp xếp theo lần cập nhật gần nhất.

    // Các dependency của AuthRepository.
    private final ApiClient apiClient;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;

    // Biến để cache (lưu trữ tạm thời) phiên đăng nhập hiện tại, giúp tránh
    // phải gọi lại API không cần thiết.
    private UserSessionData cachedSession;

    // Sử dụng Constructor Injection để nhận các dependency.
    public AuthRepository(
            ApiClient apiClient,
            UserRepository userRepository,
            RepoRepository repoRepository,
            UserMapper userMapper,
            RepoMapper repoMapper
    ) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient == null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository == null");
        this.repoRepository = Objects.requireNonNull(repoRepository, "repoRepository == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.repoMapper = Objects.requireNonNull(repoMapper, "repoMapper == null");
    }

    /**
     * Thực hiện quá trình xác thực hoàn chỉnh bằng PAT.
     * @param personalAccessToken Token do người dùng cung cấp.
     * @return Một đối tượng UserSessionData chứa toàn bộ thông tin phiên đăng nhập.
     * @throws IOException Nếu có lỗi mạng hoặc lỗi xác thực.
     */
    @SuppressLint("NewApi") // Cần cho việc sử dụng Instant.
    public UserSessionData authenticate(String personalAccessToken) throws IOException {
        if (personalAccessToken == null || personalAccessToken.isEmpty()) {
            throw new IllegalArgumentException("personalAccessToken cannot be null or empty");
        }

        // 1. Thiết lập token cho ApiClient. Kể từ đây, mọi cuộc gọi API
        //    sử dụng các repository (userRepository, repoRepository) sẽ tự động được xác thực.
        apiClient.setAuthToken(personalAccessToken);

        // 2. Lần lượt gọi API để lấy thông tin người dùng và danh sách repo của họ.
        //    Đây là các lời gọi đồng bộ (synchronous), phải được thực hiện trên một luồng nền.
        GitHubUserProfileDataEntry profile = fetchAuthenticatedUser();
        List<ReposDataEntry> repositories = fetchAuthenticatedRepositories();

        // 3. Tạo đối tượng UserSessionData hoàn chỉnh và lưu vào cache.
        cachedSession = UserSessionData.builder(profile.getUsername(), personalAccessToken)
                .tokenType("Bearer")
                .userProfile(profile)
                .repositories(repositories)
                .lastSyncedAt(Instant.now())
                .build();

        // **THAY ĐỔI**: Xóa dòng apiClient.setAuthToken(personalAccessToken); thừa ở đây.
        // Token đã được thiết lập ở đầu phương thức rồi.

        return cachedSession;
    }

    /**
     * Xử lý việc đăng xuất bằng cách xóa session đã cache và xóa token khỏi ApiClient.
     */
    public void signOut() {
        cachedSession = null;
        apiClient.clearAuthToken();
    }

    /**
     * Cung cấp phiên đăng nhập đã được cache.
     * @return UserSessionData nếu đã đăng nhập, hoặc null nếu chưa.
     */
    public UserSessionData getCachedSession() {
        return cachedSession;
    }

    /**
     * Phương thức private để lấy thông tin của người dùng đã xác thực.
     */
    private GitHubUserProfileDataEntry fetchAuthenticatedUser() throws IOException {
        Response<UserDto> response = userRepository.authenticate().execute();
        if (response.isSuccessful() && response.body() != null) {
            // Nếu thành công, dùng UserMapper để chuyển đổi DTO thành Model domain.
            return userMapper.map(response.body());
        }
        // Nếu thất bại, xóa token và ném ra ngoại lệ với thông tin chi tiết.
        apiClient.clearAuthToken();
        throw buildException("Unable to fetch authenticated user", response);
    }

    /**
     * Phương thức private để lấy danh sách repo của người dùng đã xác thực.
     */
    private List<ReposDataEntry> fetchAuthenticatedRepositories() throws IOException {
        Response<List<RepoDto>> response = repoRepository
                .getAuthenticatedRepositories(DEFAULT_REPO_PAGE, DEFAULT_REPO_PER_PAGE, DEFAULT_SORT)
                .execute();
        if (response.isSuccessful() && response.body() != null) {
            // Nếu thành công, dùng RepoMapper để chuyển đổi danh sách DTO thành danh sách Model.
            return repoMapper.mapList(response.body());
        }
        // GitHub API có thể trả về mã 404 nếu người dùng không có repo nào, đây không phải là lỗi.
        if (response.code() == 404) {
            return Collections.emptyList(); // Trả về danh sách rỗng.
        }
        // Đối với các lỗi khác, ném ra ngoại lệ.
        throw buildException("Unable to fetch repositories for authenticated user", response);
    }

    /**
     * Phương thức trợ giúp để xây dựng một thông báo lỗi chi tiết từ phản hồi của API.
     */
    private IOException buildException(String message, Response<?> response) {
        String errorBody = null;
        try {
            // Cố gắng đọc nội dung lỗi từ API để có thông báo rõ ràng hơn.
            errorBody = response != null && response.errorBody() != null
                    ? response.errorBody().string()
                    : null;
        } catch (IOException ignored) {
            // Bỏ qua nếu không đọc được.
        }
        if (errorBody == null || errorBody.isEmpty()) {
            return new IOException(message + " (Code: " + (response != null ? response.code() : "N/A") + ")");
        }
        return new IOException(message + ": " + errorBody);
    }
}