package com.example.medinear.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.medinear.R;
import com.example.medinear.utils.LocalStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2500; // 2.5 seconds
    private static final int FADE_DURATION = 1000;

    private FirebaseAuth mAuth;
    private ImageView ivLogo;
    private TextView tvAppName, tvTagline, tvLoadingText;
    private ProgressBar progressBar;

    private boolean isFcmTokenFetched = false;
    private boolean isAuthChecked = false;
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        initViews();

        // Apply animations
        startAnimations();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Check authentication
        checkAuthentication();

        // Get FCM token for notifications
        getFCMToken();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        progressBar = findViewById(R.id.progressBar);

        // Set custom colors if needed
        tvAppName.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
    }

    private void startAnimations() {
        // Logo bounce animation
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        ivLogo.startAnimation(bounceAnim);

        // Fade in app name
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvAppName.startAnimation(fadeInAnim);

        // Slide up tagline with delay
        Animation slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideUpAnim.setStartOffset(300);
        tvTagline.startAnimation(slideUpAnim);

        // Animate progress bar
        animateProgressBar();
    }

    private void animateProgressBar() {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(SPLASH_DELAY);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Progress complete
            }
        });
        animation.start();
    }

    private void checkAuthentication() {
        // Update loading text
        updateLoadingText("Checking authentication...");

        mainHandler.postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            isAuthChecked = true;
            updateLoadingText("Authentication verified ✅");

            // Wait a bit before navigating
            mainHandler.postDelayed(this::navigateToNextActivity, 500);
        }, 1000);
    }

    private void getFCMToken() {
        updateLoadingText("Setting up notifications...");

        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d(TAG, "FCM Token: " + token);

                            // Save token to SharedPreferences
                            SharedPreferences fcmPrefs = getSharedPreferences("FCM", MODE_PRIVATE);
                            fcmPrefs.edit().putString("fcm_token", token).apply();

                            // Also save to LocalStorageHelper
                            new LocalStorageHelper(this).saveFcmToken(token);

                            isFcmTokenFetched = true;
                            updateLoadingText("Notifications ready 🔔");
                            Log.d(TAG, "FCM token saved successfully");
                        } else {
                            Log.e(TAG, "Failed to get FCM token", task.getException());
                            isFcmTokenFetched = true; // Continue anyway
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "FCM not available", e);
            isFcmTokenFetched = true; // Continue anyway
        }
    }

    private void updateLoadingText(String text) {
        runOnUiThread(() -> {
            tvLoadingText.setText(text);
            tvLoadingText.setAlpha(0f);
            tvLoadingText.animate().alpha(1f).setDuration(300).start();
        });
    }

    private void navigateToNextActivity() {
        // Ensure both auth check and FCM token are done
        if (!isAuthChecked) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            // User is logged in
            Log.d(TAG, "User logged in: " + currentUser.getEmail());
            intent = new Intent(SplashActivity.this, HomeActivity.class);

            // Pass user data if needed
            if (currentUser.getDisplayName() != null) {
                SharedPreferences prefs = getSharedPreferences("MediNearPrefs", MODE_PRIVATE);
                prefs.edit().putString("user_name", currentUser.getDisplayName()).apply();
            }
        } else {
            // User is not logged in
            Log.d(TAG, "User not logged in");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }
}