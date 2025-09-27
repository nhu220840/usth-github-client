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

public class FollowersListAdapter extends RecyclerView.Adapter<FollowersListAdapter.VH> {

    public static class UserRow {
        public final String login;
        public final String avatarUrl;
        public String displayName; // tên thật (có thể null)

        public UserRow(String login, String avatarUrl) {
            this.login = login;
            this.avatarUrl = avatarUrl;
        }
    }

    private final List<UserRow> items = new ArrayList<>();

    public void submit(List<UserRow> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void updateName(String login, String displayName) {
        for (int i = 0; i < items.size(); i++) {
            UserRow r = items.get(i);
            if (r.login.equalsIgnoreCase(login)) {
                r.displayName = displayName;
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.followers_list_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserRow row = items.get(position);
        String title = (row.displayName != null && !row.displayName.isEmpty())
                ? row.displayName + " (" + row.login + ")"
                : row.login;
        h.name.setText(title);

        Glide.with(h.avatar.getContext())
                .load(row.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(h.avatar);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;
        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name   = itemView.findViewById(R.id.username);
        }
    }
}
