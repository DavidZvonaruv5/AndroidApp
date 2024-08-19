package com.example.hometask.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import com.example.hometask.viewmodel.UserListViewModel;
import com.example.hometask.viewmodel.UserListViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar loadingProgressBar;
    private Button prevButton, nextButton;
    private TextView pageInfoTextView;
    private ImageButton backButton;
    private EditText searchEditText;
    private Spinner sortSpinner;

    private UserListViewModel viewModel;

    private static final String[] SORT_OPTIONS = {"Name", "ID", "Date Added"};
    private static final int REQUEST_USER_DETAIL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        UserRepository repository = new UserRepository(this);
        UserListViewModelFactory factory = new UserListViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(UserListViewModel.class);

        initializeViews();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        Log.d(TAG, "onCreate: Initializing UserListActivity");
        viewModel.loadUsers();
    }

    private void initializeViews() {
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
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        prevButton.setOnClickListener(v -> viewModel.previousPage());
        nextButton.setOnClickListener(v -> viewModel.nextPage());

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.sortUsers(SORT_OPTIONS[position]);
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
                viewModel.searchUsers(s.toString());
            }
        });

        adapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getId());
            startActivityForResult(intent, REQUEST_USER_DETAIL);
        });
    }

    private void observeViewModel() {
        viewModel.getDisplayedUsers().observe(this, users -> {
            adapter.setUsers(users);
            adapter.notifyDataSetChanged();
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            Log.d(TAG, "observeViewModel: Loading state changed to " + isLoading);
            loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Log.e(TAG, "observeViewModel: Error received: " + error);
                showErrorMessage(error);
            }
        });

        viewModel.getCurrentPage().observe(this, currentPage -> {
            Log.d(TAG, "observeViewModel: Current page changed to " + currentPage);
            updatePaginationInfo();
        });

        viewModel.getTotalPages().observe(this, totalPages -> {
            Log.d(TAG, "observeViewModel: Total pages changed to " + totalPages);
            updatePaginationInfo();
        });
    }

    private void updatePaginationInfo() {
        int currentPage = viewModel.getCurrentPage().getValue();
        int totalPages = viewModel.getTotalPages().getValue();
        Log.d(TAG, "updatePaginationInfo: Page " + currentPage + " of " + totalPages);
        pageInfoTextView.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER_DETAIL && resultCode == RESULT_OK) {
            viewModel.loadUsers(); // Refresh the list
        }
    }
}