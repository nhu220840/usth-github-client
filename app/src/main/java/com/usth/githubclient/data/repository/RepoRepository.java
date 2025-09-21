package com.usth.githubclient.data.repository;

import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.model.ReposDataEntry;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import retrofit2.Response;

/**
 * Repository encapsulating repository related API calls.
 */
public final class RepoRepository {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PER_PAGE = 30;
    private static final String DEFAULT_SORT = "updated";

    private final GithubApiService apiService;
    private final RepoMapper repoMapper;

    public RepoRepository(GithubApiService apiService, RepoMapper repoMapper) {
        this.apiService = Objects.requireNonNull(apiService, "apiService == null");
        this.repoMapper = Objects.requireNonNull(repoMapper, "repoMapper == null");
    }

    public List<ReposDataEntry> fetchUserRepositories(String username) throws IOException {
        return fetchUserRepositories(username, DEFAULT_PER_PAGE, DEFAULT_PAGE, DEFAULT_SORT);
    }

    public List<ReposDataEntry> fetchUserRepositories(
            String username,
            int perPage,
            int page,
            String sort
    ) throws IOException {
        Response<List<RepoDto>> response =
                apiService.getUserRepositories(username, perPage, page, sort).execute();
        if (response.isSuccessful() && response.body() != null) {
            return repoMapper.mapList(response.body());
        }
        throw buildException("Unable to fetch repositories for " + username, response);
    }

    public List<ReposDataEntry> fetchAuthenticatedRepositories() throws IOException {
        return fetchAuthenticatedRepositories(DEFAULT_PER_PAGE, DEFAULT_PAGE, DEFAULT_SORT);
    }

    public List<ReposDataEntry> fetchAuthenticatedRepositories(int perPage, int page, String sort)
            throws IOException {
        Response<List<RepoDto>> response =
                apiService.getAuthenticatedRepositories(perPage, page, sort).execute();
        if (response.isSuccessful() && response.body() != null) {
            return repoMapper.mapList(response.body());
        }
        throw buildException("Unable to fetch repositories for the authenticated user", response);
    }

    public ReposDataEntry fetchRepository(String owner, String name) throws IOException {
        Response<RepoDto> response = apiService.getRepository(owner, name).execute();
        if (response.isSuccessful() && response.body() != null) {
            return repoMapper.map(response.body());
        }
        throw buildException("Unable to fetch repository " + owner + "/" + name, response);
    }

    private IOException buildException(String message, Response<?> response) {
        String errorBody;
        try {
            errorBody = response != null && response.errorBody() != null
                    ? response.errorBody().string()
                    : null;
        } catch (IOException ignored) {
            errorBody = null;
        }
        if (errorBody == null || errorBody.isEmpty()) {
            return new IOException(message);
        }
        return new IOException(message + ": " + errorBody);
    }
}

