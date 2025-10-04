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

public abstract class BaseFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected ProgressBar progressBar;
    protected TextView emptyView;
    protected TextView sectionTitle;

    // PHƯƠNG THỨC TRỪU TƯỢNG: Bắt buộc các fragment con phải định nghĩa
    // phương thức này để trả về nội dung cho tiêu đề.
    @Nullable
    protected abstract String getSectionTitle();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_list, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        progressBar = v.findViewById(R.id.progress);
        emptyView = v.findViewById(R.id.empty);
        sectionTitle = v.findViewById(R.id.section_title);
        setupRecyclerView();
        return v;
    }

    // Phương thức này vẫn giữ nguyên
    protected abstract void setupRecyclerView();

    // ----- CÁC HÀM QUẢN LÝ UI ĐÃ ĐƯỢC NÂNG CẤP -----

    protected void updateSectionTitle() {
        if (sectionTitle == null) return;

        String title = getSectionTitle(); // Lấy nội dung từ fragment con

        if (!TextUtils.isEmpty(title)) {
            sectionTitle.setText(title);
            sectionTitle.setVisibility(View.VISIBLE);
        } else {
            sectionTitle.setVisibility(View.GONE);
        }
    }

    protected void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        updateSectionTitle(); // Tự động cập nhật tiêu đề
    }

    protected void showEmpty(String msg) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(msg);
        updateSectionTitle(); // Tự động cập nhật tiêu đề
    }

    protected void showList() {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        updateSectionTitle(); // Tự động cập nhật tiêu đề
    }
}