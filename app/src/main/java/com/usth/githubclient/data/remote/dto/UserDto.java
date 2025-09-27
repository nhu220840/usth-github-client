package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Raw representation of a GitHub user as returned by the public REST API.
 */
public final class UserDto {

    @SerializedName("id")
    private long id;

    @SerializedName("login")
    private String login;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("bio")
    private String bio;

    @SerializedName("company")
    private String company;

    @SerializedName("blog")
    private String blog;

    @SerializedName("email")
    private String email;

    @SerializedName("location")
    private String location;

    @SerializedName("public_repos")
    private int publicRepos;

    @SerializedName("followers")
    private int followers;

    @SerializedName("following")
    private int following;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    /** Required by Gson. */
    public UserDto() {
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
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

    public String getBlog() {
        return blog;
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

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}