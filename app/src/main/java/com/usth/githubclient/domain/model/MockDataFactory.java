package com.usth.githubclient.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class that provides deterministic models for previewing the UI before the
 * networking stack is wired up.
 */
public final class MockDataFactory {

    private MockDataFactory() {
    }

    public static GitHubUserProfileDataEntry mockUserProfile() {
        return new GitHubUserProfileDataEntry(
                583231L,
                "octocat",
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "Friendly mock account for UI previews. Loves Kotlin and espresso tests.",
                "GitHub",
                "https://github.blog",
                "octocat@github.com",
                "San Francisco, CA",
                42,
                3580,
                9,
                "https://github.com/octocat",
                "2011-01-25T18:44:36Z",
                "2024-02-20T07:20:45Z"
        );
    }

    public static List<ReposDataEntry> mockRepositories() {
        return mockRepositories(mockUserProfile());
    }

    public static List<ReposDataEntry> mockRepositories(GitHubUserProfileDataEntry owner) {
        GitHubUserProfileDataEntry repoOwner = owner == null ? mockUserProfile() : owner;
        List<ReposDataEntry> repositories = new ArrayList<>();

        repositories.add(new ReposDataEntry(
                1296269L,
                "hello-android",
                repoOwner.getUsername() + "/hello-android",
                "Sample Android project demonstrating UI states and repository usage.",
                "Kotlin",
                1280,
                320,
                980,
                12,
                "https://github.com/" + repoOwner.getUsername() + "/hello-android",
                "main",
                false,
                false,
                "2023-05-10T08:00:00Z",
                "2024-02-18T16:30:00Z",
                "2024-02-21T09:15:00Z",
                repoOwner
        ));

        repositories.add(new ReposDataEntry(
                9876543L,
                "compose-showcase",
                repoOwner.getUsername() + "/compose-showcase",
                "Jetpack Compose components extracted from the GitHub Client UI.",
                "Kotlin",
                856,
                210,
                640,
                4,
                "https://github.com/" + repoOwner.getUsername() + "/compose-showcase",
                "main",
                false,
                false,
                "2022-07-05T07:45:00Z",
                "2024-01-05T11:20:00Z",
                "2024-02-28T17:10:00Z",
                repoOwner
        ));

        repositories.add(new ReposDataEntry(
                5550001L,
                "github-analytics",
                repoOwner.getUsername() + "/github-analytics",
                "Dashboard showcasing charts built entirely from mock GitHub metrics.",
                "TypeScript",
                642,
                98,
                502,
                7,
                "https://github.com/" + repoOwner.getUsername() + "/github-analytics",
                "develop",
                false,
                true,
                "2021-11-12T05:30:00Z",
                "2024-02-12T19:40:00Z",
                "2024-03-02T13:05:00Z",
                repoOwner
        ));

        return Collections.unmodifiableList(repositories);
    }

    public static List<GitHubUserProfileDataEntry> mockFollowers() {
        List<GitHubUserProfileDataEntry> followers = new ArrayList<>();

        followers.add(new GitHubUserProfileDataEntry(
                1024L,
                "android-dev",
                "Android Dev",
                "https://avatars.githubusercontent.com/u/1024?v=4",
                "Builds delightful Android experiences and shares UI prototyping tips.",
                "JetBrains",
                "https://blog.jetbrains.com",
                null,
                "Amsterdam, NL",
                75,
                2500,
                120,
                "https://github.com/android-dev",
                null,
                null
        ));

        followers.add(new GitHubUserProfileDataEntry(
                2048L,
                "compose-wizard",
                "Compose Wizard",
                "https://avatars.githubusercontent.com/u/2048?v=4",
                "Turns product mocks into silky smooth Compose layouts in minutes.",
                "Compose Studio",
                "https://compose.studio/blog",
                null,
                "Berlin, Germany",
                34,
                980,
                75,
                "https://github.com/compose-wizard",
                null,
                null
        ));

        followers.add(new GitHubUserProfileDataEntry(
                4096L,
                "design-systems",
                "Design Systems Guild",
                "https://avatars.githubusercontent.com/u/4096?v=4",
                "Collective of designers and engineers working on reusable UI kits.",
                "Systems Co.",
                "https://design.systems",
                null,
                "New York, USA",
                120,
                5400,
                260,
                "https://github.com/design-systems",
                null,
                null
        ));

        return Collections.unmodifiableList(followers);
    }

    public static UserSessionData mockUserSession() {
        GitHubUserProfileDataEntry profile = mockUserProfile();
        List<ReposDataEntry> repositories = mockRepositories(profile);
        return new UserSessionData(
                profile.getUsername(),
                "gho_mocktoken1234567890",
                "Bearer",
                "2024-12-31T23:59:59Z",
                "2024-03-15T14:45:00Z",
                profile,
                repositories
        );
    }
}

