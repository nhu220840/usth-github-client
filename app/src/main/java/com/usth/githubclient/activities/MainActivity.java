package com.usth.githubclient.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.color.MaterialColors;
import com.usth.githubclient.R;
import com.usth.githubclient.fragments.SearchUsersFragment;

public class MainActivity extends AppCompatActivity {

    private SearchUsersFragment searchUsersFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        FragmentManager fm = getSupportFragmentManager();
        searchUsersFragment = (SearchUsersFragment) fm.findFragmentById(R.id.fragment_container);
        if (searchUsersFragment == null) {
            searchUsersFragment = SearchUsersFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, searchUsersFragment)
                    .commit();
        }

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackground(null);
        }

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if (searchUsersFragment != null) searchUsersFragment.submitQuery(query.trim());
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                // Optional: live search
                return false;
            }
        });
    }
}
