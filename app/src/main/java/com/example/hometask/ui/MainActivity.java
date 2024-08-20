package com.example.hometask.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.hometask.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private Button syncUsersButton;
    private Button displayUsersButton;
    private Button addUserButton;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        syncUsersButton = findViewById(R.id.loadUsersButton);
        displayUsersButton = findViewById(R.id.displayUsersButton);
        addUserButton = findViewById(R.id.addUserButton);
        Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        syncUsersButton.setOnClickListener(v -> viewModel.syncUsers());
        displayUsersButton.startAnimation(buttonAnimation);
        addUserButton.startAnimation(buttonAnimation);
        syncUsersButton.startAnimation(buttonAnimation);

        displayUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserListActivity.class);
            startActivity(intent);
        });

        addUserButton.setOnClickListener(v -> {
            // TODO: Implement navigation to AddUserActivity
            Snackbar.make(findViewById(android.R.id.content), "Add User clicked", Snackbar.LENGTH_SHORT).show();
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.getErrorMessage().observe(this, this::showErrorMessage);

        viewModel.getSyncSuccess().observe(this, success -> {
            if (success) {
                showSyncSuccessMessage();
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