package com.usth.githubclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.usth.githubclient.R;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchReposListAdapter extends RecyclerView.Adapter<SearchReposListAdapter.VH> {

    private final List<RepoDto> items = new ArrayList<>();
    private final GithubApiService apiService;

    public SearchReposListAdapter(GithubApiService apiService) {
        this.apiService = apiService;
    }

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
        final RepoDto repo = items.get(position);
        final Context context = h.itemView.getContext();

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

        h.itemView.setOnClickListener(v -> {
            if (repo.getHtmlUrl() != null && !repo.getHtmlUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo.getHtmlUrl()));
                context.startActivity(browserIntent);
            }
        });

        final String[] parts = repo.getFullName().split("/");
        if (parts.length == 2) {
            final String owner = parts[0];
            final String name = parts[1];

            checkIfRepoIsStarred(owner, name, h.btnStarRepo);

            h.btnStarRepo.setOnClickListener(v -> {
                boolean isStarred = (boolean) v.getTag();
                if (isStarred) {
                    unstarRepo(owner, name, h.btnStarRepo);
                } else {
                    starRepo(owner, name, h.btnStarRepo);
                }
            });
        }
    }

    private void checkIfRepoIsStarred(String owner, String repo, ImageButton button) {
        apiService.isRepoStarred(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                boolean isStarred = response.code() == 204;
                updateStarButtonUI(isStarred, button);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                updateStarButtonUI(false, button);
            }
        });
    }

    private void starRepo(String owner, String repo, ImageButton button) {
        apiService.starRepo(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    updateStarButtonUI(true, button);
                    Toast.makeText(button.getContext(), "Starred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(button.getContext(), "Failed to star", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unstarRepo(String owner, String repo, ImageButton button) {
        apiService.unstarRepo(owner, repo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    updateStarButtonUI(false, button);
                    Toast.makeText(button.getContext(), "Unstarred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(button.getContext(), "Failed to unstar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStarButtonUI(boolean isStarred, ImageButton button) {
        Context context = button.getContext();
        if (isStarred) {
            button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_filled));
        } else {
            button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_outline));
        }
        button.setTag(isStarred);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView repoName, repoFullName, repoDescription, repoMeta, repoStats;
        ImageButton btnStarRepo;

        VH(@NonNull View itemView) {
            super(itemView);
            repoName = itemView.findViewById(R.id.repository_name);
            repoFullName = itemView.findViewById(R.id.repository_full_name);
            repoDescription = itemView.findViewById(R.id.repository_description);
            repoMeta = itemView.findViewById(R.id.repository_meta);
            repoStats = itemView.findViewById(R.id.repository_stats);
            btnStarRepo = itemView.findViewById(R.id.btn_star_repo);
        }
    }
}