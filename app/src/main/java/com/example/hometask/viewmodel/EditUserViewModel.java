package com.example.hometask.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

public class EditUserViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public EditUserViewModel(UserRepository repository) {
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

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
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

    public void updateUser(User updatedUser) {
        isLoading.setValue(true);
        repository.updateUser(updatedUser, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                saveSuccess.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
}