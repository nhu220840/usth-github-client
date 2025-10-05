package com.usth.githubclient.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.usth.githubclient.R;
import com.usth.githubclient.databinding.ActivityUserProfileBinding;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.model.UserSessionData;
import com.usth.githubclient.fragments.SettingsFragment;
import com.usth.githubclient.fragments.UserProfileFragment;

/**
 * Activity để hiển thị hồ sơ của một người dùng.
 */
public class UserProfileActivity extends AppCompatActivity {

    // Khóa để truyền username qua Intent.
    public static final String EXTRA_USERNAME = "com.usth.githubclient.extra.EXTRA_USERNAME";

    // Đối tượng ViewBinding để truy cập các view trong layout.
    private ActivityUserProfileBinding binding;

    // --- BẮT ĐẦU THAY ĐỔI ---
    // Cờ để xác định xem hồ sơ đang xem có phải là của người dùng đã đăng nhập hay không.
    private boolean isViewingAuthenticatedUserProfile = false;
    // --- KẾT THÚC THAY ĐỔI ---

    /**
     * Phương thức tĩnh để tạo Intent khởi động Activity này một cách an toàn.
     * @param context Context hiện tại.
     * @param username Tên người dùng cần hiển thị. Nếu null, sẽ hiển thị hồ sơ người dùng đã đăng nhập.
     * @return Intent đã được cấu hình.
     */
    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable String username) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        if (!TextUtils.isEmpty(username)) {
            // Đính kèm username vào Intent nếu có.
            intent.putExtra(EXTRA_USERNAME, username);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewBinding.
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar.
        setupToolbar();

        // --- BẮT ĐẦU THAY ĐỔI ---
        // Xác định xem người dùng có đang xem hồ sơ của chính họ không.
        // Điều này đúng nếu không có username nào được truyền qua Intent,
        // nghĩa là activity được khởi chạy từ tab Profile chính.
        isViewingAuthenticatedUserProfile = getIntent().getStringExtra(EXTRA_USERNAME) == null;
        // --- KẾT THÚC THAY ĐỔI ---

        // Chỉ thêm Fragment khi Activity được tạo lần đầu.
        if (savedInstanceState == null) {
            attachProfileFragment(resolveUsername());
        }
    }

    // Cấu hình Toolbar với nút back và tiêu đề.
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            // Hiển thị nút "Up" (back) trên Toolbar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.user_profile_title);
        }
        // Xử lý sự kiện khi nhấn nút back trên Toolbar.
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Gắn UserProfileFragment vào container của Activity.
     * @param username Tên người dùng để truyền cho Fragment.
     */
    private void attachProfileFragment(@Nullable String username) {
        // Tạo một thể hiện mới của UserProfileFragment.
        UserProfileFragment fragment = UserProfileFragment.newInstance(username);
        // Bắt đầu một transaction để thay thế container bằng Fragment.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, fragment, UserProfileFragment.TAG)
                .commit();
    }

    /**
     * Xác định username cần hiển thị.
     * Ưu tiên username được truyền qua Intent.
     * Nếu không có, lấy username từ session của người dùng đã đăng nhập.
     * @return Tên người dùng hoặc null.
     */
    @Nullable
    private String resolveUsername() {
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra(EXTRA_USERNAME);
            if (!TextUtils.isEmpty(username)) {
                return username.trim();
            }
        }
        // Nếu không có username trong Intent, kiểm tra session.
        UserSessionData session = ServiceLocator.getInstance().authRepository().getCachedSession();
        if (session != null) {
            return session.getUsername();
        }
        return null;
    }

    // Xử lý sự kiện khi nhấn nút "Up" (back) trên ActionBar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Khởi tạo menu trên Toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gắn menu (từ file XML) vào toolbar.
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);

        // --- BẮT ĐẦU THAY ĐỔI ---
        // Tìm item settings trong menu.
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (settingsItem != null) {
            // Thiết lập trạng thái hiển thị của nút settings dựa vào việc
            // người dùng có đang xem hồ sơ của chính mình hay không.
            settingsItem.setVisible(isViewingAuthenticatedUserProfile);
        }
        // --- KẾT THÚC THAY ĐỔI ---

        return true;
    }

    // Xử lý sự kiện khi một item trên menu được chọn.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Kiểm tra ID của item được chọn.
        if (item.getItemId() == R.id.action_settings) {
            // Khi nhấn nút settings, chuyển sang SettingsFragment.
            showSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Hiển thị SettingsFragment.
    private void showSettingsFragment() {
        // Thay thế fragment hiện tại bằng SettingsFragment.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_profile_fragment_container, new SettingsFragment())
                .addToBackStack(null) // Cho phép quay lại fragment trước đó bằng nút back.
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng đối tượng binding để tránh rò rỉ bộ nhớ.
        binding = null;
    }
}