// build.gradle.kts (Cấp Dự án)

plugins {
    // Khai báo plugin Android Application và phiên bản của nó
    id("com.android.application") version "8.7.0" apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Task dọn dẹp project (tùy chọn)
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}