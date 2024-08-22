package com.example.hometask.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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
import com.example.hometask.viewmodel.AddUserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class AddUserActivity extends AppCompatActivity {

    public static final int RESULT_USER_ADDED = 2;

    private ImageButton backButton;
    private ImageView avatarImageView;
    private Button changeAvatarButton;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private Button addUserButton;
    private Uri selectedImageUri;
    private AddUserViewModel viewModel;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(selectedImageUri).circleCrop().into(avatarImageView);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        // Set light status bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();

        // Load default avatar
        Glide.with(this)
                .load(R.drawable.baseline_person_pin_24)
                .circleCrop()
                .into(avatarImageView);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addUserButton = findViewById(R.id.addUserButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        changeAvatarButton.setOnClickListener(v -> pickImage.launch("image/*"));
        addUserButton.setOnClickListener(v -> addUser());
    }

    private void observeViewModel() {

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAddSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_USER_ADDED);
                finish();
            }
        });
    }

    private void addUser() {
        String firstName = firstNameEditText.getText() != null ? firstNameEditText.getText().toString().trim() : "";
        String lastName = lastNameEditText.getText() != null ? lastNameEditText.getText().toString().trim() : "";
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.addUser(firstName, lastName, email, selectedImageUri);
    }
}