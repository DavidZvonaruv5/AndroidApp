package com.example.hometask.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("data")
    private final T data;

    // Constructor for manual creation
    public ApiResponse(T data) {
        this.data = data;

    }

    // Getter for data
    public T getData() {
        return data;
    }
}