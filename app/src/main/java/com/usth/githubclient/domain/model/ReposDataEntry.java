package com.usth.githubclient.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Immutable domain model describing a GitHub repository.
 */
public final class ReposDataEntry {

    private final long id;
    private final String name;
    private final String fullName;
    private final String description;
    private final String language;
    private final int stargazersCount;
    private final int forksCount;
    private final int watchersCount;
    private final int openIssuesCount;
    private final String htmlUrl;
    private final String defaultBranch;
    private final boolean isPrivate;
    private final boolean isFork;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant pushedAt;
    private final GitHubUserProfileDataEntry owner;

    private ReposDataEntry(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.fullName = builder.fullName;
        this.description = builder.description;
        this.language = builder.language;
        this.stargazersCount = builder.stargazersCount;
        this.forksCount = builder.forksCount;
        this.watchersCount = builder.watchersCount;
        this.openIssuesCount = builder.openIssuesCount;
        this.htmlUrl = builder.htmlUrl;
        this.defaultBranch = builder.defaultBranch;
        this.isPrivate = builder.isPrivate;
        this.isFork = builder.isFork;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.pushedAt = builder.pushedAt;
        this.owner = builder.owner;
    }

    /**
     * Creates a new builder for a ReposDataEntry.
     * @param id The repository ID.
     * @param name The repository name.
     * @param htmlUrl The URL to the repository.
     * @return A new builder instance.
     */
    public static Builder builder(long id, String name, String htmlUrl) {
        return new Builder(id, name, htmlUrl);
    }

    /**
     * Creates a builder from the current instance.
     * @return A new builder pre-populated with data from this instance.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters for all fields, using Optional for nullable fields.
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getFullName() {
        return Optional.ofNullable(fullName);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<String> getLanguage() {
        return Optional.ofNullable(language);
    }

    public int getStargazersCount() {
        return stargazersCount;
    }

    public int getForksCount() {
        return forksCount;
    }

    public int getWatchersCount() {
        return watchersCount;
    }

    public int getOpenIssuesCount() {
        return openIssuesCount;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public Optional<String> getDefaultBranch() {
        return Optional.ofNullable(defaultBranch);
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isFork() {
        return isFork;
    }

    public Optional<Instant> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<Instant> getUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public Optional<Instant> getPushedAt() {
        return Optional.ofNullable(pushedAt);
    }

    public Optional<GitHubUserProfileDataEntry> getOwner() {
        return Optional.ofNullable(owner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReposDataEntry)) {
            return false;
        }
        ReposDataEntry that = (ReposDataEntry) o;
        return id == that.id
                && stargazersCount == that.stargazersCount
                && forksCount == that.forksCount
                && watchersCount == that.watchersCount
                && openIssuesCount == that.openIssuesCount
                && isPrivate == that.isPrivate
                && isFork == that.isFork
                && Objects.equals(name, that.name)
                && Objects.equals(fullName, that.fullName)
                && Objects.equals(description, that.description)
                && Objects.equals(language, that.language)
                && Objects.equals(htmlUrl, that.htmlUrl)
                && Objects.equals(defaultBranch, that.defaultBranch)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(pushedAt, that.pushedAt)
                && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                fullName,
                description,
                language,
                stargazersCount,
                forksCount,
                watchersCount,
                openIssuesCount,
                htmlUrl,
                defaultBranch,
                isPrivate,
                isFork,
                createdAt,
                updatedAt,
                pushedAt,
                owner
        );
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReposDataEntry.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + '\'')
                .add("fullName='" + fullName + '\'')
                .add("description='" + description + '\'')
                .add("language='" + language + '\'')
                .add("stargazersCount=" + stargazersCount)
                .add("forksCount=" + forksCount)
                .add("watchersCount=" + watchersCount)
                .add("openIssuesCount=" + openIssuesCount)
                .add("htmlUrl='" + htmlUrl + '\'')
                .add("defaultBranch='" + defaultBranch + '\'')
                .add("isPrivate=" + isPrivate)
                .add("isFork=" + isFork)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("pushedAt=" + pushedAt)
                .add("owner=" + owner)
                .toString();
    }

    /**
     * Builder for creating ReposDataEntry instances.
     */
    public static final class Builder {

        private final long id;
        private final String name;
        private final String htmlUrl;
        private String fullName;
        private String description;
        private String language;
        private int stargazersCount;
        private int forksCount;
        private int watchersCount;
        private int openIssuesCount;
        private String defaultBranch;
        private boolean isPrivate;
        private boolean isFork;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant pushedAt;
        private GitHubUserProfileDataEntry owner;

        private Builder(long id, String name, String htmlUrl) {
            if (id < 0L) {
                throw new IllegalArgumentException("id must be greater than or equal to 0");
            }
            this.id = id;
            this.name = Objects.requireNonNull(name, "name == null");
            this.htmlUrl = Objects.requireNonNull(htmlUrl, "htmlUrl == null");
        }

        private Builder(ReposDataEntry entry) {
            this.id = entry.id;
            this.name = entry.name;
            this.htmlUrl = entry.htmlUrl;
            this.fullName = entry.fullName;
            this.description = entry.description;
            this.language = entry.language;
            this.stargazersCount = entry.stargazersCount;
            this.forksCount = entry.forksCount;
            this.watchersCount = entry.watchersCount;
            this.openIssuesCount = entry.openIssuesCount;
            this.defaultBranch = entry.defaultBranch;
            this.isPrivate = entry.isPrivate;
            this.isFork = entry.isFork;
            this.createdAt = entry.createdAt;
            this.updatedAt = entry.updatedAt;
            this.pushedAt = entry.pushedAt;
            this.owner = entry.owner;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder stargazersCount(int stargazersCount) {
            if (stargazersCount < 0) {
                throw new IllegalArgumentException("stargazersCount must be >= 0");
            }
            this.stargazersCount = stargazersCount;
            return this;
        }

        public Builder forksCount(int forksCount) {
            if (forksCount < 0) {
                throw new IllegalArgumentException("forksCount must be >= 0");
            }
            this.forksCount = forksCount;
            return this;
        }

        public Builder watchersCount(int watchersCount) {
            if (watchersCount < 0) {
                throw new IllegalArgumentException("watchersCount must be >= 0");
            }
            this.watchersCount = watchersCount;
            return this;
        }

        public Builder openIssuesCount(int openIssuesCount) {
            if (openIssuesCount < 0) {
                throw new IllegalArgumentException("openIssuesCount must be >= 0");
            }
            this.openIssuesCount = openIssuesCount;
            return this;
        }

        public Builder defaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        public Builder isPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public Builder isFork(boolean isFork) {
            this.isFork = isFork;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder pushedAt(Instant pushedAt) {
            this.pushedAt = pushedAt;
            return this;
        }

        public Builder owner(GitHubUserProfileDataEntry owner) {
            this.owner = owner;
            return this;
        }

        public ReposDataEntry build() {
            return new ReposDataEntry(this);
        }
    }
}