package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface (giao diện) cho các hoạt động dữ liệu liên quan đến Kho (Repositories).
 * Lớp này trừu tượng hóa nguồn dữ liệu, định nghĩa các phương thức cần thiết
 * để lấy dữ liệu về kho từ API.
 */
public interface RepoRepository {

    /**
     * Lấy danh sách các kho của một người dùng cụ thể.
     */
    Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page);

    /**
     * Lấy danh sách các kho của người dùng đã được xác thực (đã đăng nhập).
     */
    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage);

    /**
     * Lấy danh sách các kho của người dùng đã được xác thực, có thêm tùy chọn sắp xếp.
     * @param sort Tiêu chí sắp xếp (ví dụ: "updated", "pushed", "full_name").
     */
    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort);

    /**
     * Lấy thông tin chi tiết của một kho cụ thể.
     * @param owner Tên chủ sở hữu kho.
     * @param repoName Tên của kho.
     */
    Call<RepoDto> getRepository(String owner, String repoName);

    /**
     * Tìm kiếm các kho công khai dựa trên một chuỗi truy vấn.
     */
    Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage);
}