package com.example.hometask.api;

import com.example.hometask.model.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

/**
 * ApiService interface defines the API endpoints for user-related operations.
 * This interface is used with Retrofit to make HTTP requests to the backend server.
 */
public interface ApiService {

    /**
     * Fetches a list of users from the server.
     *
     * @param page The page number to fetch. Used for pagination.
     * @return A Call object wrapping an ApiResponse containing a List of User objects.
     *         The ApiResponse also includes pagination information.
     */
    @GET("users")
    Call<ApiResponse<List<User>>> getUsers(@Query("page") int page);
}