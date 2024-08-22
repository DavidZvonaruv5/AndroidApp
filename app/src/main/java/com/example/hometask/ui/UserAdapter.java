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

public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    private OnUserClickListener listener;
    private List<User> users = Collections.synchronizedList(new ArrayList<>());

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        holder.bind(user);
    }

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
    public void submitList(List<User> newList) {
        users = Collections.synchronizedList(new ArrayList<>(newList));
        super.submitList(new ArrayList<>(users));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView emailTextView;
        private final ImageView avatarImageView;

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