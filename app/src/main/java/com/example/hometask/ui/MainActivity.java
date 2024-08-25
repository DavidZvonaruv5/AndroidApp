package com.example.hometask.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
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

/**
 * MainActivity serves as the main dashboard of the application.
 * It displays user statistics, inspirational quotes, and provides navigation to other features.
 */
public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private Button syncUsersButton;
    private Button displayUsersButton;
    private Button addUserButton;
    private TextView totalUsersTextView;
    private TextView recentlyAddedTextView;
    private TextView quoteTextView;
    private MainViewModel viewModel;
    private final Handler quoteHandler = new Handler();
    private int currentQuoteIndex = 0;


    private final String[] quotes = {
            "The secret of getting ahead is getting started.",
            "Don't watch the clock; do what it does. Keep going.",
            "The only way to do great work is to love what you do.",
            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "Believe you can and you're halfway there."
    };

    /**
     * Runnable for rotating quotes at a fixed interval.
     */
    private final Runnable quoteRunnable = new Runnable() {
        @Override
        public void run() {
            setNextQuote();
            quoteHandler.postDelayed(this, 7000); // 7 seconds
        }
    };

    /**
     * ActivityResultLauncher for handling the result of adding a new user.
     */
    private final ActivityResultLauncher<Intent> addUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AddUserActivity.RESULT_USER_ADDED) {
                    viewModel.loadDashboardData();
                }
            }
    );

    /**
     * Executes on the start of the activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupStatusBar();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initViews();
        setupAnimations();
        setupClickListeners();
        observeViewModel();
        setNextQuote();
        startQuoteRotation();

        viewModel.loadDashboardData();
    }

    /**
     * Sets up the status bar to use light icons on a light background.
     */
    private void setupStatusBar() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * Initializes views by finding them in the layout.
     */
    private void initViews() {
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        syncUsersButton = findViewById(R.id.loadUsersButton);
        displayUsersButton = findViewById(R.id.displayUsersButton);
        addUserButton = findViewById(R.id.addUserButton);
        totalUsersTextView = findViewById(R.id.totalUsersTextView);
        recentlyAddedTextView = findViewById(R.id.recentlyAddedTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
    }

    /**
     * Sets up animations for various UI elements.
     */
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

    /**
     * Sets up click listeners for buttons.
     */
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

    /**
     * Animates a button with a scale animation.
     *
     * @param v The view (button) to animate.
     */
    private void animateButton(View v) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        v.startAnimation(scaleAnimation);
    }

    /**
     * Sets up observers for the ViewModel's LiveData.
     */
    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
        viewModel.getSyncSuccess().observe(this, this::showSyncSuccessMessage);
        viewModel.getTotalUsers().observe(this, this::updateTotalUsers);
        viewModel.getRecentlyAddedUsers().observe(this, this::updateRecentlyAdded);
    }

    /**
     * Updates the UI loading state.
     *
     * @param isLoading Boolean indicating if the app is in a loading state.
     */
    private void updateLoadingState(Boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        syncUsersButton.setEnabled(!isLoading);
        displayUsersButton.setEnabled(!isLoading);
        addUserButton.setEnabled(!isLoading);
    }

    /**
     * Shows a success message when syncing is complete.
     *
     * @param success Boolean indicating if the sync was successful.
     */
    private void showSyncSuccessMessage(Boolean success) {
        if (success) {
            Snackbar.make(findViewById(android.R.id.content), "Syncing successful", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows an error message.
     *
     * @param message The error message to display.
     */
    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Updates the total users count in the UI.
     *
     * @param total The total number of users.
     */
    private void updateTotalUsers(int total) {
        totalUsersTextView.setText(String.valueOf(total));
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        totalUsersTextView.startAnimation(fadeIn);
    }

    /**
     * Updates the recently added users count in the UI.
     *
     * @param count The number of recently added users.
     */
    private void updateRecentlyAdded(int count) {
        recentlyAddedTextView.setText(String.valueOf(count));
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        recentlyAddedTextView.startAnimation(fadeIn);
    }

    /**
     * Sets the next quote in the rotation.
     */
    private void setNextQuote() {
        String quote = quotes[currentQuoteIndex];
        quoteTextView.setText(quote);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        quoteTextView.startAnimation(fadeIn);

        currentQuoteIndex = (currentQuoteIndex + 1) % quotes.length;
    }

    /**
     * Starts the quote rotation.
     */
    private void startQuoteRotation() {
        quoteHandler.postDelayed(quoteRunnable, 7000); // Start after 7 seconds
    }

    /**
     * Stops the quote rotation.
     */
    private void stopQuoteRotation() {
        quoteHandler.removeCallbacks(quoteRunnable);
    }

    /**
     * When activity resumes, executes the loading of the dashboard and starts the rotation
     * */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
        startQuoteRotation();
    }

    /**
     * When activity pauses (enters another activity but keeps this alive) stops the rotation
     * */
    @Override
    protected void onPause() {
        super.onPause();
        stopQuoteRotation();
    }
}