package com.example.hometask.api;

import com.google.gson.annotations.SerializedName;

/**
 * ApiResponse is a generic class that wraps the response data from API calls.
 * It provides a structured way to handle API responses, allowing for consistent
 * processing of different data types.
 *
 * @param <T> The type of data contained in the response.
 */
public class ApiResponse<T> {

    /**
     * The actual data returned by the API.
     * The @SerializedName annotation is used by Gson for JSON deserialization,
     * mapping the JSON field "data" to this variable.
     */
    @SerializedName("data")
    private final T data;

    /**
     * Constructor for creating an ApiResponse object manually.
     * This can be useful for testing or when creating mock responses.
     *
     * @param data The data to be wrapped in the ApiResponse.
     */
    public ApiResponse(T data) {
        this.data = data;
    }

    /**
     * Getter method for retrieving the data from the ApiResponse.
     *
     * @return The data contained in the ApiResponse.
     */
    public T getData() {
        return data;
    }
}