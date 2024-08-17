package com.example.hometask;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private Button loadUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadUsersButton = findViewById(R.id.loadUsersButton);

        loadUsersButton.setOnClickListener(v -> {
            showLoading();
            // Simulate loading process
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hideLoading();
                showSyncSuccessMessage();
                // TODO: Implement actual user loading logic
            }, 3000); // Simulating a 3-second loading process
        });
    }

    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadUsersButton.setEnabled(false);
    }

    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        loadUsersButton.setEnabled(true);
    }

    private void showSyncSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content), "Syncing successful", Snackbar.LENGTH_LONG).show();
    }
}