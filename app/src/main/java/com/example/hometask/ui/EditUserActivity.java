package com.example.hometask.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.EditUserViewModel;

/**
 * EditUserActivity allows the user to edit an existing user's details.
 * It provides UI for modifying user information and updating the avatar image.
 */
public class EditUserActivity extends AppCompatActivity {

    private AlertDialog progressDialog;
    private ImageButton backButton;
    private ImageView avatarImageView;
    private EditText firstNameEditText, lastNameEditText, emailEditText;
    private Button saveButton, changeAvatarButton;
    private Uri selectedImageUri;
    private EditUserViewModel viewModel;

    /**
     * ActivityResultLauncher for picking an image from the device storage.
     */
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    loadImageIntoView(selectedImageUri);
                } else {
                    Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    /**
     * Executes on the start of the activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        // Set light status bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        viewModel = new ViewModelProvider(this).get(EditUserViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();

        User user = (User) getIntent().getSerializableExtra("USER");
        if (user != null) {
            viewModel.setUser(user);
        }
    }

    /**
     * Initializes the views by finding them in the layout.
     */
    private void initViews() {
        backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);
        progressDialog = createProgressDialog();
    }

    /**
     * Sets up click listeners for the buttons.
     */
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        changeAvatarButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    /**
     * Observes the ViewModel for changes and updates the UI accordingly.
     */
    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUIWithUserData);
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
        viewModel.getUpdateSuccess().observe(this, this::handleUpdateSuccess);
    }

    /**
     * Updates the UI with user data.
     */
    private void updateUIWithUserData(User user) {
        if (user != null) {
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            emailEditText.setText(user.getEmail());
            loadImageIntoView(Uri.parse(user.getAvatar()));
        }
    }

    /**
     * Updates the loading state of the UI.
     */
    private void updateLoadingState(Boolean isLoading) {
        if (isLoading) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    /**
     * Shows an error message to the user.
     */
    private void showErrorMessage(String error) {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the successful update of user data.
     */
    private void handleUpdateSuccess(Boolean success) {
        if (success) {
            Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("UPDATED_USER", viewModel.getUser().getValue());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    /**
     * Creates a custom progress dialog.
     */
    private AlertDialog createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        return builder.create();
    }

    /**
     * Opens the image chooser for selecting a new avatar.
     */
    private void openImageChooser() {
        pickImage.launch("image/*");
    }

    /**
     * Loads an image into the avatar ImageView.
     */
    private void loadImageIntoView(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.baseline_person_pin_24)
                .error(R.drawable.baseline_person_pin_24)
                .circleCrop()
                .into(avatarImageView);
    }

    /**
     * Validates user input and calls the ViewModel to save changes.
     */
    private void saveChanges() {
        String firstName = firstNameEditText.getText() != null ? firstNameEditText.getText().toString().trim() : "";
        String lastName = lastNameEditText.getText() != null ? lastNameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateUser(firstName, lastName, email, selectedImageUri);
    }
}