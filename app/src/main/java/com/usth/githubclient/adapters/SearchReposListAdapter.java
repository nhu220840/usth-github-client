package com.usth.githubclient.adapters;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usth.githubclient.R;
import com.usth.githubclient.activities.RepositoryDetailActivity;
import com.usth.githubclient.data.remote.dto.RepoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SearchReposListAdapter extends RecyclerView.Adapter<SearchReposListAdapter.VH> {

    private final List<RepoDto> items = new ArrayList<>();

    public void submit(List<RepoDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_repo_list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RepoDto repo = items.get(position);

        h.repoName.setText(repo.getName());
        h.repoFullName.setText(repo.getFullName());

        if (!TextUtils.isEmpty(repo.getDescription())) {
            h.repoDescription.setText(repo.getDescription());
            h.repoDescription.setVisibility(View.VISIBLE);
        } else {
            h.repoDescription.setVisibility(View.GONE);
        }

        StringJoiner metaJoiner = new StringJoiner(" • ");
        if (!TextUtils.isEmpty(repo.getLanguage())) {
            metaJoiner.add(repo.getLanguage());
        }
        if (!TextUtils.isEmpty(repo.getDefaultBranch())) {
            metaJoiner.add(repo.getDefaultBranch());
        }
        h.repoMeta.setText(metaJoiner.toString());

        String stats = repo.getStargazersCount() + " stars • " +
                repo.getForksCount() + " forks • " +
                repo.getWatchersCount() + " watchers • " +
                repo.getOpenIssuesCount() + " issues";
        h.repoStats.setText(stats);

        // Thêm sự kiện click
        h.itemView.setOnClickListener(v -> {
            // Tách owner và name từ full_name
            String[] parts = repo.getFullName().split("/");
            if (parts.length == 2) {
                String owner = parts[0];
                String name = parts[1];
                Intent intent = RepositoryDetailActivity.newIntent(h.itemView.getContext(), owner, name);
                h.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView repoName, repoFullName, repoDescription, repoMeta, repoStats;

        VH(@NonNull View itemView) {
            super(itemView);
            repoName = itemView.findViewById(R.id.repository_name);
            repoFullName = itemView.findViewById(R.id.repository_full_name);
            repoDescription = itemView.findViewById(R.id.repository_description);
            repoMeta = itemView.findViewById(R.id.repository_meta);
            repoStats = itemView.findViewById(R.id.repository_stats);
        }
    }
}