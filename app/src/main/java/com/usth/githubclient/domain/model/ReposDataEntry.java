package com.usth.githubclient.domain.model;

import java.util.Objects;

/**
 * Domain representation of a GitHub repository.
 */
public final class ReposDataEntry {

    private final long id;
    private final String name;
    private final String fullName;
    private final String description;
    private final String language;
    private final int stargazersCount;
    private final int forksCount;
    private final int watchersCount;
    private final int openIssuesCount;
    private final String htmlUrl;
    private final String defaultBranch;
    private final boolean isPrivate;
    private final boolean isFork;
    private final String createdAt;
    private final String updatedAt;
    private final String pushedAt;
    private final GitHubUserProfileDataEntry owner;

    public ReposDataEntry(
            long id,
            String name,
            String fullName,
            String description,
            String language,
            int stargazersCount,
            int forksCount,
            int watchersCount,
            int openIssuesCount,
            String htmlUrl,
            String defaultBranch,
            boolean isPrivate,
            boolean isFork,
            String createdAt,
            String updatedAt,
            String pushedAt,
            GitHubUserProfileDataEntry owner
    ) {
        if (id < 0L) {
            throw new IllegalArgumentException("id must be greater than or equal to 0");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name == null");
        this.fullName = fullName;
        this.description = description;
        this.language = language;
        this.stargazersCount = Math.max(stargazersCount, 0);
        this.forksCount = Math.max(forksCount, 0);
        this.watchersCount = Math.max(watchersCount, 0);
        this.openIssuesCount = Math.max(openIssuesCount, 0);
        this.htmlUrl = Objects.requireNonNull(htmlUrl, "htmlUrl == null");
        this.defaultBranch = defaultBranch;
        this.isPrivate = isPrivate;
        this.isFork = isFork;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pushedAt = pushedAt;
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public int getStargazersCount() {
        return stargazersCount;
    }

    public int getForksCount() {
        return forksCount;
    }

    public int getWatchersCount() {
        return watchersCount;
    }

    public int getOpenIssuesCount() {
        return openIssuesCount;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isFork() {
        return isFork;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getPushedAt() {
        return pushedAt;
    }

    public GitHubUserProfileDataEntry getOwner() {
        return owner;
    }
}

