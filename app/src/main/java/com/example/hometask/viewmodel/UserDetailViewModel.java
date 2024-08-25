package com.example.hometask.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;

/**
 * ViewModel for the User Detail functionality.
 * Handles the business logic for viewing, updating, and deleting a specific user.
 */
public class UserDetailViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> userUpdated = new MutableLiveData<>(false);

    /**
     * Constructor for UserDetailViewModel.
     * @param application The application context.
     */
    public UserDetailViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    /**
     * @return LiveData object containing the user being viewed or edited.
     */
    public LiveData<User> getUser() {
        return user;
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
     * @return LiveData object indicating whether the user has been deleted.
     */
    public LiveData<Boolean> getUserDeleted() {
        return userDeleted;
    }

    /**
     * @return LiveData object indicating whether the user has been updated.
     */
    public LiveData<Boolean> getUserUpdated() {
        return userUpdated;
    }

    /**
     * Sets the user to be viewed or edited.
     * @param user The User object to be set.
     */
    public void setUser(User user) {
        this.user.setValue(user);
    }

    /**
     * Deletes the current user from the system.
     */
    public void deleteUser() {
        User currentUser = user.getValue();
        if (currentUser != null) {
            isLoading.setValue(true);
            userRepository.deleteUser(currentUser, new UserRepository.RepositoryCallback<>() {
                @Override
                public void onSuccess(Void result) {
                    isLoading.postValue(false);
                    userDeleted.postValue(true);
                }

                @Override
                public void onError(Exception e) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Error deleting user: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Updates the user's information in the system.
     * @param updatedUser The User object with updated information.
     */
    public void updateUser(User updatedUser) {
        isLoading.setValue(true);
        userRepository.updateUser(updatedUser, new UserRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(Void result) {
                user.postValue(updatedUser);
                isLoading.postValue(false);
                userUpdated.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error updating user: " + e.getMessage());
            }
        });
    }
}