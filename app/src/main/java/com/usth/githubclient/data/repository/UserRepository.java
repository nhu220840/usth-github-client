package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface (giao diện) cho các hoạt động dữ liệu liên quan đến Người dùng.
 * Lớp này trừu tượng hóa nguồn dữ liệu (mạng hoặc cục bộ) khỏi phần còn lại của ứng dụng.
 * Nó hoạt động như một "hợp đồng", bất kỳ lớp nào implement interface này
 * đều phải cung cấp các phương thức đã được định nghĩa ở đây.
 */
public interface UserRepository {

    /**
     * Lấy thông tin chi tiết của một người dùng dựa trên username.
     * @return một đối tượng Call của Retrofit, bao bọc một UserDto.
     */
    Call<UserDto> getUser(String username);

    /**
     * Lấy danh sách những người theo dõi (followers) của một người dùng.
     */
    Call<List<UserDto>> getFollowers(String username, int perPage, int page);

    /**
     * Lấy danh sách những người mà một người dùng đang theo dõi (following).
     */
    Call<List<UserDto>> getFollowing(String username, int perPage, int page);

    /**
     * Lấy thông tin của người dùng đã được xác thực (thông qua token).
     * Tương ứng với endpoint /user của GitHub API.
     */
    Call<UserDto> authenticate();

    /**
     * Tìm kiếm người dùng dựa trên một query.
     */
    Call<SearchUsersResponseDto> searchUsers(String query, int page, int perPage);
}