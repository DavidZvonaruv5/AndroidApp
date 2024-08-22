package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.List;
import java.util.Date;

public class MainViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncSuccess = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> recentlyAddedUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> newUsersAdded = new MutableLiveData<>();

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

    public LiveData<Integer> getTotalUsers() {
        return totalUsers;
    }

    public LiveData<Integer> getRecentlyAddedUsers() {
        return recentlyAddedUsers;
    }

    public void syncUsers() {
        isLoading.postValue(true);
        userRepository.syncUsersFromApi(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> newUsers) {
                isLoading.postValue(false);
                syncSuccess.postValue(true);
                newUsersAdded.postValue(newUsers.size());
                loadDashboardData();
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void loadDashboardData() {
        isLoading.postValue(true);
        userRepository.getUsersFromDatabase(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                totalUsers.postValue(result.size());
                int recentCount = countRecentlyAddedUsers(result);
                recentlyAddedUsers.postValue(recentCount);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Error loading dashboard data: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    private int countRecentlyAddedUsers(List<User> users) {
        Date now = new Date();
        long dayInMillis = 5 * 60 * 1000;
        int count = 0;
        for (User user : users) {
            if (now.getTime() - user.getCreatedAt().getTime() < dayInMillis) {
                count++;
            }
        }
        return count;
    }
}