package com.usth.githubclient.data.repository;

import androidx.annotation.NonNull;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;

import java.util.List;
import retrofit2.Call;

/**
 * Lớp triển khai của RepoRepository, chịu trách nhiệm lấy dữ liệu từ API GitHub từ xa.
 */
public class RepoRepositoryImpl implements RepoRepository {

    // Thể hiện của ApiClient để tạo ra service gọi API.
    private final ApiClient apiClient;

    /**
     * Constructor sử dụng kỹ thuật Dependency Injection.
     * ApiClient được cung cấp từ bên ngoài, giúp cho việc kiểm thử (testing) dễ dàng hơn
     * bằng cách cho phép thay thế ApiClient thật bằng một đối tượng giả (mock).
     */
    public RepoRepositoryImpl(@NonNull ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // Phương thức trợ giúp riêng tư để tạo ra một thể hiện của GithubApiService.
    private GithubApiService apiService() {
        return apiClient.createService(GithubApiService.class);
    }

    @Override
    public Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page) {
        // Tham số "type" được giữ lại để tương thích, mặc dù API mong đợi một giá trị sắp xếp.
        // Chuyển tiếp lời gọi đến phương thức tương ứng trong apiService.
        // Chuyển đổi page thành String vì API yêu cầu như vậy.
        return apiService().getUserRepositories(username, perPage, page, String.valueOf(type));
    }

    @Override
    public Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage) {
        // Gọi phiên bản quá tải (overloaded) của phương thức với giá trị sắp xếp là null.
        return getAuthenticatedRepositories(page, perPage, null);
    }

    @Override
    public Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort) {
        GithubApiService service = apiService();
        // Kiểm tra xem có cần sắp xếp hay không.
        if (sort == null) {
            return service.getAuthenticatedRepositories(perPage, page, null);
        }
        return service.getAuthenticatedRepositories(perPage, page, sort);
    }

    @Override
    public Call<RepoDto> getRepository(String owner, String repoName) {
        // Chuyển tiếp lời gọi đến apiService.
        return apiService().getRepository(owner, repoName);
    }

    @Override
    public Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage) {
        // Chuyển tiếp lời gọi đến apiService.
        return apiService().searchRepos(query, page, perPage);
    }
}