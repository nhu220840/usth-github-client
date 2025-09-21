package com.usth.githubclient.domain.mapper;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import com.usth.githubclient.domain.model.ReposDataEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Maps repository DTOs into domain models.
 */
public final class RepoMapper {

    private final UserMapper userMapper;

    public RepoMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
    }

    public ReposDataEntry map(RepoDto dto) {
        Objects.requireNonNull(dto, "dto == null");

        String name = normalize(dto.getName());
        String htmlUrl = normalize(dto.getHtmlUrl());
        if (name == null || htmlUrl == null) {
            throw new IllegalArgumentException("Repository name and htmlUrl are required");
        }

        GitHubUserProfileDataEntry owner = null;
        UserDto ownerDto = dto.getOwner();
        if (ownerDto != null) {
            owner = userMapper.map(ownerDto);
        }

        return new ReposDataEntry(
                dto.getId(),
                name,
                normalize(dto.getFullName()),
                normalize(dto.getDescription()),
                normalize(dto.getLanguage()),
                safeCount(dto.getStargazersCount()),
                safeCount(dto.getForksCount()),
                safeCount(dto.getWatchersCount()),
                safeCount(dto.getOpenIssuesCount()),
                htmlUrl,
                normalize(dto.getDefaultBranch()),
                dto.isPrivate(),
                dto.isFork(),
                normalize(dto.getCreatedAt()),
                normalize(dto.getUpdatedAt()),
                normalize(dto.getPushedAt()),
                owner
        );
    }

    public List<ReposDataEntry> mapList(List<RepoDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<ReposDataEntry> result = new ArrayList<>(dtos.size());
        for (RepoDto dto : dtos) {
            if (dto == null) {
                continue;
            }
            result.add(map(dto));
        }
        return Collections.unmodifiableList(result);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int safeCount(int count) {
        return Math.max(count, 0);
    }
}

