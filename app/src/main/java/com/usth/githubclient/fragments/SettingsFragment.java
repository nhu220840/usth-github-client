package com.usth.githubclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Import lớp này
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.AuthenticationActivity;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.util.ThemeManager; // Import lớp tiện ích quản lý theme.

/**
 * Fragment chịu trách nhiệm hiển thị màn hình Cài đặt, cho phép người dùng
 * thay đổi giao diện và đăng xuất.
 */
public class SettingsFragment extends Fragment {

    /**
     * Được gọi để tạo View cho Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Thổi phồng" (inflate) layout từ tệp fragment_settings.xml.
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Được gọi ngay sau khi View đã được tạo. Đây là nơi để thiết lập các listener
     * và trạng thái ban đầu cho các view.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- THAY ĐỔI: Cập nhật tiêu đề của Toolbar ---
        // Lấy Activity chứa Fragment này và cập nhật lại tiêu đề của Toolbar.
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                // Đặt tiêu đề mới là "Settings" (lấy từ strings.xml).
                activity.getSupportActionBar().setTitle(getString(R.string.title_settings));
                // Xóa phụ đề (ví dụ: /@username) có thể còn lại từ màn hình Profile.
                activity.getSupportActionBar().setSubtitle(null);
            }
        }
        // --- KẾT THÚC THAY ĐỔI ---

        // --- Xử lý nút Đăng xuất ---
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout()); // Gán sự kiện click.

        // --- Xử lý Switch Dark Mode ---
        MaterialSwitch darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        // Lấy chế độ giao diện hiện tại đã được lưu trong SharedPreferences.
        int currentMode = ThemeManager.getSavedThemeMode(requireContext());

        // Cập nhật trạng thái "checked" của Switch cho khớp với chế độ hiện tại.
        darkModeSwitch.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Lắng nghe sự kiện khi người dùng gạt switch.
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Xác định chế độ mới dựa trên trạng thái "checked" của switch.
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            // Lưu lựa chọn mới của người dùng.
            ThemeManager.saveThemeMode(requireContext(), newMode);
            // Áp dụng ngay lập tức chế độ mới cho toàn bộ ứng dụng.
            ThemeManager.applyTheme(newMode);
        });
    }

    /**
     * Thực hiện quy trình đăng xuất.
     */
    private void logout() {
        // Kiểm tra an toàn để đảm bảo Fragment vẫn còn được đính kèm vào một Activity.
        if (getActivity() == null) return;

        // 1. Gọi AuthRepository để xóa session đã cache và xóa token khỏi ApiClient.
        ServiceLocator.getInstance().authRepository().signOut();

        // 2. Tạo Intent để quay về màn hình đăng nhập (AuthenticationActivity).
        Intent intent = new Intent(getActivity(), AuthenticationActivity.class);

        // 3. Các cờ (flags) này sẽ xóa tất cả các Activity khác khỏi "lịch sử" (back stack),
        // đảm bảo người dùng không thể nhấn nút Back để quay lại màn hình chính sau khi đã đăng xuất.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 4. Khởi chạy màn hình đăng nhập.
        startActivity(intent);

        // 5. Đóng Activity hiện tại (UserProfileActivity).
        getActivity().finish();
    }
}