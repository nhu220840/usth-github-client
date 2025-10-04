package com.usth.githubclient.domain.mapper;

// Các import cần thiết.
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.ReposDataEntry;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Ánh xạ (Maps) các đối tượng {@link RepoDto} từ API thành các đối tượng
 * {@link ReposDataEntry} của tầng domain.
 */
public final class RepoMapper {

    // RepoMapper phụ thuộc vào UserMapper. Điều này là cần thiết vì một đối tượng RepoDto
    // chứa một đối tượng UserDto (thông tin về chủ sở hữu - owner).
    private final UserMapper userMapper;

    // Sử dụng Constructor Injection để nhận UserMapper.
    // Điều này giúp RepoMapper có thể tái sử dụng logic của UserMapper.
    public RepoMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
    }

    /**
     * Chuyển đổi một đối tượng RepoDto từ API thành một đối tượng domain ReposDataEntry.
     * @param dto Đối tượng thô được lấy từ service API.
     * @return Đối tượng model mô tả cùng một kho lưu trữ.
     */
    public ReposDataEntry map(RepoDto dto) {
        // Kiểm tra null để đảm bảo an toàn.
        Objects.requireNonNull(dto, "dto == null");

        // Yêu cầu các trường bắt buộc phải có giá trị.
        String name = requireNonEmpty(dto.getName(), "name");
        String htmlUrl = requireNonEmpty(dto.getHtmlUrl(), "htmlUrl");

        // Bắt đầu tạo đối tượng ReposDataEntry bằng mẫu thiết kế Builder.
        ReposDataEntry.Builder builder = ReposDataEntry.builder(dto.getId(), name, htmlUrl);

        // Chuẩn hóa và gán các giá trị tùy chọn (optional).
        String fullName = normalize(dto.getFullName());
        if (fullName != null) {
            builder.fullName(fullName);
        }

        String description = normalize(dto.getDescription());
        if (description != null) {
            builder.description(description);
        }

        String language = normalize(dto.getLanguage());
        if (language != null) {
            builder.language(language);
        }

        // Đảm bảo các giá trị số (count) là hợp lệ.
        builder.stargazersCount(safeCount(dto.getStargazersCount()));
        builder.forksCount(safeCount(dto.getForksCount()));
        builder.watchersCount(safeCount(dto.getWatchersCount()));
        builder.openIssuesCount(safeCount(dto.getOpenIssuesCount()));

        String defaultBranch = normalize(dto.getDefaultBranch());
        if (defaultBranch != null) {
            builder.defaultBranch(defaultBranch);
        }

        // Gán các giá trị boolean.
        builder.isPrivate(dto.isPrivate());
        builder.isFork(dto.isFork());

        // Phân tích và gán các giá trị ngày tháng.
        Instant createdAt = parseInstant(dto.getCreatedAt());
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }

        Instant updatedAt = parseInstant(dto.getUpdatedAt());
        if (updatedAt != null) {
            builder.updatedAt(updatedAt);
        }

        Instant pushedAt = parseInstant(dto.getPushedAt());
        if (pushedAt != null) {
            builder.pushedAt(pushedAt);
        }

        // **Điểm quan trọng**: Tái sử dụng UserMapper để ánh xạ đối tượng owner.
        // Điều này tuân thủ nguyên tắc DRY (Don't Repeat Yourself).
        UserDto owner = dto.getOwner();
        if (owner != null) {
            builder.owner(userMapper.map(owner));
        }

        // Xây dựng và trả về đối tượng ReposDataEntry cuối cùng.
        return builder.build();
    }

    /**
     * Ánh xạ một danh sách các đối tượng RepoDto sang một danh sách các đối tượng ReposDataEntry.
     */
    public List<ReposDataEntry> mapList(List<RepoDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList(); // Trả về một danh sách rỗng không thể thay đổi.
        }

        List<ReposDataEntry> entries = new ArrayList<>(dtos.size());
        for (RepoDto dto : dtos) {
            if (dto == null) {
                continue; // Bỏ qua các phần tử null trong danh sách.
            }
            entries.add(map(dto)); // Tái sử dụng phương thức map cho từng đối tượng.
        }
        // Trả về một danh sách không thể thay đổi (unmodifiable) để đảm bảo tính bất biến.
        return Collections.unmodifiableList(entries);
    }

    // --- CÁC PHƯƠNG THỨC TRỢ GIÚP (tương tự UserMapper) ---

    private String requireNonEmpty(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int safeCount(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}