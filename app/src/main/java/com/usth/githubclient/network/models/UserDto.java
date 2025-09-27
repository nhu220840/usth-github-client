package com.usth.githubclient.network.models;
import com.google.gson.annotations.SerializedName;
public class UserDto {
    public long id;
    public String login;
    @SerializedName("avatar_url")
    public String avatarUrl;
}