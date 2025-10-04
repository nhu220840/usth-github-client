package com.usth.githubclient.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

// ViewPagerAdapter kế thừa từ FragmentStateAdapter.
// Nó chịu trách nhiệm cung cấp các Fragment cho ViewPager2.
public class ViewPagerAdapter extends FragmentStateAdapter {

    // Constructor nhận vào một FragmentActivity (hoặc Fragment) để quản lý vòng đời.
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Đây là phương thức quan trọng nhất. ViewPager2 sẽ gọi nó khi cần
    // tạo một Fragment cho một vị trí (position) cụ thể.
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Dựa vào vị trí (position) để quyết định sẽ trả về Fragment nào.
        // Vị trí 0: Trang Home (hiển thị danh sách Users).
        // Vị trí 1: Trang Repositories.
        if (position == 1) {
            // Nếu vị trí là 1, tạo và trả về một thực thể mới của SearchReposFragment.
            return SearchReposFragment.newInstance();
        }
        // Đối với tất cả các trường hợp còn lại (ở đây chỉ có vị trí 0),
        // tạo và trả về một thực thể mới của SearchUsersFragment.
        return SearchUsersFragment.newInstance();
    }

    // Phương thức này cho ViewPager2 biết có tổng cộng bao nhiêu trang.
    @Override
    public int getItemCount() {
        // Chúng ta có 2 tab (Home và Repositories), vì vậy trả về 2.
        return 2;
    }
}