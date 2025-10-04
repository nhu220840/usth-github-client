package com.usth.githubclient.data.repository;

import androidx.annotation.NonNull;

import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;

import java.util.List;
import retrofit2.Call;

/**
 * Implementation of RepoRepository that fetches data from the remote GitHub API.
 */
public class RepoRepositoryImpl implements RepoRepository {

    private final ApiClient apiClient;

    public RepoRepositoryImpl(@NonNull ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    // Dependency Injection
    private GithubApiService apiService() {
        return apiClient.createService(GithubApiService.class);
    }

    @Override
    public Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page) {
        // The "type" parameter is preserved for compatibility even though the API expects a sort value.
        return apiService().getUserRepositories(username, type, perPage, String.valueOf(page));
    }

    @Override
    public Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage) {
        return getAuthenticatedRepositories(page, perPage, null);
    }

    @Override
    public Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort) {
        GithubApiService service = apiService();
        if (sort == null) {
            return service.getAuthenticatedRepositories(perPage, page, null);
        }
        return service.getAuthenticatedRepositories(perPage, page, sort);
    }

    @Override
    public Call<RepoDto> getRepository(String owner, String repoName) {
        return apiService().getRepository(owner, repoName);
    }

    @Override
    public Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage) {
        return apiService().searchRepos(query, page, perPage);
    }
}
