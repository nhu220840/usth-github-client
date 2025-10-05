package com.usth.githubclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.usth.githubclient.R;
import com.usth.githubclient.activities.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of users.
 * It manages how user data is bound to the views in the list.
 */
public class SearchUsersListAdapter extends RecyclerView.Adapter<SearchUsersListAdapter.VH> {

    /**
     * Inner class 'UserRow' defines the data structure for a single row.
     * It only contains the necessary information for display, helping to separate
     * the UI from the full Data Transfer Object (DTO) from the API.
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

    // The list that holds the data to be displayed.
    private final List<UserRow> items = new ArrayList<>();

    /**
     * Method to update the adapter's data list.
     * @param newItems The new list of users.
     */
    public void submit(List<UserRow> newItems) {
        items.clear(); // Clear the old data.
        if (newItems != null) items.addAll(newItems); // Add the new data.
        notifyDataSetChanged(); // Notifies the RecyclerView that the entire dataset has changed
        // and all items need to be redrawn.
    }

    /**
     * This method is called by SearchUsersFragment after fetching the detailed information
     * of a user.
     * @param login The login of the user to update.
     * @param displayName The new display name.
     * @param bio The new bio.
     * @param publicRepos The new public repository count.
     * @param followers The new follower count.
     */
    public void updateDetails(String login, String displayName, String bio, Integer publicRepos, Integer followers) {
        // Iterate through the list to find the user with the corresponding 'login'.
        for (int i = 0; i < items.size(); i++) {
            UserRow r = items.get(i);
            if (r.login.equalsIgnoreCase(login)) {
                boolean changed = false;
                // Check if each information field has changed.
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
                    // If there are any changes, notify the RecyclerView to update
                    // only the single item at position 'i'.
                    // This is much more efficient than notifyDataSetChanged().
                    notifyItemChanged(i);
                }
                break; // Stop the loop after finding and updating.
            }
        }
    }

    /**
     * Called when the RecyclerView needs to create a new ViewHolder (when a new row appears on screen).
     */
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for a row from the search_user_list_item.xml file.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_list_item, parent, false);
        // Returns a new ViewHolder containing the newly created view.
        return new VH(v);
    }

    /**
     * Called when the RecyclerView wants to display data at a specific position.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        // Get the data for the row at the 'position'.
        final UserRow row = items.get(position);
        final Context context = h.itemView.getContext();

        // Determine the name to display: prioritize displayName, otherwise use login.
        String displayName = (row.displayName != null && !row.displayName.trim().isEmpty())
                ? row.displayName
                : row.login;
        h.displayName.setText(displayName);
        h.displayName.setVisibility(View.VISIBLE);

        h.username.setText("@" + row.login);

        // Show the bio if it exists.
        if (row.bio != null && !row.bio.trim().isEmpty()) {
            h.bio.setVisibility(View.VISIBLE);
            h.bio.setText(row.bio);
        } else {
            h.bio.setVisibility(View.GONE);
        }

        // Show stats if they exist.
        if (row.publicRepos != null && row.followers != null) {
            String stats = row.publicRepos + " repositories · " + row.followers + " followers";
            h.stats.setVisibility(View.VISIBLE);
            h.stats.setText(stats);
        } else {
            h.stats.setVisibility(View.GONE);
        }

        // Use the Glide library to load and display the avatar.
        // Glide is very efficient as it automatically handles loading images from a URL,
        // caching them, and displaying a placeholder while loading.
        Glide.with(h.avatar.getContext())
                .load(row.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(h.avatar);

        // --- START OF CHANGE ---
        // Add OnClickListener to the entire item view.
        h.itemView.setOnClickListener(v -> {
            // Create an Intent to open UserProfileActivity.
            Intent intent = UserProfileActivity.createIntent(context, row.login);
            context.startActivity(intent);
        });
        // --- END OF CHANGE ---
    }

    /**
     * Returns the total number of items in the list.
     */
    @Override
    public int getItemCount() { return items.size(); }

    /**
     * ViewHolder class: Holds references to the views within a single row.
     * This avoids calling findViewById() multiple times, optimizing performance.
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName;
        TextView username;
        TextView bio;
        TextView stats;
        VH(@NonNull View itemView) {
            super(itemView);
            // Map the views once in the constructor.
            avatar      = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            username    = itemView.findViewById(R.id.username);
            bio         = itemView.findViewById(R.id.bio);
            stats       = itemView.findViewById(R.id.stats);
        }
    }
}