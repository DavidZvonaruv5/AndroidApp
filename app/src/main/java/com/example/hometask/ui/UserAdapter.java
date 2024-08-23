package com.example.hometask.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hometask.R;
import com.example.hometask.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserAdapter is a RecyclerView adapter for displaying a list of User objects.
 * It extends ListAdapter for efficient list updates using DiffUtil.
 */
public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    private OnUserClickListener listener;
    private List<User> users = Collections.synchronizedList(new ArrayList<>());

    /**
     * Interface for handling user click events.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    /**
     * Constructor for UserAdapter.
     */
    public UserAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Sets the listener for user click events.
     *
     * @param listener The OnUserClickListener to be set.
     */
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    /**
     * Creates new ViewHolders for the RecyclerView.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new UserViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the data at the specified position to the ViewHolder.
     *
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        holder.bind(user);
    }

    /**
     * DiffUtil callback for calculating the difference between two non-null items in a list.
     */
    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };

    /**
     * Submits a new list to be diffed, and displayed.
     *
     * @param newList The new list to be displayed.
     */
    public void submitList(List<User> newList) {
        users = Collections.synchronizedList(new ArrayList<>(newList));
        super.submitList(new ArrayList<>(users));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns the item at the specified position in the list.
     *
     * @param position The position of the item in the list.
     * @return The User at the specified position.
     */
    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    /**
     * ViewHolder class for the UserAdapter.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView emailTextView;
        private final ImageView avatarImageView;

        /**
         * Constructor for UserViewHolder.
         *
         * @param itemView The View that you inflated in onCreateViewHolder()
         */
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(getItem(position));
                }
            });
        }

        /**
         * Binds the User data to the ViewHolder.
         *
         * @param user The User object to bind to this ViewHolder.
         */
        void bind(User user) {
            nameTextView.setText(itemView.getContext().getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
            emailTextView.setText(user.getEmail());

            String avatarPath = user.getAvatar();
            if (avatarPath != null && avatarPath.startsWith("android.resource://")) {
                // It's a resource URI, load it as a resource
                Uri resourceUri = Uri.parse(avatarPath);
                Glide.with(itemView.getContext())
                        .load(resourceUri)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching for resources
                        .into(avatarImageView);
            } else {
                // It's a regular URL or null, load it as before
                Glide.with(itemView.getContext())
                        .load(avatarPath)
                        .circleCrop()
                        .placeholder(R.drawable.baseline_person_pin_24)
                        .error(R.drawable.baseline_person_pin_24)
                        .into(avatarImageView);
            }
        }
    }
}