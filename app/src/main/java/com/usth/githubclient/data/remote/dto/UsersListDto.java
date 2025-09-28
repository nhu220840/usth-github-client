package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data Transfer Object for a list of GitHub Users, typically from a search result.
 */public class UsersListDto {

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incompleteResults;

    @SerializedName("items")
    private List<UserDto> items;

    // --- Getters ---

    public int getTotalCount() {
        return totalCount;
    }

    public boolean isIncompleteResults() {
        return incompleteResults;
    }

    public List<UserDto> getItems() {
        return items;
    }
}
