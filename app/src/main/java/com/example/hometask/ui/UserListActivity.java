package com.example.hometask.ui;

import android.content.Intent;
import android.os.Build;
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
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.UserListViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import android.graphics.Rect;


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
    private static final int USERS_PER_PAGE = 6;
    private static final String[] SORT_OPTIONS = {"Name", "ID", "Date Added"};
    private int currentSortOption = 1; // Default to ID sorting

    private final ActivityResultLauncher<Intent> userDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.hasExtra("DELETED_USER_ID")) {
                            int deletedUserId = data.getIntExtra("DELETED_USER_ID", -1);
                            if (deletedUserId != -1) {
                                removeUserFromList(deletedUserId);
                            }
                        } else if (data.hasExtra("UPDATED_USER")) {
                            User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                            if (updatedUser != null) {
                                updateUserInList(updatedUser);
                            }
                        }
                        sortUsers(currentSortOption);
                        sortSpinner.setSelection(currentSortOption);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Set light status bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

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
        sortSpinner.setSelection(currentSortOption);
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(8)); // 8dp additional space
    }

    public static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
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
            userDetailLauncher.launch(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(this, users -> {
            allUsers = new ArrayList<>(users);
            filteredUsers = new ArrayList<>(allUsers);
            sortUsers(currentSortOption);
            updateUserList();
        });

        viewModel.getIsLoading().observe(this, isLoading ->
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

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
        sortUsers(currentSortOption);
        currentPage = 1;
        updateUserList();
    }

    private void updateUserList() {
        int start = (currentPage - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, filteredUsers.size());
        List<User> usersToShow = filteredUsers.subList(start, end);

        adapter.submitList(usersToShow);
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        int totalPages = getTotalPages();
        if (filteredUsers.isEmpty()) {
            pageInfoTextView.setText(R.string.no_users_found);
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            prevButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_disabled)));
            nextButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_disabled)));
        } else {
            pageInfoTextView.setText(getString(R.string.page_info, currentPage, totalPages));

            boolean isPrevEnabled = currentPage > 1;
            boolean isNextEnabled = currentPage < totalPages;

            prevButton.setEnabled(isPrevEnabled);
            nextButton.setEnabled(isNextEnabled);

            prevButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, isPrevEnabled ? R.color.primary : R.color.button_disabled)));
            nextButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, isNextEnabled ? R.color.primary : R.color.button_disabled)));
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredUsers.size() / USERS_PER_PAGE);
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_message, message), Snackbar.LENGTH_LONG).show();
    }

    private void sortUsers(int sortOption) {
        currentSortOption = sortOption;
        switch (sortOption) {
            case 0: // Name
                Collections.sort(filteredUsers, (u1, u2) -> u1.getFirstName().compareToIgnoreCase(u2.getFirstName()));
                break;
            case 1: // ID
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(filteredUsers, Comparator.comparingInt(User::getId));
                }
                break;
            case 2: // Date Added
                Collections.sort(filteredUsers, (u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()));
                break;
        }
        currentPage = 1;
        updateUserList();
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
        sortUsers(currentSortOption);
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
        sortUsers(currentSortOption);
    }
}