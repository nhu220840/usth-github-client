package com.usth.githubclient.data.remote;

// Import các lớp DTO (Data Transfer Object) - các lớp Java đại diện cho cấu trúc JSON trả về.
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import com.usth.githubclient.data.remote.dto.SearchUsersResponseDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.remote.dto.UserEmailDto;

import java.util.List;

// Import các thành phần của Retrofit.
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface (giao diện) của Retrofit, định nghĩa các endpoint của GitHub REST API
 * được sử dụng trong ứng dụng. Mỗi phương thức trong interface này tương ứng với một
 * cuộc gọi API. Retrofit sẽ tự động tạo ra một lớp triển khai cho interface này.
 */
public interface GithubApiService {

    // --- Endpoints liên quan đến User ---

    /**
     * Lấy thông tin chi tiết của một người dùng.
     * @param username Tên người dùng cần lấy thông tin.
     */
    @GET("users/{username}")
    Call<UserDto> getUser(@Path("username") String username);

    /**
     * Lấy danh sách những người theo dõi (followers) của một người dùng.
     * @param username Tên người dùng.
     * @param perPage Số lượng kết quả mỗi trang.
     * @param page Trang kết quả cần lấy.
     */
    @GET("users/{username}/followers")
    Call<List<UserDto>> getFollowers(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    /**
     * Lấy danh sách những người mà một người dùng đang theo dõi (following).
     */
    @GET("users/{username}/following")
    Call<List<UserDto>> getFollowing(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page
    );

    /**
     * Lấy thông tin của người dùng đã được xác thực (dựa trên token được cung cấp).
     */
    @GET("user")
    Call<UserDto> authenticate();

    /**
     * Lấy danh sách email của người dùng đã được xác thực. Yêu cầu scope 'user:email'.
     */
    @GET("user/emails")
    Call<List<UserEmailDto>> getUserEmails();


    // --- Endpoints liên quan đến Repository ---

    /**
     * Lấy danh sách kho lưu trữ công khai của một người dùng.
     * @param sort Tiêu chí sắp xếp (ví dụ: "created", "updated", "pushed", "full_name").
     */
    @GET("users/{username}/repos")
    Call<List<RepoDto>> getUserRepositories(
            @Path("username") String username,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );

    /**
     * Lấy thông tin chi tiết của một kho lưu trữ cụ thể.
     * @param owner Tên chủ sở hữu.
     * @param repo Tên kho lưu trữ.
     */
    @GET("repos/{owner}/{repo}")
    Call<RepoDto> getRepository(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    /**
     * Lấy danh sách các kho lưu trữ của người dùng đã xác thực.
     */
    @GET("user/repos")
    Call<List<RepoDto>> getAuthenticatedRepositories(
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sort") String sort
    );


    // --- Endpoints liên quan đến Tìm kiếm ---

    /**
     * Tìm kiếm người dùng trên GitHub.
     * @param query Từ khóa tìm kiếm.
     */
    @GET("search/users")
    Call<SearchUsersResponseDto> searchUsers(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    /**
     * Tìm kiếm kho lưu trữ trên GitHub.
     */
    @GET("search/repositories")
    Call<SearchRepoResponseDto> searchRepos(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );


    // --- Endpoints liên quan đến Star/Unstar ---

    /**
     * Kiểm tra xem một kho lưu trữ đã được người dùng star hay chưa.
     * API sẽ trả về mã 204 No Content nếu đã star, và 404 Not Found nếu chưa.
     */
    @GET("user/starred/{owner}/{repo}")
    Call<Void> isRepoStarred(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    /**
     * Đánh dấu "star" (yêu thích) một kho lưu trữ.
     */
    @PUT("user/starred/{owner}/{repo}")
    Call<Void> starRepo(
            @Path("owner") String owner,
            @Path("repo") String repo
    );

    /**
     * Bỏ "star" một kho lưu trữ.
     */
    @DELETE("user/starred/{owner}/{repo}")
    Call<Void> unstarRepo(
            @Path("owner") String owner,
            @Path("repo") String repo
    );
}