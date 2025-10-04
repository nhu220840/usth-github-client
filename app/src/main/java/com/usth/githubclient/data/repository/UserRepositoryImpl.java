package com.usth.githubclient.data.repository;

import androidx.annotation.NonNull;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;

import java.util.List;

import retrofit2.Call;
/**
 * Lớp triển khai của UserRepository, chịu trách nhiệm lấy dữ liệu từ API GitHub từ xa.
 */
public class UserRepositoryImpl implements UserRepository {

    // Thể hiện của ApiClient để tạo ra service gọi API.
    private final ApiClient apiClient;

    /**
     * Constructor sử dụng kỹ thuật Dependency Injection.
     * ApiClient được cung cấp từ bên ngoài, giúp cho việc kiểm thử (testing) dễ dàng hơn
     * bằng cách cho phép thay thế ApiClient thật bằng một đối tượng giả (mock).
     */
    public UserRepositoryImpl(@NonNull ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Phương thức trợ giúp riêng tư để tạo ra một thể hiện của GithubApiService.
    private GithubApiService apiService() {
        return apiClient.createService(GithubApiService.class);
    }

    @Override
    public Call<UserDto> getUser(String username) {
        // Chuyển tiếp (delegate) cuộc gọi mạng đến thể hiện ApiService được tạo từ ApiClient.
        return apiService().getUser(username);
    }

    @Override
    public Call<List<UserDto>> getFollowers(String username, int perPage, int page) {
        // Chuyển tiếp cuộc gọi đến phương thức tương ứng trong apiService.
        return apiService().getFollowers(username, perPage, page);
    }

    @Override
    public Call<List<UserDto>> getFollowing(String username, int perPage, int page) {
        // Chuyển tiếp cuộc gọi đến phương thức tương ứng trong apiService.
        return apiService().getFollowing(username, perPage, page);
    }

    @Override
    public Call<UserDto> authenticate() {
        // Chuyển tiếp cuộc gọi đến phương thức tương ứng trong apiService.
        return apiService().authenticate();
    }

    @Override
    public Call<SearchUsersResponseDto> searchUsers(String query, int page, int perPage) {
        // Chuyển tiếp cuộc gọi đến phương thức tương ứng trong apiService.
        return apiService().searchUsers(query, page, perPage);
    }
}