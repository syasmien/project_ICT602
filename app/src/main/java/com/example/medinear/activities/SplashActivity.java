package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        // Delay to show splash screen
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // User is logged in
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                // User is not logged in
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);

        // Get FCM token (only if you have FCM enabled)
        getFCMToken();
    }

    private void getFCMToken() {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d(TAG, "FCM Token: " + token);
                            // Save token to SharedPreferences or send to server
                            getSharedPreferences("FCM", MODE_PRIVATE)
                                    .edit()
                                    .putString("fcm_token", token)
                                    .apply();
                        } else {
                            Log.e(TAG, "Failed to get FCM token", task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "FCM not available", e);
        }
    }
}