package com.usth.githubclient.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.usth.githubclient.adapters.FollowersListAdapter;
import com.usth.githubclient.databinding.FragmentGeneralListBinding;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays a scrollable list of followers and notifies the host activity when an item is tapped.
 */
public class FollowersListFragment extends Fragment {

    public static final String TAG = "FollowersListFragment";

    private FragmentGeneralListBinding binding;
    private FollowersListAdapter adapter;
    private OnFollowerSelectedListener listener;
    private List<GitHubUserProfileDataEntry> pendingFollowers = Collections.emptyList();

    public static FollowersListFragment newInstance() {
        return new FollowersListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFollowerSelectedListener) {
            listener = (OnFollowerSelectedListener) context;
        } else {
            throw new IllegalStateException("Host activity must implement OnFollowerSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGeneralListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        applyPendingFollowers();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        adapter = new FollowersListAdapter(follower -> {
            if (listener != null) {
                listener.onFollowerSelected(follower);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        binding.recyclerView.setAdapter(adapter);
    }

    /**
     * Submit a new list of followers to render. Safe to call before the fragment view is created.
     */
    public void submitList(@Nullable List<GitHubUserProfileDataEntry> followers) {
        if (followers == null) {
            pendingFollowers = Collections.emptyList();
        } else {
            pendingFollowers = new ArrayList<>(followers);
        }
        applyPendingFollowers();
    }

    private void applyPendingFollowers() {
        if (adapter == null) {
            return;
        }
        adapter.submitList(new ArrayList<>(pendingFollowers), this::updateEmptyState);
    }

    private void updateEmptyState() {
        if (binding == null) {
            return;
        }
        boolean isEmpty = adapter == null || adapter.getItemCount() == 0;
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerView.setAdapter(null);
            binding = null;
        }
        adapter = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Notifies when a follower is tapped.
     */
    public interface OnFollowerSelectedListener {
        void onFollowerSelected(@NonNull GitHubUserProfileDataEntry follower);
    }
}