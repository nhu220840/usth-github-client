package com.usth.githubclient.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;

import com.usth.githubclient.R;
import com.usth.githubclient.fragments.FollowersListFragment;

public class MainActivity extends AppCompatActivity {

    private FollowersListFragment listFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        FragmentManager fm = getSupportFragmentManager();
        listFragment = (FollowersListFragment) fm.findFragmentById(R.id.fragment_container);
        if (listFragment == null) {
            listFragment = FollowersListFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, listFragment)
                    .commit();
        }

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("Search GitHub users…");
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if (listFragment != null) listFragment.submitQuery(query.trim());
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                // Optional: live search
                return false;
            }
        });
    }
}
