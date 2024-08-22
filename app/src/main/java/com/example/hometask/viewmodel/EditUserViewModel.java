package com.example.hometask.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditUserViewModel extends AndroidViewModel {
    private static final String TAG = "EditUserViewModel";

    private final UserRepository userRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    public EditUserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public void updateUser(String firstName, String lastName, String email, Uri selectedImageUri) {
        User currentUser = user.getValue();
        if (currentUser == null) {
            errorMessage.setValue("No user data available");
            return;
        }

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);

        if (selectedImageUri != null) {
            String imagePath = saveImageToInternalStorage(selectedImageUri);
            if (imagePath != null) {
                currentUser.setAvatar(imagePath);
            }
        }

        isLoading.setValue(true);
        userRepository.updateUser(currentUser, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.postValue(false);
                updateSuccess.postValue(true);
                user.postValue(currentUser);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error updating user: " + e.getMessage());
            }
        });
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageUri);
            File directory = getApplication().getDir("images", Context.MODE_PRIVATE);
            User currentUser = user.getValue();
            if (currentUser == null) {
                Log.e(TAG, "Current user is null when trying to save image");
                return null;
            }
            File file = new File(directory, "avatar_" + currentUser.getId() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage(), e);
            errorMessage.postValue("Error saving image: " + e.getMessage());
            return null;
        }
    }
}