package com.usth.githubclient.adapters;

// Các import cần thiết cho Android UI, xử lý sự kiện và mạng.
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

// Các import cho các lớp của dự án.
import com.usth.githubclient.R;
import com.usth.githubclient.data.remote.ApiClient; // Mặc dù không dùng trực tiếp, import có thể vẫn còn
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;

// Các import thư viện và Java.
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Adapter chịu trách nhiệm hiển thị danh sách kho lưu trữ trong RecyclerView.
public class SearchReposListAdapter extends RecyclerView.Adapter<SearchReposListAdapter.VH> {

    // Danh sách dữ liệu (RepoDto) mà adapter sẽ hiển thị.
    private final List<RepoDto> items = new ArrayList<>();
    // THAY ĐỔI 1: Khai báo apiService nhưng không khởi tạo ở đây.
    // Đối tượng này sẽ được cung cấp từ bên ngoài.
    private final GithubApiService apiService;

    // THAY ĐỔI 2: Sửa constructor để nhận vào một GithubApiService.
    // Đây được gọi là "Constructor Injection", một dạng của Dependency Injection.
    // Nó giúp lớp này không phụ thuộc cứng vào cách ApiClient được tạo ra,
    // làm cho code linh hoạt và dễ kiểm thử hơn.
    public SearchReposListAdapter(GithubApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Cập nhật danh sách dữ liệu mới cho adapter.
     * @param newItems Danh sách các RepoDto mới cần hiển thị.
     */
    public void submit(List<RepoDto> newItems) {
        items.clear(); // Xóa dữ liệu cũ.
        if (newItems != null) {
            items.addAll(newItems); // Thêm tất cả dữ liệu mới.
        }
        // Thông báo cho RecyclerView rằng toàn bộ dữ liệu đã thay đổi và cần vẽ lại.
        notifyDataSetChanged();
    }

    /**
     * Được gọi khi RecyclerView cần tạo một ViewHolder mới để hiển thị một mục.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Thổi phồng" (inflate) layout của một hàng từ tệp search_repo_list_item.xml.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_repo_list_item, parent, false);
        // Trả về một ViewHolder mới chứa view vừa tạo.
        return new VH(v);
    }

    /**
     * Được gọi khi RecyclerView muốn "kết dính" (bind) dữ liệu vào một ViewHolder
     * tại một vị trí (position) cụ thể.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        // Lấy đối tượng dữ liệu cho hàng hiện tại.
        final RepoDto repo = items.get(position);
        final Context context = h.itemView.getContext();

        // Gán dữ liệu vào các TextView tương ứng.
        h.repoName.setText(repo.getName());
        h.repoFullName.setText(repo.getFullName());

        // Chỉ hiển thị mô tả nếu nó không rỗng.
        if (!TextUtils.isEmpty(repo.getDescription())) {
            h.repoDescription.setText(repo.getDescription());
            h.repoDescription.setVisibility(View.VISIBLE);
        } else {
            h.repoDescription.setVisibility(View.GONE);
        }

        // Dùng StringJoiner để nối chuỗi một cách an toàn, tránh các giá trị null.
        StringJoiner metaJoiner = new StringJoiner(" • ");
        if (!TextUtils.isEmpty(repo.getLanguage())) {
            metaJoiner.add(repo.getLanguage());
        }
        if (!TextUtils.isEmpty(repo.getDefaultBranch())) {
            metaJoiner.add(repo.getDefaultBranch());
        }
        h.repoMeta.setText(metaJoiner.toString());

        // Hiển thị các thông số thống kê.
        String stats = repo.getStargazersCount() + " stars • " +
                repo.getForksCount() + " forks • " +
                repo.getWatchersCount() + " watchers • " +
                repo.getOpenIssuesCount() + " issues";
        h.repoStats.setText(stats);

        // Thiết lập sự kiện click cho toàn bộ item.
        h.itemView.setOnClickListener(v -> {
            if (repo.getHtmlUrl() != null && !repo.getHtmlUrl().isEmpty()) {
                // Mở trang web của repo trên GitHub bằng trình duyệt.
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo.getHtmlUrl()));
                context.startActivity(browserIntent);
            }
        });

        // Tách chuỗi "owner/repo_name" để lấy owner và tên repo.
        final String[] parts = repo.getFullName().split("/");
        if (parts.length == 2) {
            final String owner = parts[0];
            final String name = parts[1];

            // Kiểm tra trạng thái star ban đầu của repo để hiển thị icon đúng.
            checkIfRepoIsStarred(owner, name, h.btnStarRepo);

            // Thiết lập sự kiện click cho nút star.
            h.btnStarRepo.setOnClickListener(v -> {
                // Lấy trạng thái đã star hay chưa từ tag của button.
                boolean isStarred = (boolean) v.getTag();
                if (isStarred) {
                    unstarRepo(owner, name, h.btnStarRepo); // Nếu đã star, thì unstar.
                } else {
                    starRepo(owner, name, h.btnStarRepo);   // Nếu chưa star, thì star.
                }
            });
        }
    }

    /**
     * Gọi API để kiểm tra xem repo đã được người dùng star hay chưa.
     */
    private void checkIfRepoIsStarred(String owner, String repo, ImageButton button) {
        apiService.isRepoStarred(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // API trả về 204 No Content nghĩa là đã star. 404 là chưa star.
                boolean isStarred = response.code() == 204;
                updateStarButtonUI(isStarred, button);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                updateStarButtonUI(false, button); // Mặc định là chưa star nếu lỗi mạng.
            }
        });
    }

    /**
     * Gọi API để star một repo.
     */
    private void starRepo(String owner, String repo, ImageButton button) {
        apiService.starRepo(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { // 204 No Content
                    updateStarButtonUI(true, button);
                    Toast.makeText(button.getContext(), "Starred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(button.getContext(), "Failed to star", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gọi API để unstar một repo.
     */
    private void unstarRepo(String owner, String repo, ImageButton button) {
        apiService.unstarRepo(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { // 204 No Content
                    updateStarButtonUI(false, button);
                    Toast.makeText(button.getContext(), "Unstarred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(button.getContext(), "Failed to unstar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật giao diện của nút Star (đổi icon) và lưu trạng thái vào tag.
     */
    private void updateStarButtonUI(boolean isStarred, ImageButton button) {
        Context context = button.getContext();
        if (isStarred) {
            button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_filled));
        } else {
            button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_outline));
        }
        // Lưu trạng thái vào tag của button để sử dụng lại khi người dùng nhấn vào nó.
        button.setTag(isStarred);
    }

    /**
     * Trả về tổng số mục trong danh sách.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Lớp ViewHolder: Giữ các tham chiếu đến các view trong một hàng để tái sử dụng.
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView repoName, repoFullName, repoDescription, repoMeta, repoStats;
        ImageButton btnStarRepo;

        VH(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view một lần duy nhất trong constructor.
            repoName = itemView.findViewById(R.id.repository_name);
            repoFullName = itemView.findViewById(R.id.repository_full_name);
            repoDescription = itemView.findViewById(R.id.repository_description);
            repoMeta = itemView.findViewById(R.id.repository_meta);
            repoStats = itemView.findViewById(R.id.repository_stats);
            btnStarRepo = itemView.findViewById(R.id.btn_star_repo);
        }
    }
}