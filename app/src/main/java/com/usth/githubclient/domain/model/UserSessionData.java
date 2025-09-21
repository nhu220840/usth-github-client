package com.usth.githubclient.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Simple in-memory representation of the authenticated session.
 */
public final class UserSessionData {

    private final String username;
    private final String accessToken;
    private final String tokenType;
    private final String accessTokenExpiration;
    private final String lastSyncedAt;
    private final GitHubUserProfileDataEntry userProfile;
    private final List<ReposDataEntry> repositories;

    public UserSessionData(
            String username,
            String accessToken,
            String tokenType,
            String accessTokenExpiration,
            String lastSyncedAt,
            GitHubUserProfileDataEntry userProfile,
            List<ReposDataEntry> repositories
    ) {
        this.username = Objects.requireNonNull(username, "username == null");
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken == null");
        this.tokenType = tokenType;
        this.accessTokenExpiration = accessTokenExpiration;
        this.lastSyncedAt = lastSyncedAt;
        this.userProfile = userProfile;
        if (repositories == null || repositories.isEmpty()) {
            this.repositories = Collections.emptyList();
        } else {
            this.repositories = Collections.unmodifiableList(new ArrayList<>(repositories));
        }
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public String getLastSyncedAt() {
        return lastSyncedAt;
    }

    public GitHubUserProfileDataEntry getUserProfile() {
        return userProfile;
    }

    public List<ReposDataEntry> getRepositories() {
        return repositories;
    }
}

