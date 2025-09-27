// build.gradle.kts (Cấp Module: app)

plugins {
    id("com.android.application")
}

android {
    namespace = "com.usth.githubclient"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.usth.githubclient"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core & UI
    implementation(libs.appcompat.v161)
    implementation(libs.material.v1110)
    implementation(libs.constraintlayout.v214)
    implementation(libs.fragment)

    // ViewModel & LiveData for MVVM
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Networking with Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Image Loading with Glide
    implementation(libs.glide)

    // Markdown Renderer
    implementation(libs.markdownj.core)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core.v351)

    // Ensure UX (user experience)
    implementation(libs.lifecycle.viewmodel.v284)

    // Enable fragment and activity to get data
    implementation(libs.lifecycle.livedata.v284)

    // Security Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

}