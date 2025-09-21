package com.usth.githubclient.domain.mapper;

import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Maps {@link UserDto} values from the data layer into domain models.
 */
public final class UserMapper {

    public GitHubUserProfileDataEntry map(UserDto dto) {
        Objects.requireNonNull(dto, "dto == null");

        String username = normalize(dto.getLogin());
        if (username == null) {
            throw new IllegalArgumentException("User login cannot be null or blank");
        }

        return new GitHubUserProfileDataEntry(
                dto.getId(),
                username,
                normalize(dto.getName()),
                normalize(dto.getAvatarUrl()),
                normalize(dto.getBio()),
                normalize(dto.getCompany()),
                normalize(dto.getBlog()),
                normalize(dto.getEmail()),
                normalize(dto.getLocation()),
                safeCount(dto.getPublicRepos()),
                safeCount(dto.getFollowers()),
                safeCount(dto.getFollowing()),
                normalize(dto.getHtmlUrl()),
                normalize(dto.getCreatedAt()),
                normalize(dto.getUpdatedAt())
        );
    }

    public List<GitHubUserProfileDataEntry> mapList(List<UserDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<GitHubUserProfileDataEntry> result = new ArrayList<>(dtos.size());
        for (UserDto dto : dtos) {
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

