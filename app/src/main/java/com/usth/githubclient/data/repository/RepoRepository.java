package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface for data operations related to Repositories.
 */
public interface RepoRepository {

    // Get a user's repositories.
    Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page);

    // Get the authenticated user's repositories.
    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage);

    // Get the authenticated user's repositories with a sort option.
    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort);

    // Get a specific repository.
    Call<RepoDto> getRepository(String owner, String repoName);

    // Search for repositories.
    Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage);
}