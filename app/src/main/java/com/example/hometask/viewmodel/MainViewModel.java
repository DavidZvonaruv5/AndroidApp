package com.example.hometask.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

import java.util.List;

public class MainViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncSuccess = new MutableLiveData<>();

    public MainViewModel(UserRepository repository) {
        this.repository = repository;
    }


    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getSyncSuccess() {
        return syncSuccess;
    }

    public void syncUsers() {
        isLoading.setValue(true);
        repository.getAllUsers(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                isLoading.postValue(false);
                syncSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                error.postValue(e.getMessage());
            }
        });
    }
}