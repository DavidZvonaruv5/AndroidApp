package com.example.hometask.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.UserDetailViewModel;

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = "UserDetailActivity";
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private UserDetailViewModel viewModel;
    private static final int REQUEST_EDIT_USER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        viewModel = new ViewModelProvider(this).get(UserDetailViewModel.class);

        ImageButton backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button editButton = findViewById(R.id.editButton);

        Log.d(TAG, "Views initialized in onCreate");
        Log.d(TAG, "nameTextView: " + (nameTextView == null ? "null" : "not null"));
        Log.d(TAG, "emailTextView: " + (emailTextView == null ? "null" : "not null"));
        Log.d(TAG, "avatarImageView: " + (avatarImageView == null ? "null" : "not null"));

        User user = (User) getIntent().getSerializableExtra("USER");
        if (user != null) {
            viewModel.setUser(user);
        }

        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("UPDATED_USER", viewModel.getUser().getValue());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
            intent.putExtra("USER", viewModel.getUser().getValue());
            startActivityForResult(intent, REQUEST_EDIT_USER);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUI);

        viewModel.getIsLoading().observe(this, isLoading -> {
            // You can show/hide a loading indicator here if needed
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getUserDeleted().observe(this, isDeleted -> {
            if (isDeleted) {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DELETED_USER_ID", viewModel.getUser().getValue().getId());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        viewModel.getUserUpdated().observe(this, isUpdated -> {
            if (isUpdated) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_USER", viewModel.getUser().getValue());
                setResult(RESULT_OK, resultIntent);
            }
        });
    }

    private void updateUI(User user) {
        if (user != null) {
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());
            emailTextView.setText(user.getEmail());
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("UPDATED_USER")) {
                User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                viewModel.updateUser(updatedUser);
            }
        }
    }
}