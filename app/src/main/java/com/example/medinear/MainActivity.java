package com.example.medinear;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medinear.activities.HomeActivity;
import com.example.medinear.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnGoToHome;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        btnGoToHome = findViewById(R.id.btnGoToHome);
        progressBar = findViewById(R.id.progressBar);

        // Check user authentication
        checkUserAuthentication();
    }

    private void checkUserAuthentication() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnGoToHome.setVisibility(View.GONE);

        // Add a small delay for smooth transition
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // User is logged in
                progressBar.setVisibility(View.GONE);
                btnGoToHome.setVisibility(View.VISIBLE);
                btnGoToHome.setText("Go to Home");
                btnGoToHome.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
            } else {
                // User is not logged in
                progressBar.setVisibility(View.GONE);
                btnGoToHome.setVisibility(View.VISIBLE);
                btnGoToHome.setText("Login / Register");
                btnGoToHome.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        }, 1500); // Show splash for 1.5 seconds
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Re-check authentication when returning to this activity
        checkUserAuthentication();
    }
}