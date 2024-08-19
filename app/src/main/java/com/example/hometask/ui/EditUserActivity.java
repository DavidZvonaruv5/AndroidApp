package com.example.hometask.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.EditUserViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView avatarImageView;
    private EditText firstNameEditText, lastNameEditText, emailEditText;
    private Button saveButton, changeAvatarButton;
    private ProgressBar loadingProgressBar;
    private EditUserViewModel viewModel;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        viewModel = new ViewModelProvider(this).get(EditUserViewModel.class);

        initializeViews();
        setupListeners();
        observeViewModel();

        int userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId != -1) {
            viewModel.loadUser(userId);
        } else {
            showErrorMessage("Invalid user ID");
            finish();
        }
    }

    private void initializeViews() {
        ImageButton backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        backButton.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        changeAvatarButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, this::updateUI);
        viewModel.getIsLoading().observe(this, this::showLoading);
        viewModel.getError().observe(this, this::showErrorMessage);
        viewModel.getSaveSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void updateUI(User user) {
        if (user != null) {
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            emailEditText.setText(user.getEmail());
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void saveChanges() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = viewModel.getUser().getValue();
        if (updatedUser != null) {
            updatedUser.setFirstName(firstName);
            updatedUser.setLastName(lastName);
            updatedUser.setEmail(email);

            if (selectedImageUri != null) {
                String imagePath = saveImageToInternalStorage(selectedImageUri);
                updatedUser.setAvatar(imagePath);
            }

            viewModel.updateUser(updatedUser);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File directory = getDir("images", Context.MODE_PRIVATE);
            File file = new File(directory, "avatar_" + viewModel.getUser().getValue().getId() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showLoading(boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!isLoading);
        changeAvatarButton.setEnabled(!isLoading);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(avatarImageView);
        }
    }
}