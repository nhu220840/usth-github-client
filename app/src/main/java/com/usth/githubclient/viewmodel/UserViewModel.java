package com.usth.githubclient.viewmodel;

// Các import cần thiết
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.data.repository.UserRepositoryImpl;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.domain.model.UserSessionData;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

// ViewModel chịu trách nhiệm cung cấp và quản lý dữ liệu cho UserProfileFragment.
public class UserViewModel extends ViewModel {

    // MutableLiveData chứa trạng thái hiện tại của giao diện (loading, success, error).
    // Nó là 'private' để chỉ ViewModel mới có quyền thay đổi trạng thái.
    private final MutableLiveData<UserUiState> uiState = new MutableLiveData<>(UserUiState.idle());

    // ExecutorService dùng để thực thi các tác vụ mạng trên một luồng nền,
    // tránh làm treo giao diện người dùng. newSingleThreadExecutor đảm bảo
    // các yêu cầu được thực hiện tuần tự, tránh xung đột.
    private final ExecutorService executorService;

    // Khai báo các Repository và Mapper cần thiết mà ViewModel này phụ thuộc vào.
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Các biến để lưu lại trạng thái, giúp ViewModel biết đang hiển thị profile của ai.
    private String currentUsername;
    private boolean displayingAuthenticatedProfile;


    // Các chuỗi thông báo lỗi. (Cải tiến: Nên đưa vào tệp strings.xml)
    private static final String GENERIC_ERROR_MESSAGE = "Unable to load this profile right now.";
    private static final String AUTH_REQUIRED_ERROR_MESSAGE =
            "Sign in with a personal access token to view your profile.";

    // Constructor mặc định, sử dụng ServiceLocator để tự động lấy các dependency.
    public UserViewModel() {
        this(ServiceLocator.getInstance().authRepository(), buildDefaultUserRepository(), ServiceLocator.getInstance().userMapper());
    }

    // Constructor này cho phép "tiêm" (inject) các dependency từ bên ngoài.
    // Đây là một thực hành tốt, giúp cho việc viết unit test trở nên cực kỳ dễ dàng.
    public UserViewModel(@NonNull AuthRepository authRepository,
                         @NonNull UserRepository userRepository,
                         @NonNull UserMapper userMapper) {
        this.authRepository = Objects.requireNonNull(authRepository, "authRepository == null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository == null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Phương thức trợ giúp để tạo một UserRepository mặc định.
    private static UserRepository buildDefaultUserRepository() {
        ApiClient apiClient = new ApiClient();
        return new UserRepositoryImpl(apiClient);
    }

    // Cung cấp một LiveData chỉ đọc (read-only) cho Fragment.
    // Điều này đảm bảo rằng Fragment chỉ có thể quan sát dữ liệu chứ không thể thay đổi nó.
    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    /**
     * Phương thức chính để bắt đầu quá trình tải hồ sơ người dùng.
     * @param username Tên người dùng cần tải. Nếu là null hoặc rỗng, sẽ tải hồ sơ của
     * người dùng đã đăng nhập.
     */
    public void loadUserProfile(@Nullable String username) {
        String normalized = username == null ? "" : username.trim();
        boolean viewingAuthenticatedUser = normalized.isEmpty();

        // Tối ưu hóa: Nếu đang yêu cầu xem lại cùng một profile đã được hiển thị,
        // thì không cần gọi lại API.
        if (viewingAuthenticatedUser) {
            if (displayingAuthenticatedProfile && hasExistingProfile()) {
                return;
            }
            if (tryLoadProfileFromSession()) {
                return; // Nếu tải thành công từ cache thì không cần gọi API.
            }
        } else if (normalized.equalsIgnoreCase(currentUsername) && hasExistingProfile()) {
            return;
        }

        // Cập nhật các biến trạng thái và phát ra trạng thái LOADING.
        displayingAuthenticatedProfile = viewingAuthenticatedUser;
        currentUsername = viewingAuthenticatedUser ? null : normalized;
        uiState.setValue(UserUiState.loading()); // setValue() dùng trên luồng chính.

        final String requestedUsername = normalized;
        // Thực thi việc gọi API trên luồng nền đã tạo.
        executorService.execute(() -> {
            if (viewingAuthenticatedUser) {
                fetchAuthenticatedProfile();
            } else {
                fetchUserProfileByUsername(requestedUsername);
            }
        });
    }

    // Kiểm tra xem trạng thái hiện tại đã có dữ liệu profile chưa.
    private boolean hasExistingProfile() {
        UserUiState state = uiState.getValue();
        return state != null && state.getProfile() != null;
    }

    // Cố gắng tải profile từ session đã được cache trong AuthRepository khi khởi động.
    private boolean tryLoadProfileFromSession() {
        UserSessionData session = authRepository.getCachedSession();
        if (session == null) return false;

        Optional<GitHubUserProfileDataEntry> profile = session.getUserProfile();
        if (profile.isEmpty()) return false;

        GitHubUserProfileDataEntry cachedProfile = profile.get();
        currentUsername = cachedProfile.getUsername();
        displayingAuthenticatedProfile = true;
        uiState.setValue(UserUiState.success(cachedProfile)); // Cập nhật UI với dữ liệu cache.
        return true;
    }

    // Lấy hồ sơ của người dùng đã đăng nhập từ API (/user).
    private void fetchAuthenticatedProfile() {
        try {
            // .execute() là lời gọi đồng bộ, phải chạy trên luồng nền.
            Response<UserDto> response = userRepository.authenticate().execute();
            if (response.isSuccessful() && response.body() != null) {
                GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                currentUsername = profile.getUsername();
                displayingAuthenticatedProfile = true;
                // postValue() được dùng để cập nhật LiveData từ một luồng nền.
                uiState.postValue(UserUiState.success(profile));
                return;
            }

            // Xử lý các mã lỗi HTTP.
            if (response.code() == 401 || response.code() == 403) {
                uiState.postValue(UserUiState.error(AUTH_REQUIRED_ERROR_MESSAGE));
            } else {
                String errorMsg = "Unable to load this profile right now. (Code: " + response.code() + ")";
                uiState.postValue(UserUiState.error(errorMsg));
            }
        } catch (IOException exception) {
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = GENERIC_ERROR_MESSAGE;
            }
            uiState.postValue(UserUiState.error(message));
        }
    }

    // Lấy hồ sơ của một người dùng cụ thể từ API (/users/{username}).
    private void fetchUserProfileByUsername(@NonNull String username) {
        try {
            Response<UserDto> response = userRepository.getUser(username).execute();
            if (response.isSuccessful() && response.body() != null) {
                GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                currentUsername = profile.getUsername();
                displayingAuthenticatedProfile = false;
                uiState.postValue(UserUiState.success(profile));
                return;
            }

            String errorMsg = "Unable to load this profile right now. (Code: " + response.code() + ")";
            uiState.postValue(UserUiState.error(errorMsg));
        } catch (IOException exception) {
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = GENERIC_ERROR_MESSAGE;
            }
            uiState.postValue(UserUiState.error(message));
        }
    }

    // Được gọi khi người dùng muốn thử lại sau khi có lỗi.
    public void retry() {
        loadUserProfile(displayingAuthenticatedProfile ? null : currentUsername);
    }

    // Được gọi tự động khi ViewModel không còn được sử dụng và sắp bị hủy.
    @Override
    protected void onCleared() {
        super.onCleared();
        // Dọn dẹp, tắt ExecutorService để tránh rò rỉ luồng (memory leak).
        executorService.shutdownNow();
    }

    // Lớp nội tại UserUiState để biểu diễn các trạng thái giao diện một cách tường minh.
    // Việc này giúp mã nguồn trong Fragment trở nên sạch sẽ hơn khi xử lý các trạng thái.
    public static final class UserUiState {
        private final boolean loading;
        private final GitHubUserProfileDataEntry profile;
        private final String errorMessage;

        private UserUiState(boolean loading,
                            GitHubUserProfileDataEntry profile,
                            String errorMessage) {
            this.loading = loading;
            this.profile = profile;
            this.errorMessage = errorMessage;
        }

        // Các phương thức factory tĩnh giúp tạo đối tượng State dễ dàng hơn.
        public static UserUiState idle() {
            return new UserUiState(false, null, null);
        }
        public static UserUiState loading() {
            return new UserUiState(true, null, null);
        }
        public static UserUiState success(@NonNull GitHubUserProfileDataEntry profile) {
            return new UserUiState(false, Objects.requireNonNull(profile, "profile == null"), null);
        }
        public static UserUiState error(@NonNull String message) {
            return new UserUiState(false, null, Objects.requireNonNull(message, "message == null"));
        }

        // Các getter để Fragment lấy thông tin.
        public boolean isLoading() { return loading; }
        @Nullable public GitHubUserProfileDataEntry getProfile() { return profile; }
        @Nullable public String getErrorMessage() { return errorMessage; }
    }
}