// Project-level build file where you can add configuration options common to all sub-projects/modules.
// Defines the Android Gradle Plugin version.
plugins {
    id("com.android.application") version "8.7.0" apply false
}

// Optional: A custom task to clean the project build directory.
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}