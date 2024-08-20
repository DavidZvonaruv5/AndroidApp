package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> syncSuccess = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSyncSuccess() {
        return syncSuccess;
    }

    public void syncUsers() {
        isLoading.setValue(true);
        userRepository.getAllUsers(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                isLoading.postValue(false);
                syncSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }
}