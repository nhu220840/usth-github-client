package com.usth.githubclient.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Immutable domain representation of a GitHub user profile.
 */
public final class GitHubUserProfileDataEntry {

    private final long id;
    private final String username;
    private final String displayName;
    private final String avatarUrl;
    private final String bio;
    private final String company;
    private final String blogUrl;
    private final String email;
    private final String location;
    private final int publicReposCount;
    private final int followersCount;
    private final int followingCount;
    private final String profileUrl;
    private final Instant createdAt;
    private final Instant updatedAt;

    private GitHubUserProfileDataEntry(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.displayName = builder.displayName;
        this.avatarUrl = builder.avatarUrl;
        this.bio = builder.bio;
        this.company = builder.company;
        this.blogUrl = builder.blogUrl;
        this.email = builder.email;
        this.location = builder.location;
        this.publicReposCount = builder.publicReposCount;
        this.followersCount = builder.followersCount;
        this.followingCount = builder.followingCount;
        this.profileUrl = builder.profileUrl;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    /**
     * Creates a new builder with the mandatory identifier and username populated.
     * @param id The stable numeric identifier of the GitHub account.
     * @param username The login of the account.
     * @return A builder instance.
     */
    public static Builder builder(long id, String username) {
        return new Builder(id, username);
    }

    /**
     * Creates a builder pre-populated with the data from the current instance.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters for all fields, using Optional for nullable fields.
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public Optional<String> getAvatarUrl() {
        return Optional.ofNullable(avatarUrl);
    }

    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    public Optional<String> getCompany() {
        return Optional.ofNullable(company);
    }

    public Optional<String> getBlogUrl() {
        return Optional.ofNullable(blogUrl);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public int getPublicReposCount() {
        return publicReposCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public Optional<String> getProfileUrl() {
        return Optional.ofNullable(profileUrl);
    }

    public Optional<Instant> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<Instant> getUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitHubUserProfileDataEntry)) {
            return false;
        }
        GitHubUserProfileDataEntry that = (GitHubUserProfileDataEntry) o;
        return id == that.id
                && publicReposCount == that.publicReposCount
                && followersCount == that.followersCount
                && followingCount == that.followingCount
                && Objects.equals(username, that.username)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(avatarUrl, that.avatarUrl)
                && Objects.equals(bio, that.bio)
                && Objects.equals(company, that.company)
                && Objects.equals(blogUrl, that.blogUrl)
                && Objects.equals(email, that.email)
                && Objects.equals(location, that.location)
                && Objects.equals(profileUrl, that.profileUrl)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                username,
                displayName,
                avatarUrl,
                bio,
                company,
                blogUrl,
                email,
                location,
                publicReposCount,
                followersCount,
                followingCount,
                profileUrl,
                createdAt,
                updatedAt
        );
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GitHubUserProfileDataEntry.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("username='" + username + '\'')
                .add("displayName='" + displayName + '\'')
                .add("avatarUrl='" + avatarUrl + '\'')
                .add("bio='" + bio + '\'')
                .add("company='" + company + '\'')
                .add("blogUrl='" + blogUrl + '\'')
                .add("email='" + email + '\'')
                .add("location='" + location + '\'')
                .add("publicReposCount=" + publicReposCount)
                .add("followersCount=" + followersCount)
                .add("followingCount=" + followingCount)
                .add("profileUrl='" + profileUrl + '\'')
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }

    /** Builder used to configure and create immutable GitHubUserProfileDataEntry values. */
    public static final class Builder {

        private final long id;
        private final String username;
        private String displayName;
        private String avatarUrl;
        private String bio;
        private String company;
        private String blogUrl;
        private String email;
        private String location;
        private int publicReposCount;
        private int followersCount;
        private int followingCount;
        private String profileUrl;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder(long id, String username) {
            if (id < 0L) {
                throw new IllegalArgumentException("id must be greater than or equal to 0");
            }
            this.id = id;
            this.username = Objects.requireNonNull(username, "username == null");
        }

        private Builder(GitHubUserProfileDataEntry entry) {
            this.id = entry.id;
            this.username = entry.username;
            this.displayName = entry.displayName;
            this.avatarUrl = entry.avatarUrl;
            this.bio = entry.bio;
            this.company = entry.company;
            this.blogUrl = entry.blogUrl;
            this.email = entry.email;
            this.location = entry.location;
            this.publicReposCount = entry.publicReposCount;
            this.followersCount = entry.followersCount;
            this.followingCount = entry.followingCount;
            this.profileUrl = entry.profileUrl;
            this.createdAt = entry.createdAt;
            this.updatedAt = entry.updatedAt;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder blogUrl(String blogUrl) {
            this.blogUrl = blogUrl;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder publicReposCount(int publicReposCount) {
            if (publicReposCount < 0) {
                throw new IllegalArgumentException("publicReposCount must be >= 0");
            }
            this.publicReposCount = publicReposCount;
            return this;
        }

        public Builder followersCount(int followersCount) {
            if (followersCount < 0) {
                throw new IllegalArgumentException("followersCount must be >= 0");
            }
            this.followersCount = followersCount;
            return this;
        }

        public Builder followingCount(int followingCount) {
            if (followingCount < 0) {
                throw new IllegalArgumentException("followingCount must be >= 0");
            }
            this.followingCount = followingCount;
            return this;
        }

        public Builder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public GitHubUserProfileDataEntry build() {
            return new GitHubUserProfileDataEntry(this);
        }
    }
}