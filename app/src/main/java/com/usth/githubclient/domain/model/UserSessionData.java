package com.usth.githubclient.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Biểu diễn ở tầng domain của phiên đăng nhập người dùng đã được xác thực.
 */
public final class UserSessionData {

    // Tên người dùng.
    private final String username;
    // Access token để xác thực với API.
    private final String accessToken;
    // Loại token (ví dụ: "Bearer").
    private final String tokenType;
    // Thời điểm token hết hạn (nếu có).
    private final Instant accessTokenExpiration;
    // Thông tin hồ sơ chi tiết của người dùng.
    private final GitHubUserProfileDataEntry userProfile;
    // Danh sách các kho của người dùng.
    private final List<ReposDataEntry> repositories;
    // Thời điểm đồng bộ dữ liệu lần cuối.
    private final Instant lastSyncedAt;

    // Constructor riêng tư, chỉ được gọi bởi Builder.
    private UserSessionData(Builder builder) {
        this.username = builder.username;
        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.accessTokenExpiration = builder.accessTokenExpiration;
        this.userProfile = builder.userProfile;
        // Sao chép danh sách để đảm bảo tính bất biến.
        this.repositories = List.copyOf(builder.repositories);
        this.lastSyncedAt = builder.lastSyncedAt;
    }

    // Phương thức tĩnh để bắt đầu xây dựng một phiên mới.
    public static Builder builder(String username, String accessToken) {
        return new Builder(username, accessToken);
    }

    // Tạo một Builder mới từ một phiên đã có.
    public Builder toBuilder() {
        return new Builder(this);
    }

    // --- Các phương thức Getter ---
    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Optional<String> getTokenType() {
        return Optional.ofNullable(tokenType);
    }

    public Optional<Instant> getAccessTokenExpiration() {
        return Optional.ofNullable(accessTokenExpiration);
    }

    public Optional<GitHubUserProfileDataEntry> getUserProfile() {
        return Optional.ofNullable(userProfile);
    }

    public List<ReposDataEntry> getRepositories() {
        return repositories;
    }

    public Optional<Instant> getLastSyncedAt() {
        return Optional.ofNullable(lastSyncedAt);
    }

    /**
     * Trả về {@code true} nếu có thời gian hết hạn của token và
     * thời điểm được cung cấp là bằng hoặc sau thời điểm hết hạn đó.
     */
    public boolean isTokenExpired(Instant moment) {
        Objects.requireNonNull(moment, "moment == null");
        return accessTokenExpiration != null && !moment.isBefore(accessTokenExpiration);
    }

    // Kiểm tra xem token có hợp lệ tại một thời điểm nhất định hay không.
    public boolean hasValidToken(Instant moment) {
        return !accessToken.isEmpty() && !isTokenExpired(moment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSessionData)) {
            return false;
        }
        UserSessionData that = (UserSessionData) o;
        return Objects.equals(username, that.username)
                && Objects.equals(accessToken, that.accessToken)
                && Objects.equals(tokenType, that.tokenType)
                && Objects.equals(accessTokenExpiration, that.accessTokenExpiration)
                && Objects.equals(userProfile, that.userProfile)
                && Objects.equals(repositories, that.repositories)
                && Objects.equals(lastSyncedAt, that.lastSyncedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                username,
                accessToken,
                tokenType,
                accessTokenExpiration,
                userProfile,
                repositories,
                lastSyncedAt
        );
    }

    @Override
    public String toString() {
        // Access token được che đi để bảo mật khi ghi log.
        return new StringJoiner(", ", UserSessionData.class.getSimpleName() + "[", "]")
                .add("username='" + username + '\'')
                .add("accessToken='<redacted>'")
                .add("tokenType='" + tokenType + '\'')
                .add("accessTokenExpiration=" + accessTokenExpiration)
                .add("userProfile=" + userProfile)
                .add("repositories=" + repositories)
                .add("lastSyncedAt=" + lastSyncedAt)
                .toString();
    }

    /**
     * Lớp Builder để xây dựng đối tượng UserSessionData.
     */
    public static final class Builder {

        private final String username;
        private final String accessToken;
        private String tokenType;
        private Instant accessTokenExpiration;
        private GitHubUserProfileDataEntry userProfile;
        private List<ReposDataEntry> repositories = Collections.emptyList();
        private Instant lastSyncedAt;

        // Constructor với các trường bắt buộc.
        private Builder(String username, String accessToken) {
            this.username = Objects.requireNonNull(username, "username == null");
            this.accessToken = Objects.requireNonNull(accessToken, "accessToken == null");
        }

        // Constructor để sao chép từ một đối tượng đã có.
        private Builder(UserSessionData sessionData) {
            this.username = sessionData.username;
            this.accessToken = sessionData.accessToken;
            this.tokenType = sessionData.tokenType;
            this.accessTokenExpiration = sessionData.accessTokenExpiration;
            this.userProfile = sessionData.userProfile;
            this.repositories = sessionData.repositories;
            this.lastSyncedAt = sessionData.lastSyncedAt;
        }

        // Các phương thức để thiết lập giá trị.
        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder accessTokenExpiration(Instant accessTokenExpiration) {
            this.accessTokenExpiration = accessTokenExpiration;
            return this;
        }

        public Builder userProfile(GitHubUserProfileDataEntry userProfile) {
            this.userProfile = userProfile;
            return this;
        }

        public Builder repositories(List<ReposDataEntry> repositories) {
            this.repositories = repositories == null ? Collections.emptyList() : repositories;
            return this;
        }

        public Builder lastSyncedAt(Instant lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
            return this;
        }

        // Phương thức để xây dựng đối tượng UserSessionData cuối cùng.
        public UserSessionData build() {
            if (repositories == null) {
                repositories = Collections.emptyList();
            }
            return new UserSessionData(this);
        }
    }
}