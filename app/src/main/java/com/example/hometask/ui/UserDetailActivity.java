package com.example.hometask.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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

/**
 * UserDetailActivity displays detailed information about a specific user.
 * It allows viewing, editing, and deleting user information.
 */
public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = "UserDetailActivity";

    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private UserDetailViewModel viewModel;

    /**
     * ActivityResultLauncher for handling the result of editing a user.
     */
    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("UPDATED_USER")) {
                        User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                        viewModel.updateUser(updatedUser);
                    }
                }
            }
    );

    /**
     * Executes on the start of the activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        setupStatusBar();
        initViewModel();
        initViews();
        setupListeners();
        observeViewModel();
        loadUserData();
    }

    /**
     * Sets up the status bar to use light icons on a light background.
     */
    private void setupStatusBar() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);
    }

    /**
     * Initializes the ViewModel for this activity.
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(UserDetailViewModel.class);
    }

    /**
     * Initializes views by finding them in the layout.
     */
    private void initViews() {
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        Log.d(TAG, "Views initialized in onCreate");
        Log.d(TAG, "nameTextView: " + (nameTextView == null ? "null" : "not null"));
        Log.d(TAG, "emailTextView: " + (emailTextView == null ? "null" : "not null"));
        Log.d(TAG, "avatarImageView: " + (avatarImageView == null ? "null" : "not null"));
    }

    /**
     * Sets up click listeners for buttons.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button editButton = findViewById(R.id.editButton);

        backButton.setOnClickListener(v -> finishWithResult());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        editButton.setOnClickListener(v -> launchEditUserActivity());
    }

    /**
     * Finishes the activity with a result containing the updated user data.
     */
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        User user = viewModel.getUser().getValue();
        if (user != null) {
            resultIntent.putExtra("UPDATED_USER", user);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Launches the EditUserActivity to edit the current user.
     */
    private void launchEditUserActivity() {
        User user = viewModel.getUser().getValue();
        if (user != null) {
            Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
            intent.putExtra("USER", user);
            editUserLauncher.launch(intent);
        }
    }

    /**
     * Sets up observers for the ViewModel's LiveData.
     */
    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUI);
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
        viewModel.getUserDeleted().observe(this, this::handleUserDeleted);
        viewModel.getUserUpdated().observe(this, this::handleUserUpdated);
    }

    /**
     * Updates the UI with user data.
     *
     * @param user The User object containing the data to display.
     */
    private void updateUI(User user) {
        if (user != null) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .placeholder(R.drawable.baseline_person_pin_24)
                    .error(R.drawable.baseline_person_pin_24)
                    .circleCrop()
                    .into(avatarImageView);
            nameTextView.setText(getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
            emailTextView.setText(user.getEmail());
        }
    }

    /**
     * Updates the loading state of the UI.
     *
     * @param isLoading Boolean indicating if the app is in a loading state.
     */
    private void updateLoadingState(Boolean isLoading) {
        // You can show/hide a loading indicator here if needed
    }

    /**
     * Shows an error message.
     *
     * @param error The error message to display.
     */
    private void showErrorMessage(String error) {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the successful deletion of a user.
     *
     * @param isDeleted Boolean indicating if the user was successfully deleted.
     */
    private void handleUserDeleted(Boolean isDeleted) {
        if (isDeleted) {
            Toast.makeText(this, R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            User user = viewModel.getUser().getValue();
            if (user != null) {
                resultIntent.putExtra("DELETED_USER_ID", user.getId());
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    /**
     * Handles the successful update of a user.
     *
     * @param isUpdated Boolean indicating if the user was successfully updated.
     */
    private void handleUserUpdated(Boolean isUpdated) {
        if (isUpdated) {
            Toast.makeText(this, R.string.user_updated_successfully, Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            User user = viewModel.getUser().getValue();
            if (user != null) {
                resultIntent.putExtra("UPDATED_USER", user);
            }
            setResult(RESULT_OK, resultIntent);
        }
    }

    /**
     * Loads the user data from the intent.
     */
    private void loadUserData() {
        User user = (User) getIntent().getSerializableExtra("USER");
        if (user != null) {
            viewModel.setUser(user);
        }
    }

    /**
     * Shows a confirmation dialog before deleting a user.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_user)
                .setMessage(R.string.delete_user_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteUser())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}