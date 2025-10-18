package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.SearchRepoResponseDto;
import java.util.List;
import retrofit2.Call;

/**
 * Interface for data operations related to Repositories.
 */
public interface RepoRepository {

    // Get the authenticated user's repositories with a sort option.
    Call<List<RepoDto>> getAuthenticatedRepositories(int page, int perPage, String sort);
}