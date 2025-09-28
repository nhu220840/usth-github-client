package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;

import java.util.List;
import retrofit2.Call;

/**
 * Implementation of RepoRepository that fetches data from the remote GitHub API.
 */
public abstract class RepoRepositoryImpl implements RepoRepository {

    private final GithubApiService apiService;

    // Dependency Injection
    public RepoRepositoryImpl(GithubApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page) {
        return apiService.getUserRepositories(username, type, perPage, String.valueOf(page));
    }

    @Override
    public Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort) {
        return apiService.getAuthenticatedRepositories(page, perPage, sort);
    }

    @Override
    public Call<RepoDto> getRepository(String owner, String repoName) {
        return apiService.getRepository(owner, repoName);
    }

    @Override
    public Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage) {
        return apiService.searchRepos(query, page, perPage);
    }
}
