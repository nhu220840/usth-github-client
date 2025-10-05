package com.usth.githubclient.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.ViewPagerAdapter;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

/**
 * The main screen of the app, hosting the ViewPager for different fragments
 * and the BottomNavigationView for navigation.
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView navView;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        viewPager = findViewById(R.id.view_pager);
        navView = findViewById(R.id.bottom_navigation);

        // Initialize the ViewPagerAdapter and set it to the ViewPager.
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Disable horizontal swiping to control navigation only via BottomNavigationView.
        viewPager.setUserInputEnabled(false);

        setupNavigationSync();
        setupSearchView();
    }

    /**
     * Sets up synchronization between the BottomNavigationView and the ViewPager.
     */
    private void setupNavigationSync() {
        // Handle tab clicks in BottomNavigationView.
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0, false); // Switch to Home fragment.
                return true;
            } else if (itemId == R.id.nav_repositories) {
                viewPager.setCurrentItem(1, false); // Switch to Repositories fragment.
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Launch UserProfileActivity for the profile tab.
                Intent intent = UserProfileActivity.createIntent(this, null);
                startActivity(intent);
                return false; // Do not select this tab.
            }
            return false;
        });

        // Update BottomNavigationView when ViewPager page changes.
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) {
                    navView.getMenu().findItem(R.id.nav_repositories).setChecked(true);
                } else {
                    navView.getMenu().findItem(R.id.nav_home).setChecked(true);
                }
                updateSearchHint();
            }
        });
    }

    /**
     * Sets up the SearchView and its listeners.
     */
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view);
        updateSearchHint();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Submit the search query to the current fragment.
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (currentFragment instanceof SearchUsersFragment) {
                    ((SearchUsersFragment) currentFragment).submitQuery(query.trim());
                } else if (currentFragment instanceof SearchReposFragment) {
                    ((SearchReposFragment) currentFragment).submitQuery(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes in the search view (e.g., clear search).
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (newText == null || newText.trim().isEmpty()) {
                    if (currentFragment instanceof SearchUsersFragment) {
                        ((SearchUsersFragment) currentFragment).showFollowers();
                    } else if (currentFragment instanceof SearchReposFragment) {
                        ((SearchReposFragment) currentFragment).showMyRepos();
                    }
                }
                return false;
            }
        });

        // --- Styling for the SearchView ---
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackground(null);
        }
        @SuppressLint("RestrictedApi")
        SearchView.SearchAutoComplete searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            int onSurface = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurface);
            int onSurfaceVariant = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurfaceVariant);
            searchText.setTextColor(onSurface);
            searchText.setHintTextColor(onSurfaceVariant);
            searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        }
        int iconTint = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurfaceVariant);
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            ImageViewCompat.setImageTintList(searchIcon, ColorStateList.valueOf(iconTint));
        }
        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeIcon != null) {
            ImageViewCompat.setImageTintList(closeIcon, ColorStateList.valueOf(iconTint));
        }
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
    }

    /**
     * Updates the hint text of the SearchView based on the current fragment.
     */
    private void updateSearchHint() {
        SearchView searchView = findViewById(R.id.search_view);
        if (viewPager.getCurrentItem() == 0) { // Home tab (position 0)
            searchView.setQueryHint(getString(R.string.search_hint));
        } else { // Repositories tab (position 1)
            searchView.setQueryHint(getString(R.string.search_repo_hint));
        }
    }
}