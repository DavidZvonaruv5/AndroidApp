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
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private User user;
    private UserRepository userRepository;
    private static final int REQUEST_EDIT_USER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        userRepository = new UserRepository(this);

        ImageButton backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button editButton = findViewById(R.id.editButton);

        Log.d("UserDetailActivity", "Views initialized in onCreate");
        Log.d("UserDetailActivity", "nameTextView: " + (nameTextView == null ? "null" : "not null"));
        Log.d("UserDetailActivity", "emailTextView: " + (emailTextView == null ? "null" : "not null"));
        Log.d("UserDetailActivity", "avatarImageView: " + (avatarImageView == null ? "null" : "not null"));

        user = (User) getIntent().getSerializableExtra("USER");

        if (user != null) {
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());
            emailTextView.setText(user.getEmail());
        }

        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("UPDATED_USER", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
            intent.putExtra("USER", user);
            startActivityForResult(intent, REQUEST_EDIT_USER);
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
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("UPDATED_USER")) {
                user = (User) data.getSerializableExtra("UPDATED_USER");
                updateUI();
                // Set the result to be sent back to UserListActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_USER", user);
                setResult(RESULT_OK, resultIntent);
            }
        }
    }

    private void updateUI() {
        if (user != null) {
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());
            emailTextView.setText(user.getEmail());
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
        }
    }
}