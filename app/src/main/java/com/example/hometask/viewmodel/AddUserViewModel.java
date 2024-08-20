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

public class AddUserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addSuccess = new MutableLiveData<>(false);

    public AddUserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getAddSuccess() {
        return addSuccess;
    }

    public void addUser(String firstName, String lastName, String email, Uri avatarUri) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        addSuccess.setValue(false);

        String avatarPath = avatarUri != null ? avatarUri.toString() :
                "android.resource://" + getApplication().getPackageName() + "/" + R.drawable.baseline_person_pin_24;

        User newUser = new User(email, firstName, lastName, avatarPath);

        userRepository.addUser(newUser, new UserRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long newUserId) {
                isLoading.postValue(false);
                addSuccess.postValue(true);
                // You can do something with the newUserId if needed
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error adding user: " + e.getMessage());
            }
        });
    }
}