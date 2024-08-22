package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

public class UserDetailViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> userUpdated = new MutableLiveData<>(false);

    public UserDetailViewModel(Application application) {
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

    public LiveData<Boolean> getUserDeleted() {
        return userDeleted;
    }

    public LiveData<Boolean> getUserUpdated() {
        return userUpdated;
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public void deleteUser() {
        User currentUser = user.getValue();
        if (currentUser != null) {
            isLoading.setValue(true);
            userRepository.deleteUser(currentUser, new UserRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    isLoading.postValue(false);
                    userDeleted.postValue(true);
                }

                @Override
                public void onError(Exception e) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Error deleting user: " + e.getMessage());
                }
            });
        }
    }

    public void updateUser(User updatedUser) {
        isLoading.setValue(true);
        userRepository.updateUser(updatedUser, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                user.postValue(updatedUser);
                isLoading.postValue(false);
                userUpdated.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error updating user: " + e.getMessage());
            }
        });
    }
}