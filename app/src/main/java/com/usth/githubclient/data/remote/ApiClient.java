package com.usth.githubclient.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // BASE_URL là địa chỉ gốc của tất cả các endpoint của GitHub API.
    private static final String BASE_URL = "https://api.github.com/";
    // Biến retrofit và authToken được khai báo là static để chúng chỉ được
    // tạo một lần và được chia sẻ trên toàn bộ ứng dụng (Singleton pattern).
    private static Retrofit retrofit = null;
    private static String authToken = null;

    // Phương thức chính để lấy đối tượng Retrofit client đã được cấu hình.
    public static Retrofit getClient() {
        // HttpLoggingInterceptor dùng để ghi lại (log) chi tiết các yêu cầu mạng
        // và phản hồi. Rất hữu ích cho việc gỡ lỗi (debug).
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log toàn bộ nội dung request/response.

        // OkHttpClient là "trái tim" của việc kết nối mạng.
        // Chúng ta cấu hình nó bằng một Builder.
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // Thêm interceptor để log.
        httpClient.addInterceptor(logging);
        // Thêm một interceptor nữa để tùy chỉnh header cho MỌI yêu cầu mạng.
        httpClient.addInterceptor(chain -> {
            Request original = chain.request(); // Lấy yêu cầu gốc.
            Request.Builder requestBuilder = original.newBuilder()
                    // Thêm các header cần thiết mà GitHub API yêu cầu.
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "usth-github-client");
            // Nếu authToken đã được lưu (sau khi đăng nhập thành công)...
            if (authToken != null && !authToken.isEmpty()) {
                // ...thì đính kèm header Authorization vào yêu cầu.
                // "Bearer " là một chuẩn xác thực phổ biến.
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }
            Request request = requestBuilder.build();
            // Gửi yêu cầu đã được tùy chỉnh đi.
            return chain.proceed(request);
        });

        // Chỉ khởi tạo Retrofit nếu nó chưa được tạo trước đó.
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Đặt URL gốc.
                    // Sử dụng GsonConverterFactory để tự động chuyển đổi JSON thành các đối tượng Java (DTO).
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build()) // Sử dụng OkHttpClient đã được cấu hình.
                    .build();
        }
        return retrofit;
    }

    // Phương thức này được gọi sau khi đăng nhập thành công.
    public void setAuthToken(String token) {
        authToken = token;
        // Đặt lại (reset) retrofit thành null để lần gọi getClient() tiếp theo
        // sẽ tạo ra một client mới với token xác thực đã được cập nhật.
        retrofit = null;
        getClient();
    }

    // Phương thức này được gọi khi đăng xuất.
    public void clearAuthToken() {
        authToken = null;
        retrofit = null;
    }


    /**
     * PHƯƠNG THỨC NÀY ĐƯỢC THÊM VÀO
     * Tạo ra một service với một token cụ thể, hữu ích cho các cuộc gọi dùng một lần
     * như việc xác thực ban đầu trong AuthViewModel.
     * Nó không làm thay đổi authToken chung của ứng dụng.
     */
    public <T> T createService(String token, Class<T> serviceClass) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "usth-github-client")
                    // Điểm khác biệt chính: luôn sử dụng token được truyền vào phương thức này.
                    .header("Authorization", "Bearer " + token);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        // Tạo một đối tượng Retrofit tạm thời chỉ cho cuộc gọi này.
        Retrofit tempRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        // Tạo và trả về service.
        return tempRetrofit.create(serviceClass);
    }

    // Phương thức createService cũ (nếu có) giữ nguyên.
    // Dùng để tạo các service sử dụng authToken chung đã được lưu.
    public <T> T createService(Class<T> serviceClass) {
        return getClient().create(serviceClass);
    }
}