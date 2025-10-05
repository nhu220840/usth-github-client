package com.usth.githubclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchUsersListAdapter kế thừa từ RecyclerView.Adapter.
 * Nó quản lý việc hiển thị danh sách người dùng trong RecyclerView.
 */
public class SearchUsersListAdapter extends RecyclerView.Adapter<SearchUsersListAdapter.VH> {

    /**
     * Lớp nội tại (inner class) 'UserRow' định nghĩa cấu trúc dữ liệu cho một hàng.
     * Nó chỉ chứa những thông tin cần thiết cho việc hiển thị, giúp tách biệt
     * giao diện khỏi DTO (Data Transfer Object) đầy đủ từ API.
     */
    public static class UserRow {
        public final String login;
        public final String avatarUrl;
        public String displayName; // Tên thật (có thể null)
        public String bio;
        public Integer publicRepos;
        public Integer followers;

        public UserRow(String login, String avatarUrl) {
            this.login = login;
            this.avatarUrl = avatarUrl;
        }
    }

    // Danh sách chứa dữ liệu sẽ được hiển thị.
    private final List<UserRow> items = new ArrayList<>();

    /**
     * Phương thức để cập nhật danh sách dữ liệu của adapter.
     * @param newItems Danh sách người dùng mới.
     */
    public void submit(List<UserRow> newItems) {
        items.clear(); // Xóa dữ liệu cũ.
        if (newItems != null) items.addAll(newItems); // Thêm dữ liệu mới.
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng toàn bộ dữ liệu đã thay đổi
        // và cần vẽ lại tất cả các mục.
    }

    /**
     * Phương thức này được gọi bởi SearchUsersFragment sau khi đã lấy được thông tin chi tiết
     * của một người dùng.
     * @param login Tên đăng nhập của người dùng cần cập nhật.
     * @param displayName Tên hiển thị mới.
     * @param bio Tiểu sử mới.
     * @param publicRepos Số lượng kho lưu trữ công khai mới.
     * @param followers Số lượng người theo dõi mới.
     */
    public void updateDetails(String login, String displayName, String bio, Integer publicRepos, Integer followers) {
        // Duyệt qua danh sách để tìm người dùng có 'login' tương ứng.
        for (int i = 0; i < items.size(); i++) {
            UserRow r = items.get(i);
            if (r.login.equalsIgnoreCase(login)) {
                boolean changed = false;
                // Kiểm tra xem từng trường thông tin có thay đổi không.
                if ((displayName != null && !displayName.equals(r.displayName)) ||
                        (displayName == null && r.displayName != null)) {
                    r.displayName = displayName;
                    changed = true;
                }
                if ((bio != null && !bio.equals(r.bio)) || (bio == null && r.bio != null)) {
                    r.bio = bio;
                    changed = true;
                }
                if ((publicRepos != null && !publicRepos.equals(r.publicRepos)) ||
                        (publicRepos == null && r.publicRepos != null)) {
                    r.publicRepos = publicRepos;
                    changed = true;
                }
                if ((followers != null && !followers.equals(r.followers)) ||
                        (followers == null && r.followers != null)) {
                    r.followers = followers;
                    changed = true;
                }

                if (changed) {
                    // Nếu có bất kỳ thay đổi nào, chỉ thông báo cho RecyclerView cập nhật
                    // lại một mục duy nhất tại vị trí 'i'.
                    // Đây là cách làm hiệu quả hơn nhiều so với notifyDataSetChanged().
                    notifyItemChanged(i);
                }
                break; // Dừng vòng lặp sau khi đã tìm thấy và cập nhật.
            }
        }
    }

    /**
     * Được gọi khi RecyclerView cần tạo một ViewHolder mới (khi một hàng mới xuất hiện trên màn hình).
     */
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Thổi phồng" (inflate) layout của một hàng từ tệp search_user_list_item.xml.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_list_item, parent, false);
        // Trả về một ViewHolder mới chứa view vừa tạo.
        return new VH(v);
    }

    /**
     * Được gọi khi RecyclerView muốn hiển thị dữ liệu tại một vị trí cụ thể.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        // Lấy dữ liệu của hàng tại vị trí 'position'.
        final UserRow row = items.get(position);
        final Context context = h.itemView.getContext();

        // Xác định tên sẽ hiển thị: ưu tiên displayName, nếu không có thì dùng login.
        String displayName = (row.displayName != null && !row.displayName.trim().isEmpty())
                ? row.displayName
                : row.login;
        h.displayName.setText(displayName);
        h.displayName.setVisibility(View.VISIBLE);

        h.username.setText("@" + row.login);

        // Hiển thị bio nếu có.
        if (row.bio != null && !row.bio.trim().isEmpty()) {
            h.bio.setVisibility(View.VISIBLE);
            h.bio.setText(row.bio);
        } else {
            h.bio.setVisibility(View.GONE);
        }

        // Hiển thị thông số thống kê nếu có.
        if (row.publicRepos != null && row.followers != null) {
            String stats = row.publicRepos + " repositories · " + row.followers + " followers";
            h.stats.setVisibility(View.VISIBLE);
            h.stats.setText(stats);
        } else {
            h.stats.setVisibility(View.GONE);
        }

        // Dùng thư viện Glide để tải và hiển thị ảnh đại diện (avatar).
        // Glide rất hiệu quả vì nó tự động xử lý việc tải ảnh từ URL,
        // cache ảnh, và hiển thị ảnh giữ chỗ (placeholder) trong khi tải.
        Glide.with(h.avatar.getContext())
                .load(row.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(h.avatar);

        // --- BẮT ĐẦU THAY ĐỔI ---
        // Thêm OnClickListener vào toàn bộ view của một mục.
        h.itemView.setOnClickListener(v -> {
            // Tạo một Intent để mở UserProfileActivity.
            Intent intent = UserProfileActivity.createIntent(context, row.login);
            context.startActivity(intent);
        });
        // --- KẾT THÚC THAY ĐỔI ---
    }

    /**
     * Trả về tổng số mục trong danh sách.
     */
    @Override
    public int getItemCount() { return items.size(); }

    /**
     * Lớp ViewHolder: Chứa các tham chiếu đến các view trong một hàng.
     * Việc này giúp tránh phải gọi findViewById() nhiều lần, tối ưu hóa hiệu năng.
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName;
        TextView username;
        TextView bio;
        TextView stats;
        VH(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view một lần duy nhất trong constructor.
            avatar      = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            username    = itemView.findViewById(R.id.username);
            bio         = itemView.findViewById(R.id.bio);
            stats       = itemView.findViewById(R.id.stats);
        }
    }
}