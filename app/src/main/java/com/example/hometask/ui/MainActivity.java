package com.example.hometask.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.R;
import com.example.hometask.repository.UserRepository;
import com.example.hometask.viewmodel.MainViewModel;
import com.example.hometask.viewmodel.MainViewModelFactory;

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

        UserRepository repository = new UserRepository(this);
        MainViewModelFactory factory = new MainViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        initializeViews();
        setupListeners();
        observeViewModel();
    }

    private void initializeViews() {
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        syncUsersButton = findViewById(R.id.loadUsersButton);
        displayUsersButton = findViewById(R.id.displayUsersButton);
        addUserButton = findViewById(R.id.addUserButton);
    }

    private void setupListeners() {
        syncUsersButton.setOnClickListener(v -> viewModel.syncUsers());

        displayUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserListActivity.class);
            startActivity(intent);
        });

        addUserButton.setOnClickListener(v -> {
            // TODO: Implement navigation to AddUserActivity
            Snackbar.make(findViewById(android.R.id.content), "Add User feature not implemented yet", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, this::showLoading);
        viewModel.getError().observe(this, this::showErrorMessage);
        viewModel.getSyncSuccess().observe(this, success -> {
            if (success) {
                showSyncSuccessMessage();
            }
        });
    }

    private void showLoading(Boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        syncUsersButton.setEnabled(!isLoading);
        displayUsersButton.setEnabled(!isLoading);
        addUserButton.setEnabled(!isLoading);
    }

    private void showSyncSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content), "Syncing successful", Snackbar.LENGTH_LONG).show();
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }
}