// [Nguồn: 6] settings.gradle.kts

// Khối 'pluginManagement' cấu hình cách Gradle quản lý các plugin.
// Các kho lưu trữ được định nghĩa ở đây sẽ được dùng để tải về các plugin
// mà dự án của bạn cần (ví dụ: plugin Android Application).
pluginManagement {
    repositories {
        // google(): Kho lưu trữ của Google, chứa các thư viện và plugin cho Android.
        google()
        // mavenCentral(): Kho lưu trữ Maven Central, một trong những kho lớn nhất
        // cho các thư viện Java và các hệ sinh thái JVM khác.
        mavenCentral()
        // gradlePluginPortal(): Cổng thông tin plugin chính thức của Gradle.
        gradlePluginPortal()
    }
}

// Khối 'dependencyResolutionManagement' cấu hình cách Gradle xử lý
// và tải về các thư viện (dependencies) cho dự án.
dependencyResolutionManagement {
    // 'repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)':
    // Đây là một cài đặt bảo mật. Nó buộc Gradle phải khai báo tất cả các kho lưu trữ
    // trong khối này, và sẽ báo lỗi nếu một mô-đun con cố gắng tự định nghĩa
    // kho lưu trữ riêng. Điều này giúp đảm bảo tính nhất quán và bảo mật.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Khai báo các kho lưu trữ nơi Gradle sẽ tìm kiếm các thư viện
        // mà ứng dụng của bạn sử dụng (ví dụ: Retrofit, Glide, ...).
        google()
        mavenCentral()
    }
}

// 'rootProject.name = "USTH GitHub Client"': Đặt tên cho dự án gốc.
// Tên này sẽ xuất hiện trong Android Studio và các công cụ khác.
rootProject.name = "USTH GitHub Client"

// 'include(":app")': Đây là phần quan trọng nhất của tệp này.
// Nó khai báo rằng có một mô-đun tên là 'app' trong dự án.
// Nếu bạn có nhiều mô-đun (ví dụ: một mô-đun thư viện), bạn sẽ
// khai báo chúng ở đây, ví dụ: include(":app", ":mylibrary").
include(":app")