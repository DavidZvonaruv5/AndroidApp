package com.example.hometask.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.hometask.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.UserListViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar loadingProgressBar;
    private Button prevButton, nextButton;
    private TextView pageInfoTextView;
    private ImageButton backButton;
    private TextInputEditText searchEditText;
    private Spinner sortSpinner;

    private UserListViewModel viewModel;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private int currentPage = 1;
    private static final int USERS_PER_PAGE = 5;
    private static final String[] SORT_OPTIONS = {"Name", "ID", "Date Added"};
    private static final int REQUEST_USER_DETAIL = 1;
    private int currentSortOption = 1; // Default to ID sorting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

        initViews();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        viewModel.loadUsers();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        pageInfoTextView = findViewById(R.id.pageInfoTextView);
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        sortSpinner = findViewById(R.id.sortSpinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setSelection(currentSortOption); // Start sorting by ID
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
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

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortUsers(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterUsers(s.toString());
            }
        });

        adapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);
            intent.putExtra("USER", user);
            startActivityForResult(intent, REQUEST_USER_DETAIL);
        });
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(this, users -> {
            allUsers = new ArrayList<>(users);
            filteredUsers = new ArrayList<>(allUsers);
            sortUsers(currentSortOption); // Apply current sort option
            updateUserList();
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showErrorMessage(error);
            }
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        String[] queryWords = query.toLowerCase().split("\\s+");
        for (User user : allUsers) {
            String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
            boolean matchesAllWords = true;
            for (String word : queryWords) {
                if (!fullName.contains(word)) {
                    matchesAllWords = false;
                    break;
                }
            }
            if (matchesAllWords) {
                filteredUsers.add(user);
            }
        }
        sortUsers(currentSortOption); // Apply current sort option after filtering
        currentPage = 1;
        updateUserList();
    }

    private void updateUserList() {
        int start = (currentPage - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, filteredUsers.size());
        List<User> usersToShow = filteredUsers.subList(start, end);

        adapter.setUsers(usersToShow);
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        int totalPages = getTotalPages();
        if (filteredUsers.isEmpty()) {
            pageInfoTextView.setText("No users found");
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
        } else {
            pageInfoTextView.setText("Page " + currentPage + " of " + totalPages);
            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < totalPages);
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredUsers.size() / USERS_PER_PAGE);
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }

    private void sortUsers(int sortOption) {
        currentSortOption = sortOption; // Save the current sort option
        switch (sortOption) {
            case 0: // Name
                Collections.sort(filteredUsers, (u1, u2) -> u1.getFirstName().compareToIgnoreCase(u2.getFirstName()));
                break;
            case 1: // ID
                Collections.sort(filteredUsers, (u1, u2) -> Integer.compare(u1.getId(), u2.getId()));
                break;
            case 2: // Date Added
                Collections.sort(filteredUsers, (u1, u2) -> u1.getCreatedAt().compareTo(u2.getCreatedAt()));
                break;
        }
        currentPage = 1; // Reset to first page after sorting
        updateUserList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER_DETAIL && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("DELETED_USER_ID")) {
                int deletedUserId = data.getIntExtra("DELETED_USER_ID", -1);
                if (deletedUserId != -1) {
                    removeUserFromList(deletedUserId);
                }
            } else if (data != null && data.hasExtra("UPDATED_USER")) {
                User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                updateUserInList(updatedUser);
            }

            // Reapply the current sort option and update the list
            sortUsers(currentSortOption);

            // Update the spinner selection to reflect the current sort option
            sortSpinner.setSelection(currentSortOption);
        }
    }


    private void removeUserFromList(int userId) {
        Iterator<User> iteratorAll = allUsers.iterator();
        while (iteratorAll.hasNext()) {
            User user = iteratorAll.next();
            if (user.getId() == userId) {
                iteratorAll.remove();
                break;
            }

        }

        Iterator<User> iteratorFiltered = filteredUsers.iterator();
        while (iteratorFiltered.hasNext()) {
            User user = iteratorFiltered.next();
            if (user.getId() == userId) {
                iteratorFiltered.remove();
                break;
            }
        }
        sortUsers(currentSortOption); // Reapply sorting after removal
        updateUserList();
    }

    private void updateUserInList(User updatedUser) {
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId() == updatedUser.getId()) {
                allUsers.set(i, updatedUser);
                break;
            }
        }
        for (int i = 0; i < filteredUsers.size(); i++) {
            if (filteredUsers.get(i).getId() == updatedUser.getId()) {
                filteredUsers.set(i, updatedUser);
                break;
            }
        }
        sortUsers(currentSortOption); // Reapply sorting after update
    }
}