// Module-level build file for the 'app' module.

// Applies the Android Application plugin.
plugins {
    id("com.android.application")
}

android {
    // Basic Android configuration like namespace, SDK versions, and application ID.
    namespace = "com.usth.githubclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.usth.githubclient"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Configures build types, such as release builds.
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Sets Java version compatibility.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Enables view binding.
    buildFeatures {
        viewBinding = true
    }
}
// Declares dependencies for the app module.
dependencies {
    // Core UI and AndroidX libraries.
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment:1.6.2")

    // MVVM architecture components.
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")

    // Networking libraries (Retrofit, OkHttp).
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Image loading library (Glide).
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Markdown renderer.
    implementation("org.markdownj:markdownj-core:0.4")

    // Testing libraries.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}