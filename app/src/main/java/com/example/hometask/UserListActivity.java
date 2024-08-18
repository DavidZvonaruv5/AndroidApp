package com.example.hometask;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar loadingProgressBar;
    private UserRepository userRepository;
    private Button prevButton, nextButton;
    private TextView pageInfoTextView;
    private ImageButton backButton;
    private EditText searchEditText;

    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private int currentPage = 1;
    private static final int USERS_PER_PAGE = 5;
    private Spinner sortSpinner;
    private static final String[] SORT_OPTIONS = {"Name", "ID", "Date Added"};
    private static final int REQUEST_USER_DETAIL = 1;

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
        searchEditText = findViewById(R.id.searchEditText);

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
        sortSpinner = findViewById(R.id.sortSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        //start sorting by the ID
        sortSpinner.setSelection(1);

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

        loadUsers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER_DETAIL && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("DELETED_USER_ID")) {
                int deletedUserId = data.getIntExtra("DELETED_USER_ID", -1);
                if (deletedUserId != -1) {
                    removeUserFromLists(deletedUserId);
                    updateUserList();
                }
            } else {
                loadUsers(); // Refresh the entire list if it wasn't a deletion
            }
        }
    }

    private void removeUserFromLists(int userId) {
        Iterator<User> allUsersIterator = allUsers.iterator();
        while (allUsersIterator.hasNext()) {
            User user = allUsersIterator.next();
            if (user.getId() == userId) {
                allUsersIterator.remove();
                break;
            }
        }

        Iterator<User> filteredUsersIterator = filteredUsers.iterator();
        while (filteredUsersIterator.hasNext()) {
            User user = filteredUsersIterator.next();
            if (user.getId() == userId) {
                filteredUsersIterator.remove();
                break;
            }
        }
    }

    private void loadUsers() {
        showLoading();
        userRepository.getAllUsers(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                runOnUiThread(() -> {
                    hideLoading();
                    allUsers = result;
                    filteredUsers = new ArrayList<>(allUsers);
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
        pageInfoTextView.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredUsers.size() / USERS_PER_PAGE);
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

    private void sortUsers(int sortOption) {
        switch (sortOption) {
            case 0: // Name
                Collections.sort(filteredUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return u1.getFirstName().compareToIgnoreCase(u2.getFirstName());
                    }
                });
                break;
            case 1: // ID
                Collections.sort(filteredUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return Integer.compare(u1.getId(), u2.getId());
                    }
                });
                break;
            case 2: // Date Added
                Collections.sort(filteredUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return u1.getCreatedAt().compareTo(u2.getCreatedAt());
                    }
                });
                break;
        }
        currentPage = 1;
        updateUserList();
    }
}