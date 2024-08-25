package com.example.hometask.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hometask.R;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

/**
 * ViewModel for the Add User functionality.
 * Handles the business logic for adding a new user to the system.
 */
public class AddUserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addSuccess = new MutableLiveData<>(false);

    /**
     * Constructor for AddUserViewModel.
     * @param application The application context.
     */
    public AddUserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    /**
     * @return LiveData object containing any error messages.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return LiveData object indicating whether the add operation was successful.
     */
    public LiveData<Boolean> getAddSuccess() {
        return addSuccess;
    }

    /**
     * Adds a new user to the system.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email of the user.
     * @param avatarUri The URI of the user's avatar image.
     */
    public void addUser(String firstName, String lastName, String email, Uri avatarUri) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        addSuccess.setValue(false);

        // Set default avatar if none provided
        String avatarPath = avatarUri != null ? avatarUri.toString() :
                "android.resource://" + getApplication().getPackageName() + "/" + R.drawable.baseline_person_pin_24;

        User newUser = new User(email, firstName, lastName, avatarPath);

        userRepository.addUser(newUser, new UserRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(Long newUserId) {
                isLoading.postValue(false);
                addSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error adding user: " + e.getMessage());
            }
        });
    }
}