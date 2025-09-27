// build.gradle.kts (Cấp Module: app)

plugins {
    id("com.android.application")
}

android {
    namespace = "com.usth.githubclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.usth.githubclient"
        minSdk = 24
        targetSdk = 34
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment:1.6.2")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // ViewModel & LiveData for MVVM
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")


    // Networking with Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Image Loading with Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    // Markdown Renderer
    implementation("org.markdownj:markdownj-core:0.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}