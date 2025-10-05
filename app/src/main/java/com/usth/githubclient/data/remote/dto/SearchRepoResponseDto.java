package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lightweight DTO mirroring the GitHub search repositories response payload.
 */
public final class SearchRepoResponseDto {

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incompleteResults;

    // This annotation is necessary for Gson to map correctly.
    @SerializedName("items")
    private List<RepoDto> items;

    // --- Getters ---

    public int getTotalCount() {
        return totalCount;
    }

    public boolean isIncompleteResults() {
        return incompleteResults;
    }

    public List<RepoDto> getItems() {
        return items;
    }
}