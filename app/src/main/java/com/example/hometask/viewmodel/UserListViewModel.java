package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.List;

public class UserListViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

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

}