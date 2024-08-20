package com.example.hometask.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.viewmodel.EditUserViewModel;

import java.io.File;


public class EditUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private AlertDialog progressDialog;
    private ImageButton backButton;
    private ImageView avatarImageView;
    private EditText firstNameEditText, lastNameEditText, emailEditText;
    private Button saveButton, changeAvatarButton;
    private Uri selectedImageUri;
    private EditUserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

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
                Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
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

    private void saveChanges() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateUser(firstName, lastName, email, selectedImageUri);
    }


}