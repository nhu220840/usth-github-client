package com.usth.githubclient.fragments;

// Các import cần thiết cho việc xử lý Intent, URI, Bundle và các thành phần UI cơ bản.
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

// Các import từ thư viện AndroidX cho các thành phần cốt lõi.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

// Các import từ thư viện bên ngoài và các lớp của dự án.
import com.bumptech.glide.Glide; // Thư viện tải và hiển thị hình ảnh.
import com.usth.githubclient.R;
import com.usth.githubclient.databinding.FragmentUserProfileBinding; // Lớp ViewBinding được tạo tự động.
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry; // Lớp model cho dữ liệu profile.
import com.usth.githubclient.viewmodel.UserViewModel; // ViewModel tương ứng.

// Các import từ Java.
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

// Fragment chịu trách nhiệm hiển thị giao diện chi tiết của một hồ sơ người dùng.
public class UserProfileFragment extends Fragment {

    public static final String TAG = "UserProfileFragment";
    private static final String ARG_USERNAME = "arg_username"; // Key để truyền username qua Bundle.

    // Sử dụng ViewBinding thay cho findViewById để truy cập view một cách an toàn và hiệu quả.
    private FragmentUserProfileBinding binding;
    // Khai báo ViewModel.
    private UserViewModel viewModel;

    /**
     * Phương thức factory tĩnh để tạo Fragment mới.
     * Cách làm này giúp đóng gói việc truyền đối số (arguments) vào Fragment
     * một cách an toàn và rõ ràng.
     * @param username Tên người dùng cần hiển thị.
     */
    public static UserProfileFragment newInstance(@Nullable String username) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(username)) {
            args.putString(ARG_USERNAME, username);
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Được gọi để tạo View cho Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // "Thổi phồng" (inflate) layout bằng ViewBinding và trả về view gốc (binding.getRoot()).
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Được gọi ngay sau khi View đã được tạo. Đây là nơi an toàn để thực hiện các thao tác
     * liên quan đến view.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Khởi tạo ViewModel, gắn nó với vòng đời của Fragment này.
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        // Lắng nghe (observe) LiveData chứa trạng thái UI từ ViewModel.
        // Khi trạng thái thay đổi, phương thức 'renderState' sẽ được gọi.
        // getViewLifecycleOwner() đảm bảo observer này sẽ tự động bị hủy khi Fragment bị hủy.
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);

        // Lấy username từ arguments và yêu cầu ViewModel bắt đầu tải dữ liệu.
        String username = getArguments() == null ? null : getArguments().getString(ARG_USERNAME);
        viewModel.loadUserProfile(username);
    }

    /**
     * Phương thức cốt lõi: nhận trạng thái mới từ ViewModel và cập nhật (render) giao diện.
     * @param state Đối tượng UserUiState chứa thông tin về trạng thái hiện tại (loading, success, error).
     */
    private void renderState(@Nullable UserViewModel.UserUiState state) {
        // Kiểm tra an toàn, đảm bảo binding và state không null.
        if (binding == null || state == null) {
            return;
        }

        // 1. Cập nhật trạng thái Loading: Hiển thị/ẩn ProgressBar.
        binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

        // 2. Cập nhật trạng thái Lỗi: Hiển thị container lỗi và thông báo.
        if (state.getErrorMessage() != null) {
            binding.contentScroll.setVisibility(View.GONE); // Ẩn nội dung chính.
            binding.errorContainer.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(state.getErrorMessage());
        } else {
            binding.errorContainer.setVisibility(View.GONE); // Ẩn container lỗi nếu không có lỗi.
        }

        // 3. Cập nhật trạng thái Thành công: Lấy dữ liệu profile và hiển thị.
        GitHubUserProfileDataEntry profile = state.getProfile();
        if (profile != null) {
            binding.contentScroll.setVisibility(View.VISIBLE); // Hiển thị nội dung chính.
            bindProfile(profile); // Gọi phương thức trợ giúp để gán dữ liệu vào các view.
        } else if (state.getErrorMessage() == null) {
            // Nếu không có profile và cũng không có lỗi (trạng thái idle ban đầu), ẩn nội dung chính.
            binding.contentScroll.setVisibility(View.GONE);
        }
    }

    /**
     * Gán dữ liệu từ đối tượng GitHubUserProfileDataEntry vào các view tương ứng trên giao diện.
     * @param profile Đối tượng chứa thông tin profile đã được xử lý.
     */
    private void bindProfile(@NonNull GitHubUserProfileDataEntry profile) {
        // Ưu tiên hiển thị displayName, nếu không có thì dùng username.
        String displayName = profile.getDisplayName().orElse(profile.getUsername());
        binding.displayName.setText(displayName);
        binding.username.setText(getString(R.string.user_profile_username_format, profile.getUsername()));

        updateTextOrHide(binding.bio, profile.getBio().orElse(null));

        // Dùng thư viện Glide để tải và hiển thị avatar từ URL.
        Glide.with(binding.avatar)
                .load(profile.getAvatarUrl().orElse(null))
                .placeholder(R.drawable.ic_avatar_placeholder) // Ảnh hiển thị trong khi tải.
                .error(R.drawable.ic_avatar_placeholder)     // Ảnh hiển thị nếu tải lỗi.
                .circleCrop()                                // Bo tròn ảnh.
                .into(binding.avatar);

        // Hiển thị các thông số thống kê.
        binding.repositoriesValue.setText(String.valueOf(profile.getPublicReposCount()));
        binding.followersValue.setText(String.valueOf(profile.getFollowersCount()));
        binding.followingValue.setText(String.valueOf(profile.getFollowingCount()));

        // Cập nhật các thông tin chi tiết khác bằng các phương thức trợ giúp.
        updateRow(binding.locationGroup, binding.locationValue, profile.getLocation().orElse(null));
        updateRow(binding.companyGroup, binding.companyValue, profile.getCompany().orElse(null));
        updateRow(binding.emailGroup, binding.emailValue, profile.getEmail().orElse(null));
        updateLinkRow(binding.blogGroup, binding.blogValue, profile.getBlogUrl().orElse(null));
        updateLinkRow(binding.profileGroup, binding.profileValue, profile.getProfileUrl().orElse(null));
        updateRow(binding.createdAtGroup, binding.createdAtValue, formatDate(profile.getCreatedAt().orElse(null)));
        updateRow(binding.updatedAtGroup, binding.updatedAtValue, formatDate(profile.getUpdatedAt().orElse(null)));

        // Cập nhật tiêu đề của Toolbar trong Activity chứa Fragment này.
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(displayName);
            activity.getSupportActionBar().setSubtitle(
                    getString(R.string.user_profile_username_format, profile.getUsername()));
        }
    }

    /**
     * Phương thức trợ giúp: Hiển thị một TextView nếu có giá trị, nếu không thì ẩn nó đi.
     */
    private void updateTextOrHide(@NonNull TextView view, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            view.setVisibility(View.GONE);
            view.setText(null);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(value);
        }
    }

    /**
     * Phương thức trợ giúp: Hiển thị một hàng (bao gồm cả group và value) nếu có giá trị,
     * nếu không thì ẩn cả group đi.
     */
    private void updateRow(@NonNull View group, @NonNull TextView valueView, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            group.setVisibility(View.GONE);
            valueView.setText(null);
        } else {
            group.setVisibility(View.VISIBLE);
            valueView.setText(value);
        }
    }

    /**
     * Tương tự updateRow, nhưng dành cho các hàng chứa link và thêm sự kiện click.
     */
    private void updateLinkRow(@NonNull View group, @NonNull TextView valueView, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            group.setVisibility(View.GONE);
            valueView.setText(null);
            valueView.setOnClickListener(null); // Xóa listener cũ để tránh lỗi.
            return;
        }
        group.setVisibility(View.VISIBLE);
        valueView.setText(value);
        String preparedUrl = prepareUrl(value); // Chuẩn hóa URL.
        valueView.setOnClickListener(v -> openLink(preparedUrl)); // Gán sự kiện click.
    }

    /**
     * Định dạng đối tượng Instant thành một chuỗi ngày tháng dễ đọc.
     */
    @Nullable
    private String formatDate(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        return dateFormat.format(Date.from(instant));
    }

    /**
     * Chuẩn hóa một URL. Nếu URL không có scheme (http/https), nó sẽ tự động thêm "https://".
     */
    @NonNull
    private String prepareUrl(@NonNull String raw) {
        String trimmed = raw.trim();
        if (TextUtils.isEmpty(trimmed)) {
            return raw;
        }
        Uri uri = Uri.parse(trimmed);
        if (uri.getScheme() == null) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    /**
     * Mở một URL trong trình duyệt.
     */
    private void openLink(@NonNull String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // Kiểm tra xem có ứng dụng nào có thể xử lý Intent này không.
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException ignored) {
            // Lặng lẽ bỏ qua nếu không có trình duyệt nào được cài đặt.
        }
    }

    /**
     * Được gọi khi View của Fragment sắp bị hủy.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Rất quan trọng: Đặt binding thành null để giải phóng tài nguyên và tránh rò rỉ bộ nhớ.
        binding = null;
    }
}