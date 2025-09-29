package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface for data operations related to Users.
 * This abstracts the data source (network or local) from the rest of the app.
 */
interface UserRepository {

    Call<UserDto> getUser(String username);

    Call<List<UserDto>> getFollowers(String username, int perPage, int page);

    Call<List<UserDto>> getFollowing(String username, int perPage, int page);

    Call<UserDto> authenticate();

    Call<SearchUsersResponseDto> searchUsers(String query, int page, int perPage);
}
