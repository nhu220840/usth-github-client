package com.usth.githubclient.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.usth.githubclient.auth.TokenStore;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.remote.dto.UserEmailDto;
import java.util.List;
import retrofit2.Response;

// AndroidViewModel là một loại ViewModel đặc biệt có thể truy cập vào Application Context.
// Điều này hữu ích khi bạn cần Context để thực hiện các tác vụ như truy cập SharedPreferences (TokenStore).
public class AuthViewModel extends AndroidViewModel {

    // Lớp AuthUiState được định nghĩa bên trong ViewModel để biểu diễn tất cả các trạng thái
    // có thể có của giao diện đăng nhập.
    public static final class AuthUiState {
        // Enum định nghĩa các trạng thái: IDLE (nghỉ), LOADING (đang tải),
        // SIGNED_IN (đã đăng nhập), ERROR (lỗi).
        public enum Status { IDLE, LOADING, SIGNED_IN, ERROR }
        public final Status status;
        public final String error;
        public final String username;

        // Constructor để tạo một đối tượng trạng thái.
        public AuthUiState(Status s, String e, String u) { status = s; error = e; username = u; }

        // Các phương thức factory tĩnh để tạo các đối tượng trạng thái một cách dễ dàng.
        public static AuthUiState idle()     { return new AuthUiState(Status.IDLE, null, null); }
        public static AuthUiState loading()  { return new AuthUiState(Status.LOADING, null, null); }
        public static AuthUiState signedIn(String u){ return new AuthUiState(Status.SIGNED_IN, null, u); }
        public static AuthUiState error(String e)    { return new AuthUiState(Status.ERROR, e, null); }
    }

    // MutableLiveData là một LiveData có thể thay đổi giá trị.
    // ViewModel sẽ cập nhật giá trị của 'ui', và Activity sẽ lắng nghe sự thay đổi này.
    private final MutableLiveData<AuthUiState> ui = new MutableLiveData<>(AuthUiState.idle());
    // LiveData là một LiveData chỉ đọc. Activity sẽ sử dụng biến này để lấy dữ liệu,
    // đảm bảo rằng chỉ ViewModel mới có quyền thay đổi trạng thái.
    public LiveData<AuthUiState> getUiState() { return ui; }

    // Constructor của ViewModel.
    public AuthViewModel(@NonNull Application app) {
        super(app);
    }

    // Phương thức đăng nhập bằng PAT (có thể không cần định danh).
    public void signInWithPat(String pat) { signInWithPat(pat, null); }

    // Phương thức chính để xử lý đăng nhập bằng Personal Access Token (PAT).
    public void signInWithPat(String pat, @Nullable String identifier) {
        // 1. Cập nhật trạng thái giao diện thành LOADING.
        // Activity sẽ nhận được trạng thái này và hiển thị ProgressBar.
        ui.postValue(AuthUiState.loading());

        // 2. Tạo một luồng (Thread) mới để thực hiện các tác vụ mạng.
        // Tác vụ mạng không được chạy trên luồng chính (UI thread) để tránh làm treo ứng dụng.
        new Thread(() -> {
            ApiClient apiClient = new ApiClient();
            try {
                // Tạo một service API tạm thời với PAT được cung cấp.
                // Service này sẽ tự động đính kèm token vào header của request.
                GithubApiService api = apiClient.createService(pat, GithubApiService.class);

                // 3. Gọi API endpoint '/user' để xác thực PAT và lấy thông tin người dùng.
                Response<UserDto> meRes = api.authenticate().execute();
                // .execute() là lời gọi đồng bộ (synchronous), nó sẽ chặn luồng cho đến khi có kết quả.
                // Đây là lý do tại sao chúng ta phải chạy nó trong một luồng riêng.

                // 4. Kiểm tra kết quả trả về.
                if (!meRes.isSuccessful() || meRes.body() == null) {
                    // Nếu PAT không hợp lệ (ví dụ: trả về lỗi 401 Unauthorized).
                    clearStoredToken(apiClient); // Xóa token cũ nếu có.
                    ui.postValue(AuthUiState.error("PAT invalid (HTTP " + meRes.code() + ")"));
                    return; // Dừng tiến trình.
                }

                UserDto me = meRes.body();
                String login = me != null ? me.getLogin() : null;
                String publicEmail = me != null ? me.getEmail() : null;

                // 5. Nếu người dùng không nhập username/email để xác minh, đăng nhập thành công.
                if (identifier == null || identifier.trim().isEmpty()) {
                    persistTokenAndSignIn(apiClient, pat, login);
                    return;
                }

                String id = identifier.trim();
                boolean isEmail = id.contains("@");

                // 6. Nếu người dùng nhập username để xác minh.
                if (!isEmail) {
                    if (login != null && login.equalsIgnoreCase(id)) {
                        // Nếu username khớp, đăng nhập thành công.
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        // Nếu không khớp, báo lỗi.
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Username không khớp với tài khoản PAT"));
                    }
                    return;
                }

                // 7. Nếu người dùng nhập email để xác minh, kiểm tra email public trước.
                if (publicEmail != null && !publicEmail.isEmpty()) {
                    if (publicEmail.equalsIgnoreCase(id)) {
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Email (public) does not match PAT account"));
                    }
                    return;
                }

                // 8. Nếu email public không có hoặc không khớp, gọi API /user/emails để kiểm tra email private.
                // Endpoint này yêu cầu PAT phải có quyền (scope) 'user:email'.
                Response<List<UserEmailDto>> emailsRes = api.getUserEmails().execute();
                if (emailsRes.isSuccessful() && emailsRes.body() != null) {
                    boolean matched = false;
                    for (UserEmailDto item : emailsRes.body()) {
                        String email = item != null ? item.getEmail() : null;
                        if (email != null && id.equalsIgnoreCase(email)) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) {
                        persistTokenAndSignIn(apiClient, pat, login);
                    } else {
                        clearStoredToken(apiClient);
                        ui.postValue(AuthUiState.error("Email does not match PAT account"));
                    }
                } else {
                    // Nếu gọi API /user/emails thất bại (ví dụ: do thiếu scope).
                    clearStoredToken(apiClient);
                    ui.postValue(AuthUiState.error(
                            "Unable to verify email. Grant scope 'user:email' to PAT or enter username to confirm."
                    ));
                }
            } catch (Exception e) {
                // Xử lý các lỗi ngoại lệ khác (ví dụ: mất kết nối mạng).
                try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
                ui.postValue(AuthUiState.error(e.getMessage()));
            }
        }).start(); // Bắt đầu chạy luồng.
    }

    // Phương thức đăng xuất.
    public void signOut() {
        ApiClient apiClient = new ApiClient();
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        apiClient.clearAuthToken();
        ui.postValue(AuthUiState.idle()); // Quay về trạng thái ban đầu.
    }

    // Phương thức trợ giúp: Lưu token và cập nhật trạng thái thành SIGNED_IN.
    private void persistTokenAndSignIn(ApiClient apiClient, String pat, String login) {
        try {
            // Lưu token vào SharedPreferences một cách an toàn.
            TokenStore.save(getApplication(), pat);
            // Thiết lập token cho ApiClient để các cuộc gọi API sau này đều được xác thực.
            apiClient.setAuthToken(pat);
            // Cập nhật giao diện.
            ui.postValue(AuthUiState.signedIn(login));
        } catch (Exception e) {
            clearStoredToken(apiClient);
            ui.postValue(AuthUiState.error(e.getMessage()));
        }
    }

    // Phương thức trợ giúp: Xóa token đã lưu.
    private void clearStoredToken(ApiClient apiClient) {
        try { TokenStore.clear(getApplication()); } catch (Exception ignored) {}
        apiClient.clearAuthToken();
    }
}