package com.example.hometask.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

public class UserDetailViewModel extends ViewModel {
    private static final String TAG = "UserDetailViewModel";
    private final UserRepository repository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userDeleted = new MutableLiveData<>(false);

    public UserDetailViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getUserDeleted() {
        return userDeleted;
    }

    public void loadUser(int userId) {
        isLoading.setValue(true);
        repository.getUserById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void deleteUser(User userToDelete) {
        if (userToDelete == null) {
            Log.e(TAG, "Cannot delete user: user is null");
            return;
        }

        Log.d(TAG, "Deleting user with ID: " + userToDelete.getId());
        isLoading.setValue(true);
        repository.deleteUser(userToDelete, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                userDeleted.postValue(true);
                isLoading.postValue(false);
                Log.d(TAG, "User deleted successfully");
            }

            @Override
            public void onError(Exception e) {
                error.postValue("Failed to delete user: " + e.getMessage());
                isLoading.postValue(false);
                Log.e(TAG, "Failed to delete user", e);
            }
        });
    }
}