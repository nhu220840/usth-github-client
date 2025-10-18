package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lightweight DTO mirroring the GitHub search repositories response payload.
 */
public final class SearchRepoResponseDto {
    // This annotation is necessary for Gson to map correctly.
    @SerializedName("items")
    private List<RepoDto> items;

    // --- Getters ---
    public List<RepoDto> getItems() {
        return items;
    }
}