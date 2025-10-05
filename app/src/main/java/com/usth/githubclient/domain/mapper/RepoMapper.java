package com.usth.githubclient.domain.mapper;

import com.usth.githubclient.data.remote.dto.RepoDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.domain.model.ReposDataEntry;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Maps {@link RepoDto} responses to domain level {@link ReposDataEntry} values.
 */
public final class RepoMapper {

    private final UserMapper userMapper;

    public RepoMapper(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper == null");
    }

    /**
     * Maps a RepoDto to a ReposDataEntry.
     * @param dto The RepoDto to map.
     * @return The mapped ReposDataEntry.
     */
    public ReposDataEntry map(RepoDto dto) {
        Objects.requireNonNull(dto, "dto == null");

        String name = requireNonEmpty(dto.getName(), "name");
        String htmlUrl = requireNonEmpty(dto.getHtmlUrl(), "htmlUrl");

        ReposDataEntry.Builder builder = ReposDataEntry.builder(dto.getId(), name, htmlUrl);

        String fullName = normalize(dto.getFullName());
        if (fullName != null) {
            builder.fullName(fullName);
        }

        String description = normalize(dto.getDescription());
        if (description != null) {
            builder.description(description);
        }

        String language = normalize(dto.getLanguage());
        if (language != null) {
            builder.language(language);
        }

        builder.stargazersCount(safeCount(dto.getStargazersCount()));
        builder.forksCount(safeCount(dto.getForksCount()));
        builder.watchersCount(safeCount(dto.getWatchersCount()));
        builder.openIssuesCount(safeCount(dto.getOpenIssuesCount()));

        String defaultBranch = normalize(dto.getDefaultBranch());
        if (defaultBranch != null) {
            builder.defaultBranch(defaultBranch);
        }

        builder.isPrivate(dto.isPrivate());
        builder.isFork(dto.isFork());

        Instant createdAt = parseInstant(dto.getCreatedAt());
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }

        Instant updatedAt = parseInstant(dto.getUpdatedAt());
        if (updatedAt != null) {
            builder.updatedAt(updatedAt);
        }

        Instant pushedAt = parseInstant(dto.getPushedAt());
        if (pushedAt != null) {
            builder.pushedAt(pushedAt);
        }

        UserDto owner = dto.getOwner();
        if (owner != null) {
            builder.owner(userMapper.map(owner));
        }

        return builder.build();
    }

    /**
     * Maps a list of RepoDtos to a list of ReposDataEntrys.
     * @param dtos The list of RepoDtos to map.
     * @return The mapped list of ReposDataEntrys.
     */
    public List<ReposDataEntry> mapList(List<RepoDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReposDataEntry> entries = new ArrayList<>(dtos.size());
        for (RepoDto dto : dtos) {
            if (dto == null) {
                continue;
            }
            entries.add(map(dto));
        }
        return Collections.unmodifiableList(entries);
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
}