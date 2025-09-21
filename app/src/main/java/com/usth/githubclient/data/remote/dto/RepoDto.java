package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Raw representation of a GitHub repository as returned by the REST API.
 */
public final class RepoDto {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("description")
    private String description;

    @SerializedName("language")
    private String language;

    @SerializedName("stargazers_count")
    private int stargazersCount;

    @SerializedName("forks_count")
    private int forksCount;

    @SerializedName("watchers_count")
    private int watchersCount;

    @SerializedName("open_issues_count")
    private int openIssuesCount;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("default_branch")
    private String defaultBranch;

    @SerializedName("private")
    private boolean isPrivate;

    @SerializedName("fork")
    private boolean isFork;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("pushed_at")
    private String pushedAt;

    @SerializedName("owner")
    private UserDto owner;

    /** Required by Gson. */
    public RepoDto() {
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

    public UserDto getOwner() {
        return owner;
    }
}
