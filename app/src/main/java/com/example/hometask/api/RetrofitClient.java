package com.example.hometask.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient is a singleton class that provides a configured Retrofit instance
 * for making API calls. It sets up the Retrofit builder with a base URL, a Gson converter,
 * and an HTTP client with logging capabilities.
 */
public class RetrofitClient {

    /**
     * The base URL for the API. All relative URLs used in API calls will be resolved against this.
     */
    private static final String BASE_URL = "https://reqres.in/api/";

    /**
     * The Retrofit instance. It's created lazily and cached for subsequent calls.
     */
    private static Retrofit retrofit = null;

    /**
     * Returns a configured Retrofit instance.
     * If the instance doesn't exist, it creates one with logging and Gson conversion.
     *
     * @return A Retrofit instance configured with the BASE_URL and necessary converters.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Set up logging interceptor for network calls
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build OkHttpClient with the interceptor
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            // Build and configure Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}