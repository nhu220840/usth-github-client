package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;

/**
 * Implementation of UserRepository that fetches data from the remote GitHub API.
 */
public class UserRepositoryImpl implements UserRepository {

    private final GithubApiService apiService;

    // Dependency Injection: The ApiService is provided via the constructor.
    // Điều này giúp việc kiểm thử (testing) dễ dàng hơn.
    public UserRepositoryImpl(GithubApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Call<UserDto> getUser(String username) {
        // Delegate the network call to the injected ApiService.
        return apiService.getUser(username);
    }

    @Override
    public Call<List<UserDto>> getFollowers(String username, int perPage, int page) {
        return apiService.getFollowers(username, perPage, page);
    }

    @Override
    public Call<List<UserDto>> getFollowing(String username, int perPage, int page) {
        return apiService.getFollowing(username, perPage, page);
    }

    @Override
    public Call<UserDto> authenticate() {
        return apiService.authenticate();
    }

    @Override
    public Call<SearchUsersResponseDto> searchUsers(String query, int page, int perPage) {
        return apiService.searchUsers(query, page, perPage);
    }
}
