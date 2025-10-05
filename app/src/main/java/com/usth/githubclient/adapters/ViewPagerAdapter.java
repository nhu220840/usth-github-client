package com.usth.githubclient.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

/**
 * Adapter for the ViewPager2 in MainActivity, providing fragments for each tab.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the corresponding fragment for the given position.
        // Position 0: Home (Users)
        // Position 1: Repositories
        if (position == 1) {
            return SearchReposFragment.newInstance();
        }
        return SearchUsersFragment.newInstance();
    }

    @Override
    public int getItemCount() {
        // We have 2 tabs.
        return 2;
    }
}