package com.usth.githubclient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchReposViewModel extends ViewModel {

    // MutableLiveData để chứa danh sách kho lưu trữ. Chỉ ViewModel có thể thay đổi nó.
    private final MutableLiveData<List<RepoDto>> myRepos = new MutableLiveData<>();
    // MutableLiveData để chứa thông báo lỗi.
    private final MutableLiveData<String> error = new MutableLiveData<>();
    // Cờ để đảm bảo dữ liệu chỉ được tải một lần duy nhất.
    private boolean hasLoaded = false;

    // LiveData công khai để Fragment có thể lắng nghe (chỉ đọc).
    public LiveData<List<RepoDto>> getMyRepos() {
        return myRepos;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Phương thức để tải dữ liệu, chỉ thực hiện tải lần đầu tiên được gọi.
    public void loadMyRepos() {
        // Nếu đã tải rồi thì không làm gì cả. Điều này giúp giữ lại dữ liệu
        // khi người dùng xoay màn hình.
        if (hasLoaded) {
            return;
        }

        GithubApiService apiService = new ApiClient().createService(GithubApiService.class);
        // Gọi API để lấy danh sách repo của người dùng đã xác thực.
        apiService.getAuthenticatedRepositories(30, 1, "updated").enqueue(new Callback<List<RepoDto>>() {
            @Override
            public void onResponse(Call<List<RepoDto>> call, Response<List<RepoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Nếu thành công, cập nhật giá trị cho LiveData 'myRepos'.
                    // Fragment đang lắng nghe sẽ tự động nhận được dữ liệu mới này.
                    myRepos.postValue(response.body());
                    hasLoaded = true; // Đánh dấu là đã tải thành công.
                } else {
                    // Nếu có lỗi, cập nhật giá trị cho LiveData 'error'.
                    error.postValue("Failed to load your repositories.");
                }
            }

            @Override
            public void onFailure(Call<List<RepoDto>> call, Throwable t) {
                // Nếu có lỗi mạng, cập nhật giá trị cho LiveData 'error'.
                error.postValue("Network error while loading your repositories.");
            }
        });
    }
}