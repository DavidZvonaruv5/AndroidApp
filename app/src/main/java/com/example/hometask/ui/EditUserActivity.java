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


public class EditUserActivity extends AppCompatActivity {

    private AlertDialog progressDialog;
    private ImageButton backButton;
    private ImageView avatarImageView;
    private EditText firstNameEditText, lastNameEditText, emailEditText;
    private Button saveButton, changeAvatarButton;
    private Uri selectedImageUri;
    private EditUserViewModel viewModel;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.baseline_person_pin_24)
                            .error(R.drawable.baseline_person_pin_24)
                            .circleCrop()
                            .into(avatarImageView);
                } else {
                    Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

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

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        changeAvatarButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                firstNameEditText.setText(user.getFirstName());
                lastNameEditText.setText(user.getLastName());
                emailEditText.setText(user.getEmail());
                Glide.with(this)
                        .load(user.getAvatar())
                        .placeholder(R.drawable.baseline_person_pin_24) // Add this line
                        .error(R.drawable.baseline_person_pin_24) // Add this line
                        .circleCrop()
                        .into(avatarImageView);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_USER", viewModel.getUser().getValue());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    // Method to create the custom progress dialog
    private AlertDialog createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        return builder.create();
    }

    private void openImageChooser() {
        pickImage.launch("image/*");
    }

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