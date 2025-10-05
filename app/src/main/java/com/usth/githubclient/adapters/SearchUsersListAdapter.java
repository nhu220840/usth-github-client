package com.usth.githubclient.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usth.githubclient.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of users.
 */
public class SearchUsersListAdapter extends RecyclerView.Adapter<SearchUsersListAdapter.VH> {

    /**
     * Represents a row in the user list.
     */
    public static class UserRow {
        public final String login;
        public final String avatarUrl;
        public String displayName; // Real name (can be null)
        public String bio;
        public Integer publicRepos;
        public Integer followers;


        public UserRow(String login, String avatarUrl) {
            this.login = login;
            this.avatarUrl = avatarUrl;
        }
    }

    private final List<UserRow> items = new ArrayList<>();

    /**
     * Submits a new list of users to the adapter.
     * @param newItems The new list of users.
     */
    public void submit(List<UserRow> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Updates the details of a user in the list.
     * @param login The login of the user to update.
     * @param displayName The new display name.
     * @param bio The new bio.
     * @param publicRepos The new public repository count.
     * @param followers The new follower count.
     */
    public void updateDetails(String login, String displayName, String bio, Integer publicRepos, Integer followers) {
        for (int i = 0; i < items.size(); i++) {
            UserRow r = items.get(i);
            if (r.login.equalsIgnoreCase(login)) {
                boolean changed = false;
                if ((displayName != null && !displayName.equals(r.displayName)) ||
                        (displayName == null && r.displayName != null)) {
                    r.displayName = displayName;
                    changed = true;
                }
                if ((bio != null && !bio.equals(r.bio)) || (bio == null && r.bio != null)) {
                    r.bio = bio;
                    changed = true;
                }
                if ((publicRepos != null && !publicRepos.equals(r.publicRepos)) ||
                        (publicRepos == null && r.publicRepos != null)) {
                    r.publicRepos = publicRepos;
                    changed = true;
                }
                if ((followers != null && !followers.equals(r.followers)) ||
                        (followers == null && r.followers != null)) {
                    r.followers = followers;
                    changed = true;
                }

                if (changed) {
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserRow row = items.get(position);

        // Bind user data to the views.
        String displayName = (row.displayName != null && !row.displayName.trim().isEmpty())
                ? row.displayName
                : row.login;
        h.displayName.setText(displayName);
        h.displayName.setVisibility(View.VISIBLE);

        h.username.setText("@" + row.login);

        if (row.bio != null && !row.bio.trim().isEmpty()) {
            h.bio.setVisibility(View.VISIBLE);
            h.bio.setText(row.bio);
        } else {
            h.bio.setVisibility(View.GONE);
        }

        if (row.publicRepos != null && row.followers != null) {
            String stats = row.publicRepos + " repositories · " + row.followers + " followers";
            h.stats.setVisibility(View.VISIBLE);
            h.stats.setText(stats);
        } else {
            h.stats.setVisibility(View.GONE);
        }

        // Load the user's avatar using Glide.
        Glide.with(h.avatar.getContext())
                .load(row.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(h.avatar);
    }

    @Override
    public int getItemCount() { return items.size(); }

    /**
     * ViewHolder for user items.
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName;
        TextView username;
        TextView bio;
        TextView stats;
        VH(@NonNull View itemView) {
            super(itemView);
            avatar      = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            username    = itemView.findViewById(R.id.username);
            bio         = itemView.findViewById(R.id.bio);
            stats       = itemView.findViewById(R.id.stats);
        }
    }
}