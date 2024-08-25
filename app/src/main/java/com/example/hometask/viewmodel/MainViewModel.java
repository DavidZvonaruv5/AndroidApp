package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.List;
import java.util.Date;

/**
 * ViewModel for the Main Activity.
 * Handles the business logic for the main dashboard, including user synchronization and statistics.
 */
public class MainViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncSuccess = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> recentlyAddedUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> newUsersAdded = new MutableLiveData<>();

    /**
     * Constructor for MainViewModel.
     * @param application The application context.
     */
    public MainViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    /**
     * @return LiveData object indicating whether a loading operation is in progress.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * @return LiveData object containing any error messages.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return LiveData object indicating whether the sync operation was successful.
     */
    public LiveData<Boolean> getSyncSuccess() {
        return syncSuccess;
    }

    /**
     * @return LiveData object containing the total number of users.
     */
    public LiveData<Integer> getTotalUsers() {
        return totalUsers;
    }

    /**
     * @return LiveData object containing the number of recently added users.
     */
    public LiveData<Integer> getRecentlyAddedUsers() {
        return recentlyAddedUsers;
    }

    /**
     * Synchronizes users from the API with the local database.
     */
    public void syncUsers() {
        isLoading.postValue(true);
        userRepository.syncUsersFromApi(new UserRepository.RepositoryCallback<>() {
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

    /**
     * Loads dashboard data including total users and recently added users.
     */
    public void loadDashboardData() {
        isLoading.postValue(true);
        userRepository.getUsersFromDatabase(new UserRepository.RepositoryCallback<>() {
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

    /**
     * Counts the number of users added in the last 5 minutes.
     * @param users List of all users.
     * @return The number of recently added users.
     */
    private int countRecentlyAddedUsers(List<User> users) {
        Date now = new Date();
        long dayInMillis = 5 * 60 * 1000; // 5 minutes
        int count = 0;
        for (User user : users) {
            if (now.getTime() - user.getCreatedAt().getTime() < dayInMillis) {
                count++;
            }
        }
        return count;
    }
}