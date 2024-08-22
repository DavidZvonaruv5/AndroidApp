package com.example.hometask.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.hometask.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.hometask.viewmodel.MainViewModel;


public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private Button syncUsersButton;
    private Button displayUsersButton;
    private Button addUserButton;
    private TextView totalUsersTextView;
    private TextView recentlyAddedTextView;
    private TextView quoteTextView;
    private MainViewModel viewModel;
    private TextView titleTextView;
    private final Handler quoteHandler = new Handler();
    private int currentQuoteIndex = 0;


    private final String[] quotes = {
            "The secret of getting ahead is getting started.",
            "Don't watch the clock; do what it does. Keep going.",
            "The only way to do great work is to love what you do.",
            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "Believe you can and you're halfway there."
    };

    private final Runnable quoteRunnable = new Runnable() {
        @Override
        public void run() {
            setNextQuote();
            quoteHandler.postDelayed(this, 7000); // 7 seconds
        }
    };

    private final ActivityResultLauncher<Intent> addUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AddUserActivity.RESULT_USER_ADDED) {
                    viewModel.loadDashboardData();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set light status bar
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        // Ensure status bar icons are visible on light background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        titleTextView = findViewById(R.id.titleTextView);
        animateTitle();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initViews();
        setupAnimations();
        setupClickListeners();
        observeViewModel();
        setNextQuote();
        startQuoteRotation();

        viewModel.loadDashboardData();
    }

    private void initViews() {
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        syncUsersButton = findViewById(R.id.loadUsersButton);
        displayUsersButton = findViewById(R.id.displayUsersButton);
        addUserButton = findViewById(R.id.addUserButton);
        totalUsersTextView = findViewById(R.id.totalUsersTextView);
        recentlyAddedTextView = findViewById(R.id.recentlyAddedTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
    }

    private void animateTitle() {
        AnimationSet animationSet = new AnimationSet(true);

        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        Animation floatAnimation = AnimationUtils.loadAnimation(this, R.anim.float_animation);

        animationSet.addAnimation(pulseAnimation);
        animationSet.addAnimation(floatAnimation);

        titleTextView.startAnimation(animationSet);
    }


    private void setupAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);

        totalUsersTextView.startAnimation(fadeIn);
        recentlyAddedTextView.startAnimation(fadeIn);
        quoteTextView.startAnimation(fadeIn);

        Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        displayUsersButton.startAnimation(buttonAnimation);
        addUserButton.startAnimation(buttonAnimation);
        syncUsersButton.startAnimation(buttonAnimation);
    }

    private void setupClickListeners() {
        syncUsersButton.setOnClickListener(v -> {
            viewModel.syncUsers();
            animateButton(v);
        });

        displayUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserListActivity.class);
            startActivity(intent);
            animateButton(v);
        });
        addUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddUserActivity.class);
            addUserLauncher.launch(intent);
            animateButton(v);
        });
    }

    private void animateButton(View v) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        v.startAnimation(scaleAnimation);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
        viewModel.getSyncSuccess().observe(this, this::showSyncSuccessMessage);
        viewModel.getTotalUsers().observe(this, this::updateTotalUsers);
        viewModel.getRecentlyAddedUsers().observe(this, this::updateRecentlyAdded);
    }

    private void updateLoadingState(Boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        syncUsersButton.setEnabled(!isLoading);
        displayUsersButton.setEnabled(!isLoading);
        addUserButton.setEnabled(!isLoading);
    }

    private void showSyncSuccessMessage(Boolean success) {
        if (success) {
            Snackbar.make(findViewById(android.R.id.content), "Syncing successful", Snackbar.LENGTH_LONG).show();
        }
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }

    private void updateTotalUsers(int total) {
        totalUsersTextView.setText(getString(R.string.total_users, total));
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        totalUsersTextView.startAnimation(fadeIn);
    }

    private void updateRecentlyAdded(int count) {
        recentlyAddedTextView.setText(getString(R.string.recently_added, count));
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        recentlyAddedTextView.startAnimation(fadeIn);
    }

    private void setNextQuote() {
        String quote = quotes[currentQuoteIndex];
        quoteTextView.setText(quote);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        quoteTextView.startAnimation(fadeIn);

        // Move to the next quote, reset to 0 if we've reached the end
        currentQuoteIndex = (currentQuoteIndex + 1) % quotes.length;
    }

    private void startQuoteRotation() {
        quoteHandler.postDelayed(quoteRunnable, 7000); // Start after 7 seconds
    }

    private void stopQuoteRotation() {
        quoteHandler.removeCallbacks(quoteRunnable);
    }



    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
        startQuoteRotation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopQuoteRotation();
    }
}