package com.example.hometask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private AlertDialog progressDialog;
    private ImageButton backButton;
    private ImageView avatarImageView;
    private EditText firstNameEditText, lastNameEditText, emailEditText;
    private Button saveButton, changeAvatarButton;
    private User user;
    private UserRepository userRepository;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        userRepository = new UserRepository(this);

        backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);

        // Initialize the custom progress dialog
        progressDialog = createProgressDialog();

        user = (User) getIntent().getSerializableExtra("USER");

        if (user != null) {
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());
            emailEditText.setText(user.getEmail());
            Glide.with(this).load(user.getAvatar()).circleCrop().into(avatarImageView);
        }

        backButton.setOnClickListener(v -> finish());
        changeAvatarButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    // Method to create the custom progress dialog
    private AlertDialog createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(dialogView);
        builder.setCancelable(false);  // Prevent dismissing the dialog by tapping outside
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
        try {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);

            if (selectedImageUri != null) {
                String imagePath = saveImageToInternalStorage(selectedImageUri);
                user.setAvatar(imagePath);
            }

            // Show the custom progress dialog
            progressDialog.show();

            userRepository.updateUser(user, new UserRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();  // Dismiss the dialog when done
                        Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("UPDATED_USER", user);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();  // Dismiss the dialog in case of an error
                        String errorMessage = "Error updating user: " + e.getMessage();
                        Toast.makeText(EditUserActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("EditUserActivity", errorMessage, e);
                    });
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();  // Dismiss the dialog in case of an exception
            String errorMessage = "Unexpected error in saveChanges: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("EditUserActivity", errorMessage, e);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File directory = getDir("images", Context.MODE_PRIVATE);
            File file = new File(directory, "avatar_" + user.getId() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}