package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO representing the payload returned by the GitHub search users endpoint.
 */
public final class SearchUsersResponseDto {
    private List<UserDto> items;

    public List<UserDto> getItems() {
        return items;
    }
}