package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO representing the payload returned by the GitHub search users endpoint.
 */
public final class SearchUsersResponseDto {

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incompleteResults;

    private List<UserDto> items;

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