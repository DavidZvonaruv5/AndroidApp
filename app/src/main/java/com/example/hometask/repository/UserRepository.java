package com.example.hometask.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.hometask.api.ApiResponse;
import com.example.hometask.api.ApiService;
import com.example.hometask.api.RetrofitClient;
import com.example.hometask.database.AppDatabase;
import com.example.hometask.database.UserDao;
import com.example.hometask.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService apiService;
    private final UserDao userDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public UserRepository(Context context) {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    public void syncUsersFromApi(final RepositoryCallback<List<User>> callback) {
        getAllUsers(new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> apiUsers) {
                mergeUsersWithDatabase(apiUsers, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getAllUsers(final RepositoryCallback<List<User>> callback) {
        final List<User> allUsers = new ArrayList<>();

        fetchUsersFromPage(1, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.addAll(users);

                fetchUsersFromPage(2, new RepositoryCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        allUsers.addAll(users);
                        callback.onSuccess(allUsers);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getUsersFromDatabase(RepositoryCallback<List<User>> callback) {
        executor.execute(() -> {
            try {
                List<User> users = userDao.getAllUsers();
                callback.onSuccess(users);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private void fetchUsersFromPage(int page, final RepositoryCallback<List<User>> callback) {
        apiService.getUsers(page).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    callback.onSuccess(users);
                } else {
                    callback.onError(new Exception("API call unsuccessful for page " + page));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    private void mergeUsersWithDatabase(final List<User> apiUsers, final RepositoryCallback<List<User>> callback) {
        executor.execute(() -> {
            List<User> newUsers = new ArrayList<>();
            for (User apiUser : apiUsers) {
                User existingUser = userDao.getUserById(apiUser.getId());
                if (existingUser == null) {
                    apiUser.setCreatedAt(new Date());
                    userDao.insertUser(apiUser);
                    newUsers.add(apiUser);
                }
            }
            callback.onSuccess(newUsers);
        });
    }

    public void deleteUser(User user, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                userDao.deleteUser(user);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateUser(User user, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                userDao.updateUser(user);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addUser(User user, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long newUserId = userDao.insertUser(user);
                callback.onSuccess(newUserId);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}