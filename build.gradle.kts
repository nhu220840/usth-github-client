// build.gradle.kts (Cấp Dự án)

plugins {
    // Khai báo plugin Android Application và phiên bản của nó
    id("com.android.application") version "8.13.0" apply false
}

// Task dọn dẹp project (tùy chọn)
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}