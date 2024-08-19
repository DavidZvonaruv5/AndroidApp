package com.example.hometask.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.hometask.repository.UserRepository;

public class UserListViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository repository;

    public UserListViewModelFactory(UserRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserListViewModel.class)) {
            return (T) new UserListViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}