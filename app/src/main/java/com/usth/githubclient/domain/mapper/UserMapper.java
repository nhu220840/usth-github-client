package com.usth.githubclient.domain.mapper;

// Các import cần thiết.
import android.annotation.SuppressLint;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import java.time.Instant; // Dùng để xử lý các mốc thời gian.
import java.time.format.DateTimeParseException; // Dùng để bắt lỗi khi phân tích chuỗi ngày tháng.
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ánh xạ (Maps) các giá trị {@link UserDto} từ tầng dữ liệu (data layer)
 * thành các đối tượng model của tầng domain mà phần còn lại của ứng dụng có thể sử dụng.
 * Mục đích là để tách biệt cấu trúc dữ liệu thô từ API với cấu trúc dữ liệu
 * an toàn và nhất quán được dùng trong logic nghiệp vụ.
 */
public final class UserMapper {

    /**
     * Chuyển đổi một đối tượng UserDto từ API thành một đối tượng domain bất biến (immutable).
     *
     * @param dto Đối tượng thô (raw payload) được lấy từ service API (Retrofit/Gson).
     * @return Đối tượng model (GitHubUserProfileDataEntry) mô tả cùng một người dùng.
     */
    public GitHubUserProfileDataEntry map(UserDto dto) {
        // Luôn kiểm tra null ở đầu phương thức để tránh NullPointerException.
        Objects.requireNonNull(dto, "dto == null");

        // Yêu cầu trường 'login' (username) phải tồn tại, vì nó là định danh chính.
        String username = requireNonEmpty(dto.getLogin(), "login");
        // Sử dụng mẫu thiết kế Builder để tạo đối tượng bất biến một cách an toàn và linh hoạt.
        GitHubUserProfileDataEntry.Builder builder =
                GitHubUserProfileDataEntry.builder(dto.getId(), username);

        // Đối với mỗi trường dữ liệu, ta "chuẩn hóa" nó và chỉ gán vào builder nếu nó có giá trị.
        // Việc này đảm bảo đối tượng domain của chúng ta không chứa các chuỗi rỗng hoặc chỉ có khoảng trắng.
        String displayName = normalize(dto.getName());
        if (displayName != null) {
            builder.displayName(displayName);
        }

        String avatarUrl = normalize(dto.getAvatarUrl());
        if (avatarUrl != null) {
            builder.avatarUrl(avatarUrl);
        }

        String bio = normalize(dto.getBio());
        if (bio != null) {
            builder.bio(bio);
        }

        String company = normalize(dto.getCompany());
        if (company != null) {
            builder.company(company);
        }

        String blog = normalize(dto.getBlog());
        if (blog != null) {
            builder.blogUrl(blog);
        }

        String email = normalize(dto.getEmail());
        if (email != null) {
            builder.email(email);
        }

        String location = normalize(dto.getLocation());
        if (location != null) {
            builder.location(location);
        }

        // Đối với các giá trị số, ta dùng hàm 'safeCount' để đảm bảo chúng không phải là số âm.
        builder.publicReposCount(safeCount(dto.getPublicRepos()));
        builder.followersCount(safeCount(dto.getFollowers()));
        builder.followingCount(safeCount(dto.getFollowing()));

        String profileUrl = normalize(dto.getHtmlUrl());
        if (profileUrl != null) {
            builder.profileUrl(profileUrl);
        }

        // Chuyển đổi các chuỗi ngày tháng từ API (định dạng ISO 8601) thành đối tượng Instant.
        Instant createdAt = parseInstant(dto.getCreatedAt());
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }

        Instant updatedAt = parseInstant(dto.getUpdatedAt());
        if (updatedAt != null) {
            builder.updatedAt(updatedAt);
        }

        // Xây dựng và trả về đối tượng GitHubUserProfileDataEntry cuối cùng.
        return builder.build();
    }

    /**
     * Phương thức trợ giúp: Đảm bảo một chuỗi không null hoặc chỉ chứa khoảng trắng.
     * Ném ra ngoại lệ nếu không hợp lệ.
     */
    private String requireNonEmpty(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return normalized;
    }

    /**
     * Phương thức trợ giúp: Chuẩn hóa một chuỗi.
     * - Xóa khoảng trắng ở đầu và cuối.
     * - Trả về null nếu chuỗi ban đầu là null hoặc chuỗi sau khi xóa khoảng trắng là rỗng.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Phương thức trợ giúp: Đảm bảo một giá trị số (count) là hợp lệ.
     * Trả về 0 nếu giá trị là null hoặc nhỏ hơn 0.
     */
    private int safeCount(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    /**
     * Phương thức trợ giúp: Phân tích một chuỗi ngày tháng theo chuẩn ISO 8601 thành đối tượng Instant.
     * Trả về null nếu chuỗi không hợp lệ hoặc rỗng.
     */
    @SuppressLint("NewApi") // Cần thiết vì Instant là một phần của Java 8 API (API level 26+).
    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // Lặng lẽ bỏ qua lỗi và trả về null nếu chuỗi không đúng định dạng.
            return null;
        }
    }

    /**
     * Ánh xạ một danh sách các đối tượng UserDto sang một danh sách các đối tượng GitHubUserProfileDataEntry.
     */
    public List<GitHubUserProfileDataEntry> mapList(List<UserDto> dtoList) {
        List<GitHubUserProfileDataEntry> result = new ArrayList<>();
        if (dtoList != null) {
            for (UserDto dto : dtoList) {
                // Tái sử dụng phương thức map cho từng đối tượng.
                GitHubUserProfileDataEntry entry = map(dto);
                if (entry != null) result.add(entry);
            }
        }
        return result;
    }
}