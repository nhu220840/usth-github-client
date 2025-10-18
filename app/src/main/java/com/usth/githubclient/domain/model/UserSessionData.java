package com.usth.githubclient.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Domain level representation of the authenticated user session.
 */
public final class UserSessionData {

    private final String username;
    private final String accessToken;
    private final String tokenType;
    private final Instant accessTokenExpiration;
    private final GitHubUserProfileDataEntry userProfile;
    private final List<ReposDataEntry> repositories;
    private final Instant lastSyncedAt;

    private UserSessionData(Builder builder) {
        this.username = builder.username;
        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.accessTokenExpiration = builder.accessTokenExpiration;
        this.userProfile = builder.userProfile;
        this.repositories = List.copyOf(builder.repositories);
        this.lastSyncedAt = builder.lastSyncedAt;
    }

    // Getters for all fields, using Optional for nullable fields.
    public String getUsername() {
        return username;
    }

    public List<ReposDataEntry> getRepositories() {
        return repositories;
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
     * Builder for creating UserSessionData instances.
     */
    public static final class Builder {

        private final String username;
        private final String accessToken;
        private String tokenType;
        private Instant accessTokenExpiration;
        private GitHubUserProfileDataEntry userProfile;
        private List<ReposDataEntry> repositories = Collections.emptyList();
        private Instant lastSyncedAt;

        private Builder(String username, String accessToken) {
            this.username = Objects.requireNonNull(username, "username == null");
            this.accessToken = Objects.requireNonNull(accessToken, "accessToken == null");
        }

        private Builder(UserSessionData sessionData) {
            this.username = sessionData.username;
            this.accessToken = sessionData.accessToken;
            this.tokenType = sessionData.tokenType;
            this.accessTokenExpiration = sessionData.accessTokenExpiration;
            this.userProfile = sessionData.userProfile;
            this.repositories = sessionData.repositories;
            this.lastSyncedAt = sessionData.lastSyncedAt;
        }

        public UserSessionData build() {
            if (repositories == null) {
                repositories = Collections.emptyList();
            }
            return new UserSessionData(this);
        }
    }
}