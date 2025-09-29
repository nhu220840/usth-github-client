package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface for data operations related to Repositories.
 */
public interface RepoRepository {

    Call<List<RepoDto>> getUserRepositories(String username, int type, int perPage, int page);

    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage);

    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort);

    Call<RepoDto> getRepository(String owner, String repoName);

    Call<SearchRepoResponseDto> searchRepos(String query, int page, int perPage);
}
