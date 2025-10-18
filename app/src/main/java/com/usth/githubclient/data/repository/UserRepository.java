package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface for data operations related to Users.
 * This abstracts the data source (network or local) from the rest of the app.
 */
public interface UserRepository {

    // Get a user's details.
    Call<UserDto> getUser(String username);

    // Get a user's followers.
    Call<List<UserDto>> getFollowers(String username, int perPage, int page);

    // Authenticate the user.
    Call<UserDto> authenticate();

    // Get user events.
    Call<List<EventDto>> getUserEvents(String username, int page, int perPage);
}