package com.example.hometask.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import com.example.hometask.viewmodel.UserDetailViewModel;
import com.example.hometask.viewmodel.UserDetailViewModelFactory;

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = "UserDetailActivity";
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private ProgressBar loadingProgressBar;
    private UserDetailViewModel viewModel;
    private static final int REQUEST_EDIT_USER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        UserRepository repository = new UserRepository(this);
        UserDetailViewModelFactory factory = new UserDetailViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(UserDetailViewModel.class);

        initializeViews();
        observeViewModel();

        int userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId != -1) {
            viewModel.loadUser(userId);
        } else {
            showErrorMessage("Invalid user ID");
            finish();
        }
    }

    private void initializeViews() {
        ImageButton backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button editButton = findViewById(R.id.editButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        backButton.setOnClickListener(v -> finish());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        editButton.setOnClickListener(v -> {
            User user = viewModel.getUser().getValue();
            if (user != null) {
                Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivityForResult(intent, REQUEST_EDIT_USER);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUI);
        viewModel.getIsLoading().observe(this, this::showLoading);
        viewModel.getError().observe(this, this::showErrorMessage);
        viewModel.getUserDeleted().observe(this, isDeleted -> {
            if (isDeleted) {
                Log.d(TAG, "User deleted successfully");
                setResult(RESULT_OK);
                finish();
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
                .setPositiveButton("Delete", (dialog, which) -> {
                    User user = viewModel.getUser().getValue();
                    if (user != null) {
                        Log.d(TAG, "Deleting user with ID: " + user.getId());
                        viewModel.deleteUser(user);
                    } else {
                        Log.e(TAG, "Cannot delete user: user is null");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading(Boolean isLoading) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            User user = viewModel.getUser().getValue();
            if (user != null) {
                viewModel.loadUser(user.getId());
            }
        }
    }
}