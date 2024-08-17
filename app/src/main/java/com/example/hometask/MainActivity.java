package com.example.hometask;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import com.example.hometask.repository.RepositoryCallback;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private Button syncUsersButton;
    private Button displayUsersButton;
    private Button addUserButton;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        syncUsersButton = findViewById(R.id.loadUsersButton);
        displayUsersButton = findViewById(R.id.displayUsersButton);
        addUserButton = findViewById(R.id.addUserButton);

        userRepository = new UserRepository(this);

        syncUsersButton.setOnClickListener(v -> syncUsers());

        displayUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserListActivity.class);
            startActivity(intent);
        });

        addUserButton.setOnClickListener(v -> {
            // TODO: Implement navigation to AddUserActivity
            Snackbar.make(findViewById(android.R.id.content), "Add User clicked", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void syncUsers() {
        showLoading();
        userRepository.getAllUsers(new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                runOnUiThread(() -> {
                    hideLoading();
                    showSyncSuccessMessage();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    showErrorMessage(e.getMessage());
                });
            }
        });
    }

    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        syncUsersButton.setEnabled(false);
        displayUsersButton.setEnabled(false);
        addUserButton.setEnabled(false);
    }

    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        syncUsersButton.setEnabled(true);
        displayUsersButton.setEnabled(true);
        addUserButton.setEnabled(true);
    }

    private void showSyncSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content), "Syncing successful", Snackbar.LENGTH_LONG).show();
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }
}