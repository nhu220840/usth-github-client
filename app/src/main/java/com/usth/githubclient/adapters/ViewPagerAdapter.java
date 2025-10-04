package com.usth.githubclient.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về Fragment tương ứng với vị trí
        // Vị trí 0: Home (Users)
        // Vị trí 1: Repositories
        if (position == 1) {
            return SearchReposFragment.newInstance();
        }
        return SearchUsersFragment.newInstance();
    }

    @Override
    public int getItemCount() {
        // Chúng ta có 2 tab
        return 2;
    }
}