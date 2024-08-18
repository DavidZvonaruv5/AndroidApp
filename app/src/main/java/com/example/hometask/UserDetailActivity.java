package com.example.hometask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

public class UserDetailActivity extends AppCompatActivity {

    private User user;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        userRepository = new UserRepository(this);

        ImageButton backButton = findViewById(R.id.backButton);
        ImageView avatarImageView = findViewById(R.id.avatarImageView);
        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView emailTextView = findViewById(R.id.emailTextView);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button editButton = findViewById(R.id.editButton);

        user = (User) getIntent().getSerializableExtra("USER");

        if (user != null) {
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());
            emailTextView.setText(user.getEmail());
        }

        backButton.setOnClickListener(v -> finish());

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        editButton.setOnClickListener(v -> {
            // TODO: Implement edit functionality
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser() {
        userRepository.deleteUser(user, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("DELETED_USER_ID", user.getId());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("UserDetailActivity", "Error deleting user", e);
                });
            }
        });
    }
}