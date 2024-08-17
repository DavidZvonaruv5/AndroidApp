package com.example.hometask.api;

import com.example.hometask.model.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @GET("users")
    Call<ApiResponse<List<User>>> getUsers(@Query("page") int page);
}