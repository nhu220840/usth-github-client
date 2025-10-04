// [Nguồn: 2] build.gradle.kts (Cấp Module: app)

// Khối 'plugins' ở cấp mô-đun dùng để áp dụng các plugin đã được khai báo
// ở tệp build.gradle.kts cấp dự án.
plugins {
    // Áp dụng plugin Android Application. Dấu # và chú thích đã được xóa để gọn hơn.
    id("com.android.application")
}

// Khối 'android' là nơi cấu hình tất cả các thông số dành riêng cho Android.
android {
    // 'namespace': Một định danh duy nhất cho mã nguồn của bạn (thường giống với
    // package name trong tệp Manifest). Đây là yêu cầu bắt buộc cho các dự án mới.
    namespace = "com.usth.githubclient"
    // 'compileSdk': Phiên bản Android API mà mã nguồn của bạn sẽ được biên dịch (compile)
    // với nó. Bạn nên luôn sử dụng phiên bản API mới nhất.
    compileSdk = 34

    // 'defaultConfig': Các cấu hình mặc định sẽ được áp dụng cho tất cả các
    // biến thể build (build variants) như 'debug' hay 'release'.
    defaultConfig {
        // 'applicationId': Định danh duy nhất của ứng dụng trên Cửa hàng Google Play.
        applicationId = "com.usth.githubclient"
        // 'minSdk': Phiên bản Android API thấp nhất mà ứng dụng của bạn có thể chạy trên đó.
        // Thiết bị có API thấp hơn 26 sẽ không cài đặt được ứng dụng.
        minSdk = 26
        // 'targetSdk': Phiên bản Android API mà ứng dụng của bạn được thiết kế để chạy trên đó.
        // Bạn nên luôn đặt targetSdk là phiên bản Android mới nhất.
        targetSdk = 34
        // 'versionCode': Một số nguyên, là phiên bản nội bộ của ứng dụng.
        // Mỗi khi bạn upload một phiên bản mới lên Google Play, versionCode phải tăng lên.
        versionCode = 1
        // 'versionName': Một chuỗi ký tự, là phiên bản mà người dùng nhìn thấy.
        versionName = "1.0"
    }

    // 'buildTypes': Cấu hình các loại build khác nhau.
    buildTypes {
        // 'release': Cấu hình cho phiên bản phát hành (bản build mà bạn sẽ
        // đưa lên Google Play).
        release {
            // 'isMinifyEnabled = false': Đặt là 'true' để bật tính năng thu nhỏ mã nguồn
            // (code shrinking) bằng R8/ProGuard. Tính năng này giúp giảm kích thước APK
            // và làm mã nguồn khó bị dịch ngược hơn.
            // BẠN NÊN BẬT TÍNH NĂNG NÀY CHO PHIÊN BẢN RELEASE.
            isMinifyEnabled = false
            // 'proguardFiles': Chỉ định các tệp chứa quy tắc cho ProGuard.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // 'compileOptions': Cấu hình các tùy chọn cho trình biên dịch Java/Kotlin.
    compileOptions {
        // 'sourceCompatibility' và 'targetCompatibility': Đặt phiên bản Java
        // mà mã nguồn của bạn sử dụng và phiên bản JVM mà mã của bạn sẽ chạy trên đó.
        // JavaVersion.VERSION_17 tương ứng với Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // 'buildFeatures': Bật/tắt các tính năng build của Android Gradle Plugin.
    buildFeatures {
        // 'viewBinding = true': Bật tính năng View Binding.
        // Tính năng này tạo ra các lớp tham chiếu trực tiếp đến các view trong layout,
        // giúp loại bỏ nhu cầu sử dụng findViewById, làm mã nguồn an toàn và gọn gàng hơn.
        viewBinding = true
    }
}

// Khối 'dependencies' là nơi bạn khai báo tất cả các thư viện bên ngoài
// mà dự án của bạn cần.
dependencies {
    // Sử dụng các nhóm (region) bên dưới để thêm/bớt thư viện đúng mục đích.
    // [Nguồn: 5]
    // region Core UI & AndroidX
    // Các thư viện cơ bản cho giao diện người dùng và các thành phần cốt lõi của AndroidX.
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment:1.6.2")

    // region Kiến trúc ứng dụng (MVVM)
    // Các thư viện hỗ trợ kiến trúc MVVM (Model-View-ViewModel).
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")

    // region Kết nối mạng (Retrofit + OkHttp)
    // Các thư viện để thực hiện các cuộc gọi mạng (API).
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // region Xử lý hình ảnh
    // Thư viện Glide để tải và hiển thị hình ảnh một cách hiệu quả.
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Bật annotation processor nếu sử dụng GlideApp.

    // [Nguồn: 6]
    // region Markdown Renderer
    // Thư viện để hiển thị văn bản định dạng Markdown.
    implementation("org.markdownj:markdownj-core:0.4")

    // region Kiểm thử
    // Các thư viện dành cho việc viết và chạy test.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}