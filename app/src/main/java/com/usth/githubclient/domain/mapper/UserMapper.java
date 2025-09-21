package com.usth.githubclient.domain.mapper;

import android.annotation.SuppressLint;

import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maps {@link UserDto} values coming from the data layer into domain models that the rest of the
 * application can operate on.
 */
public final class UserMapper {

    /**
     * Convert an API user payload to the immutable domain representation.
     *
     * @param dto raw payload fetched from the remote service.
     * @return domain model describing the same user.
     */
    public GitHubUserProfileDataEntry map(UserDto dto) {
        Objects.requireNonNull(dto, "dto == null");

        String username = requireNonEmpty(dto.getLogin(), "login");
        GitHubUserProfileDataEntry.Builder builder =
                GitHubUserProfileDataEntry.builder(dto.getId(), username);

        String displayName = normalize(dto.getName());
        if (displayName != null) {
            builder.displayName(displayName);
        }

        String avatarUrl = normalize(dto.getAvatarUrl());
        if (avatarUrl != null) {
            builder.avatarUrl(avatarUrl);
        }

        String bio = normalize(dto.getBio());
        if (bio != null) {
            builder.bio(bio);
        }

        String company = normalize(dto.getCompany());
        if (company != null) {
            builder.company(company);
        }

        String blog = normalize(dto.getBlog());
        if (blog != null) {
            builder.blogUrl(blog);
        }

        String email = normalize(dto.getEmail());
        if (email != null) {
            builder.email(email);
        }

        String location = normalize(dto.getLocation());
        if (location != null) {
            builder.location(location);
        }

        builder.publicReposCount(safeCount(dto.getPublicRepos()));
        builder.followersCount(safeCount(dto.getFollowers()));
        builder.followingCount(safeCount(dto.getFollowing()));

        String profileUrl = normalize(dto.getHtmlUrl());
        if (profileUrl != null) {
            builder.profileUrl(profileUrl);
        }

        Instant createdAt = parseInstant(dto.getCreatedAt());
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }

        Instant updatedAt = parseInstant(dto.getUpdatedAt());
        if (updatedAt != null) {
            builder.updatedAt(updatedAt);
        }

        return builder.build();
    }

    private String requireNonEmpty(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int safeCount(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    @SuppressLint("NewApi")
    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    public List<GitHubUserProfileDataEntry> mapList(List<UserDto> dtoList) {
        List<GitHubUserProfileDataEntry> result = new ArrayList<>();
        if (dtoList != null) {
            for (UserDto dto : dtoList) {
                GitHubUserProfileDataEntry entry = map(dto);
                if (entry != null) result.add(entry);
            }
        }
        return result;
    }
}