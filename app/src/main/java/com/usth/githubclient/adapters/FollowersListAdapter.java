package com.usth.githubclient.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usth.githubclient.R;
import com.usth.githubclient.databinding.FollowersListItemBinding;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;

/**
 * Adapter responsible for rendering follower entries inside a RecyclerView.
 */
public class FollowersListAdapter extends ListAdapter<GitHubUserProfileDataEntry, FollowersListAdapter.FollowerViewHolder> {

    private static final DiffUtil.ItemCallback<GitHubUserProfileDataEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GitHubUserProfileDataEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull GitHubUserProfileDataEntry oldItem,
                                               @NonNull GitHubUserProfileDataEntry newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull GitHubUserProfileDataEntry oldItem,
                                                  @NonNull GitHubUserProfileDataEntry newItem) {
                    // So sánh trực tiếp các đối tượng Optional và sửa tên phương thức
                    return oldItem.getUsername().equals(newItem.getUsername())
                            && oldItem.getDisplayName().equals(newItem.getDisplayName())
                            && oldItem.getBio().equals(newItem.getBio())
                            && oldItem.getFollowersCount() == newItem.getFollowersCount()
                            && oldItem.getPublicReposCount() == newItem.getPublicReposCount()
                            && oldItem.getAvatarUrl().equals(newItem.getAvatarUrl());
                }
            };

    private final OnFollowerClickListener listener;

    public FollowersListAdapter(@NonNull OnFollowerClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        FollowersListItemBinding binding = FollowersListItemBinding.inflate(inflater, parent, false);
        return new FollowerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FollowerViewHolder extends RecyclerView.ViewHolder {

        private final FollowersListItemBinding binding;

        FollowerViewHolder(@NonNull FollowersListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GitHubUserProfileDataEntry follower) {
            // Sử dụng orElse để lấy giá trị từ Optional, nếu không có thì dùng username
            String displayName = follower.getDisplayName().orElse(follower.getUsername());
            binding.displayName.setText(displayName);
            binding.username.setText(itemView.getResources()
                    .getString(R.string.followers_username_format, follower.getUsername()));

            // Lấy giá trị từ Optional và kiểm tra
            String bioText = follower.getBio().orElse(null);
            if (TextUtils.isEmpty(bioText)) {
                binding.bio.setVisibility(View.GONE);
                binding.bio.setText(null);
            } else {
                binding.bio.setVisibility(View.VISIBLE);
                binding.bio.setText(bioText);
            }

            // Sửa lại tên phương thức cho chính xác
            binding.stats.setText(itemView.getResources().getString(
                    R.string.followers_stats_format,
                    follower.getPublicReposCount(),
                    follower.getFollowersCount()));

            // Lấy URL từ Optional trước khi truyền vào Glide
            Glide.with(binding.avatar)
                    .load(follower.getAvatarUrl().orElse(null))
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.avatar);

            itemView.setOnClickListener(v -> listener.onFollowerClicked(follower));
        }
    }

    /**
     * Callback invoked when a follower item is tapped.
     */
    public interface OnFollowerClickListener {
        void onFollowerClicked(@NonNull GitHubUserProfileDataEntry follower);
    }
}