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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.usth.githubclient.R;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

public class MainActivity extends AppCompatActivity {

    private SearchUsersFragment searchUsersFragment;
    private SearchReposFragment searchReposFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));
        setupBottomNavigation();
        setupSearchView();
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        FragmentManager fm = getSupportFragmentManager();

        searchUsersFragment = SearchUsersFragment.newInstance();
        searchReposFragment = SearchReposFragment.newInstance();

        fm.beginTransaction().add(R.id.fragment_container, searchReposFragment, "2").hide(searchReposFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, searchUsersFragment, "1").commit();
        activeFragment = searchUsersFragment;

        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                fm.beginTransaction().hide(activeFragment).show(searchUsersFragment).commit();
                activeFragment = searchUsersFragment;
                updateSearchHint();
                return true;
            } else if (item.getItemId() == R.id.nav_repositories) {
                fm.beginTransaction().hide(activeFragment).show(searchReposFragment).commit();
                activeFragment = searchReposFragment;
                updateSearchHint();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = UserProfileActivity.createIntent(this, null);
                startActivity(intent);
                return false; // Return false để không chọn item này
            }
            return false;
        });
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view);
        updateSearchHint(); // Set initial hint

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (activeFragment == searchUsersFragment) {
                    searchUsersFragment.submitQuery(query.trim());
                } else if (activeFragment == searchReposFragment) {
                    searchReposFragment.submitQuery(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    if (activeFragment == searchUsersFragment) {
                        searchUsersFragment.showFollowers();
                    } else if (activeFragment == searchReposFragment) {
                        searchReposFragment.showMyRepos();
                    }
                }
                return false;
            }
        });

        // Styling for the SearchView
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

    private void updateSearchHint() {
        SearchView searchView = findViewById(R.id.search_view);
        if (activeFragment == searchUsersFragment) {
            searchView.setQueryHint(getString(R.string.search_hint));
        } else if (activeFragment == searchReposFragment) {
            searchView.setQueryHint(getString(R.string.search_repo_hint));
        }
    }
}