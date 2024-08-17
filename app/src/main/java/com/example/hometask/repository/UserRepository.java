package com.example.hometask.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.example.hometask.api.ApiResponse;
import com.example.hometask.api.ApiService;
import com.example.hometask.api.RetrofitClient;
import com.example.hometask.database.AppDatabase;
import com.example.hometask.database.UserDao;
import com.example.hometask.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;

public class UserRepository {
    private ApiService apiService;
    private UserDao userDao;

    public UserRepository(Context context) {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    public void getAllUsers(final RepositoryCallback<List<User>> callback) {
        final List<User> allUsers = new ArrayList<>();

        // Fetch page 1
        fetchUsersFromPage(1, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.addAll(users);

                // Fetch page 2
                fetchUsersFromPage(2, new RepositoryCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        allUsers.addAll(users);
                        saveUsersToDatabase(allUsers);
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
    public void getUsersFromDatabase(final RepositoryCallback<List<User>> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<User> users = userDao.getAllUsers();
                    callback.onSuccess(users);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    private void fetchUsersFromPage(int page, final RepositoryCallback<List<User>> callback) {
        apiService.getUsers(page).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    callback.onSuccess(users);
                } else {
                    callback.onError(new Exception("API call unsuccessful for page " + page));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    private void saveUsersToDatabase(final List<User> users) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                userDao.deleteAllUsers(); // Clear existing users
                userDao.insertUsers(users);
            }
        }).start();
    }
}