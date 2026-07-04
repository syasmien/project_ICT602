package com.example.medinear.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("MediNearPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {

        // Login Button
        btnLogin.setOnClickListener(v -> loginUser());

        // Register
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your email first");
                etEmail.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email has been sent. Please check your email.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String message = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Failed to send password reset email.";

                            Toast.makeText(LoginActivity.this,
                                    message,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("is_logged_in", true);
                        editor.putString("user_email", email);

                        if (user != null && user.getDisplayName() != null) {
                            editor.putString("user_name", user.getDisplayName());
                        } else {
                            editor.putString("user_name",
                                    email.substring(0, email.indexOf("@")));
                        }

                        editor.apply();

                        Toast.makeText(LoginActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();

                    } else {

                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed.";

                        Toast.makeText(LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}