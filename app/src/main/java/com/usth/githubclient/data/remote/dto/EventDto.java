package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class EventDto {
    @SerializedName("type")
    private String type;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}