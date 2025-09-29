package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO representing the payload returned by the GitHub search repositories endpoint.
 */
public final class SearchRepoResponseDto {

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incompleteResults;

    // Quan trọng: Annotation này là cần thiết để Gson ánh xạ đúng
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
