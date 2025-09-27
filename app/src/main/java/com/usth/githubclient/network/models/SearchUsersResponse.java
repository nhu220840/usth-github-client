package com.usth.githubclient.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchUsersResponse {
    @SerializedName("total_count") public int totalCount;
    @SerializedName("incomplete_results") public boolean incompleteResults;
    public List<UserDto> items; // mỗi item có login, avatar_url (đã trùng UserDto)
}
