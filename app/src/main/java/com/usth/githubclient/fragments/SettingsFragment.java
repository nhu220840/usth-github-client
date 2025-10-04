package com.usth.githubclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.AuthenticationActivity;
import com.usth.githubclient.di.ServiceLocator;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout cho fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
        if (getActivity() == null) return;

        // Xóa thông tin phiên đăng nhập đã lưu
        ServiceLocator.getInstance().authRepository().signOut();

        // Tạo Intent để quay về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), AuthenticationActivity.class);

        // Xóa hết các Activity cũ khỏi stack để người dùng không thể quay lại
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        getActivity().finish();
    }
}