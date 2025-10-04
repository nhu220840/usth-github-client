package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Biểu diễn của một mục email được trả về bởi API REST của GitHub.
 */
public final class UserEmailDto {

    @SerializedName("email")
    private String email;

    @SerializedName("primary")
    private boolean primary;

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("visibility")
    private String visibility;

    /** Cần thiết bởi Gson. */
    public UserEmailDto() {
    }

    public String getEmail() {
        return email;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getVisibility() {
        return visibility;
    }
}