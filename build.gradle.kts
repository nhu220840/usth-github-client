// [Nguồn: 1] build.gradle.kts (Cấp Dự án)

// Khối 'plugins' dùng để khai báo các plugin cần thiết cho việc build dự án.
// Các plugin này thường được áp dụng cho toàn bộ dự án hoặc các mô-đun con.
plugins {
    // Khai báo plugin Android Application và phiên bản của nó.
    // Plugin này cần thiết để build các ứng dụng Android.
    // 'id("com.android.application")': Đây là ID của plugin.
    // 'version "8.7.0"': Đây là phiên bản của plugin.
    // 'apply false': Câu lệnh này có nghĩa là plugin sẽ không được áp dụng ngay lập tức
    // cho tệp build này (cấp dự án), mà thay vào đó sẽ được khai báo ở đây để
    // các mô-đun con (như mô-đun 'app') có thể sử dụng mà không cần khai báo lại phiên bản.
    id("com.android.application") version "8.7.0" apply false
}

// 'tasks.register<Delete>("clean")': Đây là một task tùy chỉnh tên là 'clean'.
// Task này sẽ được thực thi khi bạn chạy lệnh './gradlew clean' trong terminal.
// '<Delete>': Đây là kiểu của task, có chức năng xóa tệp hoặc thư mục.
tasks.register<Delete>("clean") {
    // 'delete(rootProject.buildDir)': Hành động của task là xóa thư mục 'build'
    // nằm ở thư mục gốc của dự án. Thư mục này chứa các tệp được tạo ra
    // trong quá trình build và có thể an toàn để xóa đi.
    delete(rootProject.buildDir)
}