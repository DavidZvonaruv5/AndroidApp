package com.example.hometask.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.hometask.model.User;
import com.example.hometask.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserListViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> displayedUsers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(1);
    private static final int USERS_PER_PAGE = 5;

    public UserListViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<User>> getDisplayedUsers() {
        return displayedUsers;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public void loadUsers() {
        isLoading.setValue(true);
        repository.getAllUsers(new UserRepository.RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                allUsers.setValue(result);
                updatePagination(result);
                updateUserList();
                isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void previousPage() {
        if (currentPage.getValue() > 1) {
            currentPage.setValue(currentPage.getValue() - 1);
            updateUserList();
        }
    }

    public void nextPage() {
        if (currentPage.getValue() < totalPages.getValue()) {
            currentPage.setValue(currentPage.getValue() + 1);
            updateUserList();
        }
    }

    public void sortUsers(final String sortOption) {
        List<User> currentUsers = allUsers.getValue();
        if (currentUsers != null) {
            Collections.sort(currentUsers, new Comparator<User>() {
                @Override
                public int compare(User u1, User u2) {
                    switch (sortOption) {
                        case "Name":
                            return u1.getFirstName().compareToIgnoreCase(u2.getFirstName());
                        case "ID":
                            return Integer.compare(u1.getId(), u2.getId());
                        case "Date Added":
                            return u1.getCreatedAt().compareTo(u2.getCreatedAt());
                        default:
                            return 0;
                    }
                }
            });
            allUsers.setValue(currentUsers);
            updateUserList();
        }
    }

    public void searchUsers(String query) {
        List<User> allUsersList = allUsers.getValue();
        if (allUsersList != null) {
            List<User> filteredUsers = new ArrayList<>();
            for (User user : allUsersList) {
                if (user.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                        user.getLastName().toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
            updatePagination(filteredUsers);
            updateUserList();
        }
    }


    private void updatePagination(List<User> userList) {
        int total = (int) Math.ceil((double) userList.size() / USERS_PER_PAGE);
        totalPages.setValue(Math.max(1, total));
        currentPage.setValue(1);
    }

    private void updateUserList() {
        List<User> allUsersList = allUsers.getValue();
        if (allUsersList != null) {
            int start = (currentPage.getValue() - 1) * USERS_PER_PAGE;
            int end = Math.min(start + USERS_PER_PAGE, allUsersList.size());
            if (start < end) {
                List<User> pageUsers = new ArrayList<>(allUsersList.subList(start, end));
                displayedUsers.setValue(pageUsers);
            } else {
                displayedUsers.setValue(new ArrayList<>());
            }
        }
    }
}