package com.usth.githubclient.network.models;

import com.google.gson.annotations.SerializedName;

public class UserDetailDto {
    public String login;
    public String name;
    @SerializedName("avatar_url") public String avatarUrl;
}
