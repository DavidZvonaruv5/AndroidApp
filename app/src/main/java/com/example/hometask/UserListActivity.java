package com.example.hometask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import com.example.hometask.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar loadingProgressBar;
    private UserRepository userRepository;
    private Button prevButton, nextButton;
    private TextView pageInfoTextView;
    private ImageButton backButton;

    private List<User> allUsers = new ArrayList<>();
    private int currentPage = 1;
    private static final int USERS_PER_PAGE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerView = findViewById(R.id.recyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        pageInfoTextView = findViewById(R.id.pageInfoTextView);
        backButton = findViewById(R.id.backButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);

        userRepository = new UserRepository(this);

        backButton.setOnClickListener(v -> finish());

        prevButton.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updateUserList();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentPage < getTotalPages()) {
                currentPage++;
                updateUserList();
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        showLoading();
        userRepository.getAllUsers(new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                runOnUiThread(() -> {
                    hideLoading();
                    allUsers = result;
                    updateUserList();
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

    private void updateUserList() {
        int start = (currentPage - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, allUsers.size());
        List<User> usersToShow = allUsers.subList(start, end);

        adapter.setUsers(usersToShow);
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        int totalPages = getTotalPages();
        pageInfoTextView.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) allUsers.size() / USERS_PER_PAGE);
    }

    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }
}