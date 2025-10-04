package com.usth.githubclient.di;

// Import tất cả các lớp mà ServiceLocator sẽ quản lý.
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.repository.AuthRepository;
import com.usth.githubclient.data.repository.RepoRepository;
import com.usth.githubclient.data.repository.RepoRepositoryImpl;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.data.repository.UserRepositoryImpl;
import com.usth.githubclient.domain.mapper.RepoMapper;
import com.usth.githubclient.domain.mapper.UserMapper;

/**
 * Một "container" chứa các dependency (đối tượng phụ thuộc) rất nhỏ gọn.
 * Nó giúp cung cấp các thể hiện (instances) của Mapper & Repository trên toàn bộ ứng dụng
 * mà không cần phải sử dụng một framework DI phức tạp.
 * Đây là một dạng của mẫu thiết kế Service Locator.
 */
public final class ServiceLocator {

    // 'volatile' đảm bảo rằng các thay đổi đối với biến 'instance' sẽ được
    // các luồng (threads) khác nhìn thấy ngay lập tức. Rất quan trọng trong môi trường đa luồng.
    // 'static' để biến này là duy nhất cho toàn bộ ứng dụng.
    private static volatile ServiceLocator instance;

    // Khai báo các đối tượng mà ServiceLocator sẽ quản lý.
    // Chúng được khai báo là 'final' để đảm bảo chúng chỉ được khởi tạo một lần.
    private final ApiClient apiClient;
    private final UserMapper userMapper;
    private final RepoMapper repoMapper;
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;
    private final AuthRepository authRepository;


    // Constructor được khai báo là 'private' để ngăn các lớp khác tự tạo ra
    // các thể hiện mới của ServiceLocator. Điều này đảm bảo tính Singleton.
    private ServiceLocator() {
        // Khởi tạo tất cả các đối tượng dependency theo đúng thứ tự.
        apiClient = new ApiClient();
        userMapper = new UserMapper();
        // RepoMapper cần UserMapper, vì vậy chúng ta truyền nó vào constructor.
        repoMapper = new RepoMapper(userMapper);

        // Các lớp Impl cần ApiClient.
        userRepository = new UserRepositoryImpl(apiClient);
        repoRepository = new RepoRepositoryImpl(apiClient);
        // AuthRepository cần tất cả các dependency khác.
        authRepository = new AuthRepository(apiClient, userRepository, repoRepository, userMapper, repoMapper);
    }

    /**
     * Phương thức tĩnh để lấy thể hiện duy nhất của ServiceLocator.
     * Đây là cách triển khai "Double-Checked Locking" cho Singleton, vừa hiệu quả vừa an toàn.
     */
    public static ServiceLocator getInstance() {
        if (instance == null) { // Kiểm tra lần 1 (nhanh, không cần khóa).
            synchronized (ServiceLocator.class) { // Khóa để đảm bảo an toàn luồng.
                if (instance == null) { // Kiểm tra lần 2 (bên trong khối đã được khóa).
                    instance = new ServiceLocator(); // Chỉ tạo đối tượng nếu nó thực sự là null.
                }
            }
        }
        return instance;
    }

    // Các phương thức công khai để cung cấp các dependency cho các lớp khác.
    public ApiClient apiClient() {
        return apiClient;
    }

    public UserMapper userMapper() {
        return userMapper;
    }

    public RepoMapper repoMapper() {
        return repoMapper;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public RepoRepository repoRepository() {
        return repoRepository;
    }

    public AuthRepository authRepository() {
        return authRepository;
    }

    /**
     * Xóa thể hiện singleton. Chủ yếu được dùng trong các bài test để có thể
     * tạo lại một "graph" dependency hoàn toàn mới cho mỗi bài test.
     */
    public static void reset() {
        synchronized (ServiceLocator.class) {
            instance = null;
        }
    }
}