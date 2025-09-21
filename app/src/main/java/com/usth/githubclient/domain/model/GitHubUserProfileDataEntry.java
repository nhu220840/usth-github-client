package com.usth.githubclient.domain.model;

import java.util.Objects;

/**
 * Domain contract that represents a GitHub user profile.
 *
 * <p>The model is intentionally lightweight so it can be shared across layers without leaking
 * networking concerns. Optional fields are represented with nullable references.</p>
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
    private final int publicRepos;
    private final int followers;
    private final int following;
    private final String profileUrl;
    private final String createdAt;
    private final String updatedAt;

    public GitHubUserProfileDataEntry(
            long id,
            String username,
            String displayName,
            String avatarUrl,
            String bio,
            String company,
            String blogUrl,
            String email,
            String location,
            int publicRepos,
            int followers,
            int following,
            String profileUrl,
            String createdAt,
            String updatedAt
    ) {
        if (id < 0L) {
            throw new IllegalArgumentException("id must be greater than or equal to 0");
        }
        this.id = id;
        this.username = Objects.requireNonNull(username, "username == null");
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.company = company;
        this.blogUrl = blogUrl;
        this.email = email;
        this.location = location;
        this.publicRepos = Math.max(publicRepos, 0);
        this.followers = Math.max(followers, 0);
        this.following = Math.max(following, 0);
        this.profileUrl = profileUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public String getCompany() {
        return company;
    }

    public String getBlogUrl() {
        return blogUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

