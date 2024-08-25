package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.List;

/**
 * ViewModel for the User List functionality.
 * Handles the business logic for loading and displaying a list of users.
 */
public class UserListViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Constructor for UserListViewModel.
     * @param application The application context.
     */
    public UserListViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    /**
     * @return LiveData object containing the list of users.
     */
    public LiveData<List<User>> getUsers() {
        return users;
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
     * Loads the list of users from the database.
     */
    public void loadUsers() {
        isLoading.postValue(true);
        userRepository.getUsersFromDatabase(new UserRepository.RepositoryCallback<>() {
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