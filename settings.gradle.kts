// Configures the repositories for plugin management.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Configures dependency resolution management for the project.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Sets the root project name and includes the app module.
rootProject.name = "USTH GitHub Client"
include(":app")