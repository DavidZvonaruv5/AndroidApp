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

/**
 * UserRepository acts as a single source of truth for all user-related data operations.
 * It manages the interaction between the remote data source (API) and local data source (Room database).
 */
public class UserRepository {
    private final ApiService apiService;
    private final UserDao userDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Callback interface for repository operations.
     * @param <T> The type of the result.
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * Constructor for UserRepository.
     * @param context The application context.
     */
    public UserRepository(Context context) {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    /**
     * Synchronizes users from the API with the local database.
     * @param callback Callback to handle the result.
     */
    public void syncUsersFromApi(final RepositoryCallback<List<User>> callback) {
        getAllUsers(new RepositoryCallback<>() {
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

    /**
     * Fetches all users from the API.
     * @param callback Callback to handle the result.
     */
    public void getAllUsers(final RepositoryCallback<List<User>> callback) {
        final List<User> allUsers = new ArrayList<>();

        fetchUsersFromPage(1, new RepositoryCallback<>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.addAll(users);

                fetchUsersFromPage(2, new RepositoryCallback<>() {
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

    /**
     * Retrieves all users from the local database.
     * @param callback Callback to handle the result.
     */
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

    /**
     * Fetches users from a specific page of the API.
     * @param page The page number to fetch.
     * @param callback Callback to handle the result.
     */
    private void fetchUsersFromPage(int page, final RepositoryCallback<List<User>> callback) {
        apiService.getUsers(page).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().data();
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

    /**
     * Merges users from the API with the local database.
     * @param apiUsers List of users from the API.
     * @param callback Callback to handle the result.
     */
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

    /**
     * Deletes a user from the local database.
     * @param user The user to delete.
     * @param callback Callback to handle the result.
     */
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

    /**
     * Updates a user in the local database.
     * @param user The user to update.
     * @param callback Callback to handle the result.
     */
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

    /**
     * Adds a new user to the local database.
     * @param user The user to add.
     * @param callback Callback to handle the result.
     */
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