package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

public class UserListViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<List<User>> users = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserListViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadUsers() {
        isLoading.postValue(true);
        userRepository.getUsersFromDatabase(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                users.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void removeUser(int userId) {
        List<User> currentUsers = users.getValue();
        if (currentUsers != null) {
            List<User> updatedUsers = new ArrayList<>(currentUsers);
            User userToRemove = null;
            for (User user : updatedUsers) {
                if (user.getId() == userId) {
                    userToRemove = user;
                    break;
                }
            }
            if (userToRemove != null) {
                updatedUsers.remove(userToRemove);
                users.postValue(updatedUsers);

                // Update the database
                userRepository.deleteUser(userToRemove, new UserRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // User deleted successfully from the database
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.postValue("Error deleting user: " + e.getMessage());
                    }
                });
            }
        }
    }

    public void updateUser(User updatedUser) {
        List<User> currentUsers = users.getValue();
        if (currentUsers != null) {
            List<User> updatedUsers = new ArrayList<>(currentUsers);
            for (int i = 0; i < updatedUsers.size(); i++) {
                if (updatedUsers.get(i).getId() == updatedUser.getId()) {
                    updatedUsers.set(i, updatedUser);
                    break;
                }
            }
            users.postValue(updatedUsers);

            // Update the database
            userRepository.updateUser(updatedUser, new UserRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // User updated successfully in the database
                }

                @Override
                public void onError(Exception e) {
                    errorMessage.postValue("Error updating user: " + e.getMessage());
                }
            });
        }
    }
}