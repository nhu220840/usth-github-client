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
 * An abstract base fragment that provides common UI components and functionality for list-based fragments.
 */
public abstract class BaseFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected ProgressBar progressBar;
    protected TextView emptyView;
    protected TextView sectionTitle;

    // Abstract method that child fragments must implement to provide a section title.
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

    // Abstract method to set up the RecyclerView.
    protected abstract void setupRecyclerView();

    // ----- UI management methods -----

    /**
     * Updates the section title.
     */
    protected void updateSectionTitle() {
        if (sectionTitle == null) return;

        String title = getSectionTitle(); // Get the title from the child fragment.

        if (!TextUtils.isEmpty(title)) {
            sectionTitle.setText(title);
            sectionTitle.setVisibility(View.VISIBLE);
        } else {
            sectionTitle.setVisibility(View.GONE);
        }
    }

    /**
     * Shows or hides the loading indicator.
     * @param loading True to show the loading indicator, false to hide it.
     */
    protected void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        updateSectionTitle(); // Automatically update the title.
    }

    /**
     * Shows the empty view with a message.
     * @param msg The message to display.
     */
    protected void showEmpty(String msg) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(msg);
        updateSectionTitle(); // Automatically update the title.
    }

    /**
     * Shows the list view.
     */
    protected void showList() {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        updateSectionTitle(); // Automatically update the title.
    }
}