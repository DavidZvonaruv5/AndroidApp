package com.example.hometask.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.viewmodel.AddUserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class AddUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton backButton;
    private ImageView avatarImageView;
    private Button changeAvatarButton;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private Button addUserButton;
    private Uri selectedImageUri;
    private AddUserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

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
        changeAvatarButton.setOnClickListener(v -> openImageChooser());
        addUserButton.setOnClickListener(v -> addUser());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            // TODO: Show/hide loading indicator
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAddSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(avatarImageView);
        }
    }

    private void addUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.addUser(firstName, lastName, email, selectedImageUri);
    }
}