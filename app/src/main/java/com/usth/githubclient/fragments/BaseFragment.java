package com.usth.githubclient.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.usth.githubclient.R;

/**
 * Một lớp Fragment cơ sở trừu tượng chứa các thành phần UI chung và logic
 * để hiển thị danh sách, trạng thái đang tải và trạng thái trống.
 * Các Fragment hiển thị danh sách khác trong ứng dụng sẽ kế thừa từ lớp này.
 */
public abstract class BaseFragment extends Fragment {

    // Các thành phần UI được chia sẻ giữa các Fragment con.
    protected RecyclerView recyclerView;
    protected ProgressBar progressBar;
    protected TextView emptyView; // Hiển thị khi danh sách trống hoặc có lỗi.
    protected TextView sectionTitle; // Tiêu đề của khu vực danh sách.

    /**
     * PHƯƠNG THỨC TRỪU TƯỢNG: Bắt buộc các fragment con phải định nghĩa
     * phương thức này để trả về nội dung cho tiêu đề.
     * @return Chuỗi tiêu đề hoặc null để ẩn tiêu đề.
     */
    @Nullable
    protected abstract String getSectionTitle();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout chung cho các fragment danh sách.
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        // Ánh xạ các view từ layout.
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);
        // Gọi phương thức trừu tượng để các lớp con thiết lập RecyclerView của riêng mình.
        setupRecyclerView();
        return v;
    }

    /**
     * PHƯƠNG THỨC TRỪU TƯỢNG: Bắt buộc các fragment con phải định nghĩa
     * cách thiết lập RecyclerView (ví dụ: LayoutManager, Adapter).
     */
    protected abstract void setupRecyclerView();

    // ----- CÁC HÀM QUẢN LÝ UI ĐÃ ĐƯỢC NÂNG CẤP -----

    /**
     * Cập nhật nội dung và trạng thái hiển thị của tiêu đề khu vực.
     */
    protected void updateSectionTitle() {
        if (sectionTitle == null) return;

        String title = getSectionTitle(); // Lấy nội dung từ fragment con.

        if (!TextUtils.isEmpty(title)) {
            sectionTitle.setText(title);
            sectionTitle.setVisibility(View.VISIBLE);
        } else {
            sectionTitle.setVisibility(View.GONE);
        }
    }

    /**
     * Hiển thị trạng thái đang tải.
     * @param loading true để hiển thị ProgressBar, false để ẩn.
     */
    protected void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        // Ẩn RecyclerView đi thay vì GONE để nó không bị vẽ lại từ đầu.
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        updateSectionTitle(); // Tự động cập nhật tiêu đề.
    }

    /**
     * Hiển thị trạng thái trống hoặc lỗi.
     * @param msg Thông báo để hiển thị.
     */
    protected void showEmpty(String msg) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(msg);
        updateSectionTitle(); // Tự động cập nhật tiêu đề.
    }

    /**
     * Hiển thị danh sách trong RecyclerView.
     */
    protected void showList() {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        updateSectionTitle(); // Tự động cập nhật tiêu đề.
    }
}