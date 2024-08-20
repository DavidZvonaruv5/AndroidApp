package com.example.hometask.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditUserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

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
            currentUser.setAvatar(imagePath);
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
            File file = new File(directory, "avatar_" + user.getValue().getId() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.postValue("Error saving image: " + e.getMessage());
            return null;
        }
    }
}